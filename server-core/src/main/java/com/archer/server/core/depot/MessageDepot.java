package com.archer.server.core.depot;

import com.alibaba.fastjson.JSON;
import com.archer.server.core.bean.AppInfo;
import com.archer.server.core.bean.CommonScheduledExecutor;
import com.archer.server.core.bean.MessageUpdateExecutor;
import com.archer.server.core.count.Counter;
import com.archer.server.core.dao.mapper.ArcherMessageMapper;
import com.archer.server.core.model.ArcherMessage;
import com.archer.server.core.service.application.ApplicationService;
import com.archer.server.core.service.cluster.ClusterService;
import com.archer.server.common.constant.RedisKey;
import com.archer.server.common.constant.StatusConstants;
import com.archer.server.common.constant.ValueConstants;
import com.archer.server.common.exception.ApplicationInvalidException;
import com.archer.server.common.exception.RestRuntimeException;
import com.archer.server.common.util.GrayLogUtil;
import com.archer.server.common.util.HttpRequestUtil;
import com.archer.server.common.util.TimeUtil;
import com.archer.server.core.service.cluster.MessageCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消息仓库
 *
 * @author Shinobu
 * @since 2018/11/27
 */
@Component
public class MessageDepot {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDepot.class);

    /**
     * 发送错误计数大于此值时将导致应用关闭
     */
    private static final int SHUTDOWN_ERROR_COUNT = 50;

    /**
     * 发送中消息的索引实例包装类
     *
     * 为了保证内存操作与缓存操作的同步性，并提供故障恢复机制
     */
    public static class IdIndex {

        /**
         * 消息ID索引
         */
        private final static ConcurrentHashMap<String, ArcherMessage> ID_INDEX = new ConcurrentHashMap<>(1024);

        private static volatile MessageCacheService messageCacheService = null;

        private static volatile String appId = null;

        public static void init(@NotNull MessageCacheService m, @NotNull String appId) {
            if (messageCacheService == null) {
                synchronized (IdIndex.class) {
                    if (messageCacheService == null) {
                        messageCacheService = m;
                        IdIndex.appId = appId;
                    }
                }
            }
        }

        public static void refreshAppId(@NotNull String appId) {
            IdIndex.appId = appId;
        }

        public static List<String> keys() {
            return new ArrayList<>(ID_INDEX.keySet());
        }

        public static void removeAll() {
            ID_INDEX.forEach((k, v) -> remove(k));
        }

        public static int size() {
            return ID_INDEX.size();
        }

        static ArcherMessage get(@NotNull String messageId) {
            return ID_INDEX.get(messageId);
        }

        static void putIfAbsent(@NotNull String messageId, ArcherMessage message) {

            if (messageCacheService.putIfAbsent(messageId, appId)) {
                throw new RestRuntimeException("MessageDepot.IdIndex.putIfAbsent: 消息（" + messageId + "）正在发送中，持有者：" +
                        messageCacheService.getAppIdOfMessage(messageId));
            }

            var previous = ID_INDEX.putIfAbsent(messageId, message);
            if (previous != null) {
                throw new RestRuntimeException("MessageDepot.IdIndex.putIfAbsent: 消息（" + messageId + "）已存在");
            }
        }

        static void remove(@NotNull String messageId) {
            if (ID_INDEX.remove(messageId) == null) {
                throw new RestRuntimeException("MessageDepot.IdIndex.putIfAbsent: 消息（" + messageId + "）不存在");
            }
            messageCacheService.delete(messageId);
        }

    }

    private ConcurrentHashMap<ArcherMessage, String> updatingMessages = new ConcurrentHashMap<>(1024);

    /**
     * 发送错误计数，连续十次错误将导致应用关闭
     */
    private final AtomicInteger sendErrorCount = new AtomicInteger(0);

    private final AppInfo appInfo;

    private final ArcherMessageMapper archerMessageMapper;

    private final MessageUpdateExecutor messageUpdateExecutor;

    private final CommonScheduledExecutor commonScheduledExecutor;

    private final ThreadPoolExecutor commonParallelExecutor;

    private final ApplicationService applicationService;

    private final ClusterService clusterService;

    @Autowired
    public MessageDepot(AppInfo appInfo, ArcherMessageMapper archerMessageMapper, MessageUpdateExecutor messageUpdateExecutor,
                        CommonScheduledExecutor commonScheduledExecutor, ThreadPoolExecutor commonParallelExecutor,
                        ApplicationService applicationService, ClusterService clusterService) {
        this.appInfo = appInfo;
        this.archerMessageMapper = archerMessageMapper;
        this.messageUpdateExecutor = messageUpdateExecutor;
        this.commonScheduledExecutor = commonScheduledExecutor;
        this.commonParallelExecutor = commonParallelExecutor;
        this.applicationService = applicationService;
        this.clusterService = clusterService;
    }

    /**
     * 将消息加入重发流程
     */
    public void produce(@NotNull ArcherMessage message) {
        if (!appInfo.isRunning()) {
            return;
        }
        commonParallelExecutor.execute(() -> {
            if (IdIndex.get(message.getId()) != null) {
                LOGGER.warn("MessageDepot.produce: 待入库消息ID发生重复，id: " + message.getId());
                return;
            }
            IdIndex.putIfAbsent(message.getId(), message);
            route(message);
        });
    }

    /**
     * route message
     */
    private void route(@NotNull ArcherMessage message) {
        if (!appInfo.isRunning()) {
            return;
        }

        // 重发完毕
        var nextInterval = message.nextInterval();
        if (nextInterval == null) {
            IdIndex.remove(message.getId());
            message.setStatus(StatusConstants.END_COMPLETED);
            update(message);
            GrayLogUtil.messageLifecycleLog(message.getId(), "finished", TimeUtil.nowMillisStr(), null);
            return;
        }
        // 间隔过长，转为闲置状态
        var nextMillisCountdown = message.nextMillisCountdown();
        if (nextMillisCountdown >= ValueConstants.SLOW_MESSAGE_COUNTDOWN) {
            IdIndex.remove(message.getId());
            message.setStatus(StatusConstants.IDLE);
            var now = new Date();
            // 提前一分钟回到队列
            message.setNextTime(new Date(now.getTime() + nextMillisCountdown - 60 * 1000));
            update(message);
            GrayLogUtil.messageLifecycleLog(message.getId(), "change to IDLE", TimeUtil.nowMillisStr(), null);
            return;
        }

        // 发送
        var clone = message.deepClone();
        message.levelUp();
        update(message);
        commonScheduledExecutor.schedule(() -> {
            try {
                send(clone, message);
                route(message);
                sendErrorCount.set(0);
            } catch (Exception e) {
                sendErrorCount.getAndIncrement();
                LOGGER.error("MessageDepot.send: error, error count: " + sendErrorCount.intValue(), e);
                if (sendErrorCount.intValue() > SHUTDOWN_ERROR_COUNT) {
                    applicationService.shutdown();
                }
            }
        }, nextMillisCountdown, TimeUnit.MICROSECONDS);
    }

    /**
     * 发送消息
     */
    private void send(@NotNull ArcherMessage snapshot, @NotNull ArcherMessage message) {
        // 检查是否已被主动停止以及收到响应
        if (hadStoppedOrResponded(message.getId())) {
            IdIndex.remove(message.getId());
            return;
        }

        var request = buildRequest(snapshot);
        message.setLastTime(new Date());
        var time = TimeUtil.nowMillisStr();
        GrayLogUtil.messageLifecycleLog(snapshot.getId(), "sending, index: " + snapshot.getIntervalIndex(), time, null);
        GrayLogUtil.httpPacketLog(snapshot.getId(), 1, time, request.toString(), JSON.toJSONString(request.headers().map()), snapshot.getContent());
        var completableFuture = HttpRequestUtil.CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        Counter.SEND_COUNTER.increase();
        clusterService.increaseSendCount();
        update(message);

        completableFuture.whenCompleteAsync((response, throwable) -> {
            if (throwable != null) {
                handleThrows(snapshot, throwable);
                return;
            }
            handleResponse(snapshot, message, response);
        });
    }

    private void handleThrows(ArcherMessage snapshot, Throwable throwable) {
        var messageId = snapshot.getId();
        GrayLogUtil.messageLifecycleLog(messageId, "sending error, index: " + snapshot.getIntervalIndex(), TimeUtil.nowMillisStr(), null);
        LOGGER.error("send.handleThrows: 消息（" + messageId + "）发送失败", throwable);
    }

    private void handleResponse(@NotNull ArcherMessage snapshot, @NotNull ArcherMessage message, HttpResponse<String> response) {
        var body = response.body();
        var messageId = snapshot.getId();
        var time = TimeUtil.nowMillisStr();
        GrayLogUtil.messageLifecycleLog(messageId, "sending responded, index: " + snapshot.getIntervalIndex(), time, body);
        GrayLogUtil.httpPacketLog(messageId, 2, time, response.toString(), JSON.toJSONString(response.headers().map()), body);
        if (body != null && snapshot.getStoppedKeyWordsList().stream()
                .anyMatch(k -> body.contains(k.toUpperCase()) || body.contains(k.toLowerCase()))) {
            IdIndex.remove(messageId);
            message.setStatus(StatusConstants.END_RESPONDED);
            update(message);
        }
    }

    /**
     * 是否已被主动停止或已收到响应
     */
    private boolean hadStoppedOrResponded(@NotNull String messageId) {
        if (IdIndex.get(messageId) == null) {
            return true;
        }
        var dbStatus = archerMessageMapper.selectStatusByPrimaryKey(messageId);
        return StatusConstants.END_RESPONDED.equals(dbStatus) || StatusConstants.END_STOPPED.equals(dbStatus);
    }

    /**
     * build http request
     */
    private HttpRequest buildRequest(@NotNull ArcherMessage snapshot) {
        var builder = HttpRequest.newBuilder(URI.create(snapshot.getUrl()));
        switch (snapshot.getMethod().toUpperCase()) {
            case "GET":
                builder.GET();
                break;
            case "POST":
                builder.POST(HttpRequest.BodyPublishers.ofString(snapshot.getContent(), StandardCharsets.UTF_8));
                break;
            default:
                throw new ApplicationInvalidException("消息请求method有误", snapshot);
        }
        if (ValueConstants.POST_CONTENT_TYPE_JSON.equals(snapshot.getContentType())) {
            builder.header("Content-Type", "application/json");
        }
        else if (ValueConstants.POST_CONTENT_TYPE_FORM.equals(snapshot.getContentType())) {
            builder.header("Content-Type", "application/x-www-form-urlencoded");
        }
        snapshot.getHeaders().forEach(builder::header);
        return builder.build();
    }

    /**
     * 更新消息主要字段
     */
    private void update(ArcherMessage message) {
        if (!appInfo.isRunning() || updatingMessages.contains(message)) {
            return;
        }
        updatingMessages.put(message, "");
        messageUpdateExecutor.execute(() -> {
            updatingMessages.remove(message);
            var clone = message.simplyClone();
            archerMessageMapper.updateSendingMainColumnsById(clone.getId(), clone.getStatus(),
                    clone.getLastTime(), clone.getNextTime(), clone.getIntervalIndex());
        });
    }

}
