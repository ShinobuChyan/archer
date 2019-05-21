package com.archer.server.core.service.cluster.impl;

import com.archer.server.core.bean.AppInfo;
import com.archer.server.core.model.ClusterLockName;
import com.archer.server.core.service.cluster.ClusterService;
import com.archer.server.common.constant.RedisKey;
import com.archer.server.common.exception.ApplicationInvalidException;
import com.archer.server.common.exception.RestRuntimeException;
import com.archer.server.common.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

/**
 * 集群相关
 *
 * @author Shinobu
 * @since 3.1.0
 */
@Service
public class ClusterServiceImpl implements ClusterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterServiceImpl.class);

    private static final String APP_VALID_VALUE = "valid";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AppInfo appInfo;

    /**
     * 向集群注册自身，有效时间为60S
     */
    @Override
    public void register() {
        String appId = appInfo.getAppId();
        if (appId != null && !isInvalidApp(appId)) {
            LOGGER.warn("实例注册失败：原appId尚未失效, " + appId);
            return;
        }

        appId = ProcessHandle.current().pid() + "@" + System.getProperty("os.hostname");
        String s = stringRedisTemplate.opsForValue().get(RedisKey.CLUSTER_REGISTRY + appId);
        if (s != null) {
            throw new RestRuntimeException("实例注册失败：appId已存在, " + appId);
        }

        Boolean valid = stringRedisTemplate.opsForValue().setIfAbsent(RedisKey.CLUSTER_REGISTRY + appId, APP_VALID_VALUE);
        if (valid != null && valid) {
            stringRedisTemplate.expire(RedisKey.CLUSTER_REGISTRY + appId, 60L, TimeUnit.SECONDS);
            appInfo.setAppId(appId);
            LOGGER.info("集群注册成功，appId：" + appId);
            return;
        }
        throw new RestRuntimeException("实例注册失败：appId已存在, " + appId);
    }

    /**
     * 心跳
     */
    @Override
    public void heartbeat() {
        if (appInfo.getAppId() == null) {
            throw new RestRuntimeException("实例尚未注册");
        }
        String s = stringRedisTemplate.opsForValue().get(RedisKey.CLUSTER_REGISTRY + appInfo.getAppId());
        if (s == null) {
            throw new ApplicationInvalidException("心跳失败：无法查询到注册信息, app = " + appInfo.getAppId());
        }
        stringRedisTemplate.opsForValue().set(RedisKey.CLUSTER_REGISTRY + appInfo.getAppId(), APP_VALID_VALUE, 60L, TimeUnit.SECONDS);
    }

    /**
     * 注销
     */
    @Override
    public void logout() {
        String appId = appInfo.getAppId();
        if (appId == null || isInvalidApp(appId)) {
            return;
        }

        var r = stringRedisTemplate.delete(RedisKey.CLUSTER_REGISTRY + appInfo.getAppId());
        if (r != null && r) {
            appInfo.setAppId(null);
            appInfo.setRunning(false);
            LOGGER.info("实例已从集群注销");
        }
    }

    /**
     * 判断实例是否已经失效
     *
     * @param appId 实例ID
     * @return 是否已经失效
     */
    @Override
    public boolean isInvalidApp(String appId) {
        return appId == null || stringRedisTemplate.opsForValue().get(RedisKey.CLUSTER_REGISTRY + appId) == null;
    }

    /**
     * 获取独占锁，锁默认六十秒超时，获取行为默认十秒超时
     *
     * @param lockName 锁名称
     */
    @Override
    public void lock(@NotNull ClusterLockName lockName) {
        lock(lockName, 10L);
    }

    /**
     * 尝试获取锁
     *
     * @param lockName 锁名称
     * @param seconds  最大阻塞时间
     * @return isSuccess
     */
    @Override
    public boolean tryLock(ClusterLockName lockName, long seconds) {
        try {
            lock(lockName, seconds);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 释放锁
     */
    @Override
    public void unlock(@NotNull ClusterLockName lockName) {
        String appId = stringRedisTemplate.opsForValue().get(RedisKey.CLUSTER_LOCK + lockName);
        if (appId == null || !appId.equals(appInfo.getAppId())) {
            LOGGER.warn("锁已失效或非当前实例持有，lock = " + lockName + ", holder = " + appId);
            return;
        }
        stringRedisTemplate.delete(RedisKey.CLUSTER_LOCK + lockName);
    }

    /**
     * 增加集群消息入界计数
     */
    @Override
    public void increaseInboundCount() {
        stringRedisTemplate.opsForValue().increment(RedisKey.CLUSTER_COUNT_INBOUND + TimeUtil.nowDateStr(), 1);
    }

    /**
     * 增加集群http请求发送计数
     */
    @Override
    public void increaseSendCount() {
        stringRedisTemplate.opsForValue().increment(RedisKey.CLUSTER_COUNT_SEND + TimeUtil.nowDateStr(), 1);
    }

    private void lock(@NotNull ClusterLockName lockName, long seconds) {
        if (appInfo.getAppId().equals(stringRedisTemplate.opsForValue().get(lockName))) {
            return;
        }

        long maxCount = seconds * 10;
        long count = 0;
        while (count < maxCount) {
            Boolean isSuccess = stringRedisTemplate.opsForValue().setIfAbsent(RedisKey.CLUSTER_LOCK + lockName, appInfo.getAppId());
            if (isSuccess != null && isSuccess) {
                stringRedisTemplate.expire(RedisKey.CLUSTER_LOCK + lockName, 60L, TimeUnit.SECONDS);
                return;
            }
            count++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RestRuntimeException(e.getMessage(), e);
            }
        }
        throw new RestRuntimeException("锁获取超时：lock = " + lockName);
    }
}
