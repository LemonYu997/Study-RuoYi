package com.lemon.common.utils.spring;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * spring工具类
 * 继承于hutool的自制加强版
 */
@Component
public final class SpringUtils extends SpringUtil {
    /**
     * 获取 spring 上下文
     */
    public static ApplicationContext context() {
        return getApplicationContext();
    }

    /**
     * 获取aop代理对象
     *
     * @param invoker
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker) {
        return (T) AopContext.currentProxy();
    }
}
