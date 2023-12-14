package com.lemon.framework.config;

import cn.hutool.core.net.NetUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.lemon.framework.handler.CreateAndUpdateMetaObjectHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Mybatis-Plus 配置
 * 注解 @EnableTransactionManagement 开启事务管理
 * proxyTargetClass = true 表示使用 CGLIB 动态代理，false 为使用 JDK 动态代理
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Configuration
@MapperScan("${mybatis-plus.mapperPackage}")    //包扫描
public class MybatisPlusConfig {
    /**
     * 配置主键id的自动生成策略
     * 使用网卡信息绑定雪花生成器，防止分布式雪花id重复
     */
    @Bean
    public IdentifierGenerator idGenerator() {
        return new DefaultIdentifierGenerator(NetUtil.getLocalhost());
    }

    /**
     * 元对象字段填充器
     * 在新增和修改时填充 更新者 和 更新时间
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new CreateAndUpdateMetaObjectHandler();
    }
}
