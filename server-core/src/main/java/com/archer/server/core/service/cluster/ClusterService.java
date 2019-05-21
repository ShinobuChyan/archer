package com.archer.server.core.service.cluster;

import com.archer.server.core.model.ClusterLockName;

import javax.validation.constraints.NotNull;

/**
 * 集群相关
 *
 * @author Shinobu
 * @since 3.1.0
 */
public interface ClusterService {

    /**
     * 向集群注册自身，有效时间为20S
     */
    void register();

    /**
     * 心跳
     */
    void heartbeat();

    /**
     * 注销
     */
    void logout();

    /**
     * 判断实例是否已经失效
     *
     * @param appId 实例ID
     * @return 是否已经失效
     */
    boolean isInvalidApp(String appId);

    /**
     * 获取独占锁，锁默认六十秒超时，获取行为默认十秒超时
     *
     * @param lockName 锁名称
     */
    void lock(@NotNull ClusterLockName lockName);

    /**
     * 尝试获取锁
     *
     * @param lockName 锁名称
     * @param seconds  最大阻塞时间
     * @return isSuccess
     */
    boolean tryLock(@NotNull ClusterLockName lockName, @NotNull long seconds);

    /**
     * 释放锁
     *
     * @param lockName 锁名称
     */
    void unlock(@NotNull ClusterLockName lockName);

    /**
     * 增加集群消息入界计数
     */
    void increaseInboundCount();

    /**
     * 增加集群http请求发送计数
     */
    void increaseSendCount();

}
