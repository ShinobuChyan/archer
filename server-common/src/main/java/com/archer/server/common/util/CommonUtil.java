package com.archer.server.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * 通用未分类方法
 *
 * @author Shinobu
 * @since 2018/3/6
 */
public class CommonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtil.class);

    /**
     * 实体类同名属性互传
     *
     * @param source 源实体
     * @param target 目标实体
     */
    public static void attrTransfer(Object source, Object target) {

        Class sClass = source.getClass();
        Class tClass = target.getClass();

        Field[] sFields = sClass.getDeclaredFields();

        for (Field sField : sFields) {

            if ("serialVersionUID".equals(sField.getName())) {
                continue;
            }

            try {
                Field tField = tClass.getDeclaredField(sField.getName());
                sField.setAccessible(true);
                tField.setAccessible(true);
                tField.set(target, sField.get(source));
            } catch (Exception ignore) {
            }
        }
    }

}
