package com.archer.server.core.count;

/**
 * 计数相关
 *
 * @author Shinobu
 * @since 2019/1/23
 */
public class Counter {

    /**
     * 消息入界计数
     */
    public static final CyclicCounter INBOUND_COUNTER = new CyclicCounter(24);

    /**
     * http请求发送计数
     */
    public static final CyclicCounter SEND_COUNTER = new CyclicCounter(24);

}
