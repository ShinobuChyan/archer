package com.archer.server.core.model;

/**
 * 集群锁可选类型
 *
 * @author Shinobu
 * @since 2018/9/25
 */
public enum ClusterLockEnum {

    /**
     * 获取闲置消息的锁
     */
    FETCH_IDLE_MESSAGE,
    /**
     * 检查已失效app占有消息的行为的锁
     */
    CHECK_MESSAGE_APP_INVALID

}
