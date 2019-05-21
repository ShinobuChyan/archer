package com.archer.server.common.interfaces;

import java.lang.reflect.Field;

/**
 * 含有String字段的Rest API接口参数VO类须实现此接口
 *
 * 内部如有实体结构则递归trim
 *
 * @author Shinobu
 * @since 2017/12/21
 */
public interface Trimmable {

    /**
     * 去除String字段的首尾空格、将空字符串的字段置为null
     */
    default void trim() {

        Class c = this.getClass();

        for (Field field : c.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if ((field.get(this) instanceof String)) {
                    field.set(this, "".equals(field.get(this)) ? null : ((String) field.get(this)).trim());
                }
                if ((field.get(this) instanceof Trimmable)) {
                    ((Trimmable) field.get(this)).trim();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
