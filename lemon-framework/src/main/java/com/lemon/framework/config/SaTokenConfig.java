package com.lemon.framework.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.lemon.common.utils.spring.SpringUtils;
import com.lemon.framework.config.properties.SecurityProperties;
import com.lemon.framework.handler.AllUrlHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class SaTokenConfig implements WebMvcConfigurer {
    //自定义排除路径
    private final SecurityProperties securityProperties;

    /**
     * 注册 sa-token 拦截器
     * 只要注册就会默认开启注解拦截
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册路由拦截器，自定义校验规则
        registry.addInterceptor(new SaInterceptor(handler -> {
            //从Spring容器中获取AllUrlHandler，获取服务中所有控制器定义的请求路径
            AllUrlHandler urlHandler = SpringUtils.getBean(AllUrlHandler.class);
            //登录验证，排除路径
            SaRouter
                // 获取所有路径
                .match(urlHandler.getUrls())
                // 对未排除的路径进行检查
                .check(() -> {
                    // 检查是否登录 是否有token
                    StpUtil.checkLogin();

                    // 有效率影响 用于临时测试
                    // if (log.isDebugEnabled()) {
                    //     log.info("剩余有效时间: {}", StpUtil.getTokenTimeout());
                    //     log.info("临时有效时间: {}", StpUtil.getTokenActiveTimeout());
                    // }
                });
        })).addPathPatterns("/**")
            // 排除不需要拦截的路径
            .excludePathPatterns(securityProperties.getExcludes());
    }

    /**
     * Sa-Token 整合 jwt (简单模式)
     * 生成JWT风格token
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }
}
