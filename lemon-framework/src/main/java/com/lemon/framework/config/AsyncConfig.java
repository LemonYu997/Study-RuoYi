package com.lemon.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步配置
 * proxyTargetClass = true 表示创建目标代理类
 */
@EnableAsync(proxyTargetClass = true)
@Configuration
public class AsyncConfig {
}
