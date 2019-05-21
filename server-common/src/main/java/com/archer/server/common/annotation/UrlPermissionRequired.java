package com.archer.server.common.annotation;

import java.lang.annotation.*;

/**
 * 用于检查用户所在角色对Rest API的访问权限
 *
 * 标记于API对应的Controller方法上
 *
 * @author Shinobu
 * @since 2018/3/1
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlPermissionRequired {
}
