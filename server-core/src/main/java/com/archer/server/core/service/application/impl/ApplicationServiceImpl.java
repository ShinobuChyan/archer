package com.archer.server.core.service.application.impl;

import com.alibaba.fastjson.JSON;
import com.archer.server.core.bean.AppInfo;
import com.archer.server.core.bean.CommonScheduledExecutor;
import com.archer.server.core.bean.MessageUpdateExecutor;
import com.archer.server.core.count.Counter;
import com.archer.server.core.dao.mapper.ArcherMessageMapper;
import com.archer.server.core.depot.MessageDepot;
import com.archer.server.core.model.BasicAppStatusInfo;
import com.archer.server.core.service.application.ApplicationService;
import com.archer.server.core.service.cluster.ClusterService;
import com.archer.server.common.constant.RedisKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 应用实例相关服务
 *
 * @author Shinobu
 * @since 2019/1/17
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    @Resource
    private AppInfo appInfo;

    @Resource
    private ClusterService clusterService;

    @Resource
    private CommonScheduledExecutor commonScheduledExecutor;

    @Resource
    private MessageUpdateExecutor messageUpdateExecutor;

    @Resource
    private ThreadPoolExecutor grayLogExecutor;

    @Resource
    private ArcherMessageMapper archerMessageMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 停止应用
     */
    @Override
    public synchronized void shutdown() {
        clusterService.logout();
        var sendTasks = commonScheduledExecutor.shutdownNow();
        LOGGER.info("shutdown: commonScheduledExecutor已停止，未执行任务数: " + sendTasks.size());
        var updateTasks = messageUpdateExecutor.shutdownNow();
        LOGGER.info("shutdown: messageUpdateExecutor已停止，未执行任务数: " + updateTasks.size());
        var sendingMessageIdList = MessageDepot.IdIndex.keys();
        LOGGER.info("shutdown: 重发流程中的消息数: " + sendingMessageIdList.size());
        MessageDepot.IdIndex.removeAll();
        LOGGER.info("shutdown: 重发流程中的消息已清空");
        var idleCount = archerMessageMapper.updateSendingMessageToIdleByIdListWhenAppShutdown(sendingMessageIdList);
        LOGGER.info("shutdown: 转为闲置状态的消息数: " + idleCount);
        LOGGER.info("shutdown: finished");
    }

    /**
     * 重新启动
     */
    @Override
    public synchronized void restart() {
        if (appInfo.isRunning()) {
            LOGGER.info("restart: 应用正在运行中");
            return;
        }
        clusterService.register();
        MessageDepot.IdIndex.refreshAppKey(appInfo.getAppId());
        commonScheduledExecutor.restart();
        messageUpdateExecutor.restart();
        appInfo.setRunning(true);
        LOGGER.info("restart: 应用已重新启动");
    }

    /**
     * 刷新redis中的应用基本信息
     */
    @Override
    public void refreshBasicAppStatusInfo() {
        var appId = appInfo.getAppId();
        if (appId == null) {
            return;
        }

        var basicInfo = new BasicAppStatusInfo()
                .setAppId(appId)
                .setRunning(appInfo.isRunning())
                .setScheduledExecutorRunning(commonScheduledExecutor.isRunning())
                .setScheduledExecutorTaskCount(commonScheduledExecutor.getTaskCount())
                .setUpdateExecutorRunning(messageUpdateExecutor.isRunning())
                .setUpdateExecutorTaskCount(messageUpdateExecutor.getTaskCount())
                .setProcessingMessageCount(MessageDepot.IdIndex.size())
                .setLifecycleExecutorTaskCount(grayLogExecutor.getTaskCount())
                .setInboundCount(Counter.INBOUND_COUNTER.toLinkedMap())
                .setSendCount(Counter.SEND_COUNTER.toLinkedMap());
        stringRedisTemplate.opsForHash().put(RedisKey.APP_BASIC_INFO, appId, JSON.toJSONString(basicInfo));
        stringRedisTemplate.expire(RedisKey.APP_BASIC_INFO + ":" + appId, 30L, TimeUnit.SECONDS);
    }
}
