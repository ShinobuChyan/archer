package com.archer.server.core.scheduled;

import com.archer.server.common.util.TimeUtil;
import com.archer.server.core.bean.AppInfo;
import com.archer.server.core.count.Counter;
import com.archer.server.core.dao.mapper.ArcherMessageMapper;
import com.archer.server.core.depot.MessageDepot;
import com.archer.server.core.entity.ArcherMessageEntity;
import com.archer.server.core.model.ArcherMessage;
import com.archer.server.core.model.ClusterLockEnum;
import com.archer.server.core.model.ClusterLockName;
import com.archer.server.core.service.application.ApplicationService;
import com.archer.server.core.service.cluster.ClusterService;
import com.archer.server.common.constant.RedisKey;
import com.archer.server.common.exception.ApplicationInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 主要定时任务
 *
 * @author Shinobu
 * @since 2019/1/17
 */
@Component
public class MainTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainTask.class);

    private final static int HEARTBEAT_PRINT_COUNT = 5;

    private static int heartbeatCount;

    @Resource
    private ApplicationService applicationService;

    @Resource
    private ClusterService clusterService;

    @Resource
    private AppInfo appInfo;

    @Resource
    private ArcherMessageMapper archerMessageMapper;

    @Resource
    private MessageDepot messageDepot;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 保持心跳
     */
    @Scheduled(fixedRate = 1000 * 5)
    public void heartbeat() {
        if (appInfo == null || !appInfo.isRunning()) {
            return;
        }
        try {
            clusterService.heartbeat();
        } catch (ApplicationInvalidException aie) {
            LOGGER.error(aie.getMessage(), aie);
            applicationService.shutdown();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (++heartbeatCount == HEARTBEAT_PRINT_COUNT) {
            LOGGER.info("heartbeat");
            heartbeatCount = 0;
        }
    }

    /**
     * 拉取闲置状态的消息
     */
    @Scheduled(fixedRate = 1000 * 30)
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fetchIdleMessages() {
        if (appInfo == null || !appInfo.isRunning()) {
            return;
        }

        // lock
        var lockName = ClusterLockName.newLockName(ClusterLockEnum.FETCH_IDLE_MESSAGE, null);
        boolean lockResult = clusterService.tryLock(lockName, 10);
        if (!lockResult) {
            return;
        }
        try {
            fetchAndProduce();
        } finally {
            clusterService.unlock(lockName);
        }
    }

    private void fetchAndProduce() {
        // fetch & update
        var entities = archerMessageMapper.selectPreparedIdleMessages(TimeUtil.toStandardTimeStr(new Date()));
        var updateCount = archerMessageMapper
                .updateFetchedIdleMessagesByIdList(entities.stream()
                        .map(ArcherMessageEntity::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
        if (!Integer.valueOf(updateCount).equals(entities.size())) {
            LOGGER.warn("fetchIdleMessages: 拉取到的消息数目与更新数目不符, 回滚");
            throw new RuntimeException();
        }

        // produce
        entities.stream()
                .map(ArcherMessage::new)
                .forEach(m -> {
                    try {
                        messageDepot.produce(m);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                });
    }

    /**
     * 每十秒由任一应用实例检查所有发送中消息的所属实例的状态，如果存在stamp则说明刚被检查过
     *
     * 如果消息所属实例已失效，则置为闲置状态
     */
    @Scheduled(fixedRate = 1000 * 10)
    public void checkMessagesOfInvalidInstance() {
        if (appInfo == null || !appInfo.isRunning()) {
            return;
        }
        final var stringRedisTemplate = this.stringRedisTemplate;
        final var clusterService = this.clusterService;
        if (stringRedisTemplate.opsForValue().get(RedisKey.MESSAGE_APP_CHECKING_STAMP) != null) {
            return;
        }

        var lockName = ClusterLockName.newLockName(ClusterLockEnum.CHECK_MESSAGE_APP_INVALID, null);
        var isLocked = clusterService.tryLock(lockName, 1);
        if (!isLocked) {
            return;
        }
        try {
            var sendingMessageIdList = stringRedisTemplate.opsForHash().keys(RedisKey.MESSAGE_APP);
            sendingMessageIdList.forEach(messageId -> {
                if (messageId == null) {
                    return;
                }
                var appId = stringRedisTemplate.opsForHash().get(RedisKey.MESSAGE_APP, messageId);
                if (clusterService.isInvalidApp((String) appId)) {
                    stringRedisTemplate.opsForHash().delete(RedisKey.MESSAGE_APP, messageId);
                    archerMessageMapper.updateFetchedIdleMessagesByIdList(Collections.singletonList((String) messageId));
                }
            });
            stringRedisTemplate.opsForValue().set(RedisKey.MESSAGE_APP_CHECKING_STAMP,
                    String.valueOf(System.currentTimeMillis()),
                    60L, TimeUnit.SECONDS);
        } finally {
            clusterService.unlock(lockName);
        }
    }

    /**
     * 定时刷新应用状态信息、统计计数信息等
     */
    @Scheduled(fixedRate = 1000)
    public void refreshAppStatusInfo() {
        var calendar = Calendar.getInstance();
        Counter.INBOUND_COUNTER.tryRotate(calendar);
        Counter.SEND_COUNTER.tryRotate(calendar);
        applicationService.refreshBasicAppStatusInfo();
    }

}
