package com.archer.server.common.constant;

/**
 * redis的key值
 *
 * @author Shinobu
 * @since 2018/11/6
 */
public class RedisKey {

    /**
     * 集群注册中心redis-key
     */
    public static final String CLUSTER_REGISTRY = "archer-server:cluster:registry:";
    /**
     * 独占锁redis-key
     */
    public static final String CLUSTER_LOCK = "archer-server:cluster:lock:";
    /**
     * 集群计数信息 - 消息入界计数
     */
    public static final String CLUSTER_COUNT_INBOUND = "archer-server:cluster:count:inbound:";
    /**
     * 集群计数信息 - 消息入界计数
     */
    public static final String CLUSTER_COUNT_SEND = "archer-server:cluster:count:send";

    /**
     * 用于存储发送中消息与其所在实例的n-1关系，存储形式为hash，key为消息id，value为实例id
     */
    public static final String MESSAGE_APP = "archer-server:message-app:";
    /**
     * 失效实例检查标签，表示刚被检查过，一分钟后过期
     */
    public static final String MESSAGE_APP_CHECKING_STAMP = "archer-server:message-app:checking-stamp:";

    /**
     * 应用实例相关 - 基本信息
     */
    public static final String APP_BASIC_INFO = "archer-server:app:basic-info:";


}
