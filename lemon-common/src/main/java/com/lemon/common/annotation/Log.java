package com.lemon.common.annotation;

import com.lemon.common.enums.BusinessType;
import com.lemon.common.enums.OperatorType;

import java.lang.annotation.*;

/**
 * 自定义日志操作注解
 * Target 可以使用在参数或者方法上
 * Retention 注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在
 * Documented 会被 JavaDoc 工具处理
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 模块
     */
    String title() default "";

    /**
     * 功能
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 操作人类别 默认后台
     */
    OperatorType operatorType() default OperatorType.MANAGE;

    /**
     * 是否保存请求的参数
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应的参数
     */
    boolean isSaveResponseData() default true;

    /**
     * 排除指定的请求参数
     */
    String[] excludeParamNames() default {};
}
