package com.lemon.common.annotation;

import java.lang.annotation.*;

/**
 * SQL数据权限注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {
    DataColumn[] value();
}
