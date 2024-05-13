package com.lemon.framework.config;

import cn.hutool.core.net.NetUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.lemon.framework.handler.CreateAndUpdateMetaObjectHandler;
import com.lemon.framework.interceptor.PlusDataPermissionInterceptor;
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
     * 插件配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 数据权限处理插件
        interceptor.addInnerInterceptor(dataPermissionInterceptor());
        // 分页插件 注意多个插件使用时，需要将分页插件放在最后边
        interceptor.addInnerInterceptor(paginationInnerInterceptor());

        return interceptor;
    }

    private PlusDataPermissionInterceptor dataPermissionInterceptor() {
        return new PlusDataPermissionInterceptor();
    }

    /**
     * 分页插件
     */
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInnerInterceptor.setMaxLimit(-1L);
        // 分页合理化 溢出总页数后会进行处理，即使用第 1 页
        paginationInnerInterceptor.setOverflow(true);
        return paginationInnerInterceptor;
    }

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
