package com.archer.server.common.constant;

/**
 * @author Shinobu
 * @since 2018/10/30
 */
public class StatusConstants {

    /**
     * 消息状态 - 响应结束
     */
    public static final String END_RESPONDED = "0";
    /**
     * 消息状态 - 自然结束
     */
    public static final String END_COMPLETED = "2";
    /**
     * 消息状态 - 主动结束
     */
    public static final String END_STOPPED = "3";
    /**
     * 消息状态 - 放置中
     */
    public static final String IDLE = "-1";

}
