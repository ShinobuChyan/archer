package com.archer.server.common.constant;

/**
 * @author Shinobu
 * @since 2018/11/5
 */
public class PatternConstants {

    /**
     * 用户名正则：1-16位字母数字的组合，不能是纯数字
     */
    public static final String USERNAME = "^(?![0-9]+$)[0-9A-z]{1,16}$";
    /**
     * 手机号正则：以1开头的11位数字组合
     */
    public static final String MOBILE = "^1[0-9]{10}$";
    /**
     * 邮箱正则：网上抄来的
     */
    public static final String EMAIL = "^[a-z0-9]+([._\\\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$";

    /**
     * URL地址格式正则
     */
    public static final String URL = "(http|https)://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-.,@?^=%&amp;:/~+#]*[\\w\\-@?^=%&amp;/~+#])?";

    public static final String METHOD = "(?i)(get|post)";
    /**
     * 重发间隔格式正则
     */
    public static final String INTERVAL_LIST = "^\\d(,\\d+){0,9}$";

}
