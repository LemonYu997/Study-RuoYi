package com.lemon.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 程序注解配置
 * 注解 @EnableAspectJAutoProxy 开启 AOP
 * exposeProxy 解决代理引发的切面失效问题，设为 true 可以使用 AopContext 获取代理类
 */
@Configuration
// 表示通过aop框架暴露该代理对象,AopContext能够访问
@EnableAspectJAutoProxy(exposeProxy = true)
public class ApplicationConfig {
}
