package com.lemon.framework.config;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.framework.config.properties.RedissonProperties;
import com.lemon.framework.handler.KeyPrefixHandler;
import lombok.extern.slf4j.Slf4j;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redis配置
 * 注解@EnableCaching：开启Spring Cache，需要搭配 CacheManager一起使用
 * 注解@EnableConfigurationProperties：使使用 @ConfigurationProperties 注解的类生效。
 */
@Slf4j
@Configuration
@EnableCaching
@EnableConfigurationProperties(RedissonProperties.class)
public class RedisConfig {

    @Autowired
    private RedissonProperties redissonProperties;

    /**
     * 自定义缓存管理器 整合spring-cache
     */
    @Bean
    public CacheManager cacheManager() {
        //TODO 之后需要使用自定义缓存容器
        return new ConcurrentMapCacheManager();
    }

    /**
     * 配置 Redisson
     * 分为单机模式和集群模式
     */
    @Bean
    public RedissonAutoConfigurationCustomizer redissonCustomizer() {
        return config -> {
            //线程池数量
            config.setThreads(redissonProperties.getThreads())
                //Netty线程池数量,默认值 = 当前处理核数量 * 2
                .setNettyThreads(redissonProperties.getNettyThreads())
                //使用 Jackson 作为序列化方式
                .setCodec(new JsonJacksonCodec());

            //获取自定义的单机配置 如果有就使用
            RedissonProperties.SingleServerConfig singleServerConfig = redissonProperties.getSingleServerConfig();
            if (ObjectUtil.isNull(singleServerConfig)) {
                //使用单机模式
                config.useSingleServer()
                    //设置redis key前缀
                    .setNameMapper(new KeyPrefixHandler(redissonProperties.getKeyPrefix()))
                    //命令等待超时，单位：毫秒
                    .setTimeout(singleServerConfig.getTimeout())
                    //客户端名称
                    .setClientName(singleServerConfig.getClientName())
                    //连接空闲超时，单位：毫秒
                    .setIdleConnectionTimeout(singleServerConfig.getIdleConnectionTimeout())
                    //发布和订阅连接池大小
                    .setSubscriptionConnectionPoolSize(singleServerConfig.getSubscriptionConnectionPoolSize())
                    //最小空闲连接数
                    .setConnectionMinimumIdleSize(singleServerConfig.getConnectionMinimumIdleSize())
                    //连接池大小
                    .setConnectionPoolSize(singleServerConfig.getConnectionPoolSize());
            }
            log.info("初始化 redis 配置");
        };
    }

    /*
     * redis集群配置 yml
     *
     * --- # redis 集群配置(单机与集群只能开启一个另一个需要注释掉)
     * spring:
     *   redis:
     *     cluster:
     *       nodes:
     *         - 192.168.0.100:6379
     *         - 192.168.0.101:6379
     *         - 192.168.0.102:6379
     *     # 密码
     *     password:
     *     # 连接超时时间
     *     timeout: 10s
     *     # 是否开启ssl
     *     ssl: false
     *
     * redisson:
     *   # 线程池数量
     *   threads: 16
     *   # Netty线程池数量
     *   nettyThreads: 32
     *   # 集群配置
     *   clusterServersConfig:
     *     # 客户端名称
     *     clientName: ${ruoyi.name}
     *     # master最小空闲连接数
     *     masterConnectionMinimumIdleSize: 32
     *     # master连接池大小
     *     masterConnectionPoolSize: 64
     *     # slave最小空闲连接数
     *     slaveConnectionMinimumIdleSize: 32
     *     # slave连接池大小
     *     slaveConnectionPoolSize: 64
     *     # 连接空闲超时，单位：毫秒
     *     idleConnectionTimeout: 10000
     *     # 命令等待超时，单位：毫秒
     *     timeout: 3000
     *     # 发布和订阅连接池大小
     *     subscriptionConnectionPoolSize: 50
     *     # 读取模式
     *     readMode: "SLAVE"
     *     # 订阅模式
     *     subscriptionMode: "MASTER"
     */
}
