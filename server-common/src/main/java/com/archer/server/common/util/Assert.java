package com.archer.server.common.util;

import com.archer.server.common.exception.AssertFailedException;

import java.util.Collection;

/**
 * 断言工具
 *
 * @author Shinobu
 * @since 2018/3/23
 */
public class Assert {

    private static final String NOT_NULL = "参数列表不正确";

    private static final String TOO_LONG = "参数长度不正确";

    private static final String TOO_SHORT = "参数长度不正确";

    private static final String MATCH_FAILED = "参数格式有误";

    private static final String NOT_EMPTY = "参数不得为空";

    /**
     * not null
     */
    public static void notNull(Object... objects) {
        if (objects == null) {
            return;
        }
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] == null) {
                throw new AssertFailedException(NOT_NULL + ": " + i);
            }
        }
    }

    /**
     * string length
     */
    public static void length(Integer min, Integer max, String... strings) {
        if (strings == null) {
            return;
        }
        for (int i = 0; i < strings.length; i++) {
            if (strings[i] == null) {
                return;
            }
            if (min != null && strings[i].length() < min) {
                throw new AssertFailedException(TOO_SHORT + ": " + i);
            }
            if (max != null && strings[i].length() > max) {
                throw new AssertFailedException(TOO_LONG + ": " + i);
            }
        }
    }

    /**
     * string pattern
     */
    public static void match(String regexp, String... strings) {
        if (strings == null || regexp == null) {
            return;
        }
        for (int i = 0; i < strings.length; i++) {
            if (strings[i] == null) {
                return;
            }
            if (!strings[i].matches(regexp)) {
                throw new AssertFailedException(MATCH_FAILED + ": " + i);
            }
        }
    }

    /**
     * not empty
     */
    public static void notEmpty(Object... objects) {
        if (objects == null) {
            return;
        }
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] == null) {
                throw new AssertFailedException(NOT_EMPTY + ": " + i);
            }
            if (objects[i] instanceof String && ((String) objects[i]).isEmpty()) {
                throw new AssertFailedException(NOT_EMPTY + ": " + i);
            }
            if (objects[i] instanceof Collection && ((Collection) objects[i]).isEmpty()) {
                throw new AssertFailedException(NOT_EMPTY + ": " + i);
            }
        }
    }

}