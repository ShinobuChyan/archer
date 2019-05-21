package com.archer.server.common.constant;

/**
 * @author Shinobu
 * @since 2018/10/30
 */
public class ValueConstants {

    public static final String CREATOR_SYSTEM = "archer-server";

    /**
     * 慢消息的判断依据：重发倒计时大于等于三十分钟
     */
    public static final int SLOW_MESSAGE_COUNTDOWN = 30 * 60 * 1000;

    /**
     * 标识请求体类型的值 - JSON形式
     */
    public static final String POST_CONTENT_TYPE_JSON = "1";
    /**
     * 标识请求体类型的值 - FORM形式
     */
    public static final String POST_CONTENT_TYPE_FORM = "2";

    /**
     * 默认停止重发的响应体关键词
     */
    public static final String DEFAULT_STOPPED_KEY_WORDS = "success,failed";

}
