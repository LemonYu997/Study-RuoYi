package com.lemon.framework.handler;

import cn.hutool.core.util.ReUtil;
import com.lemon.common.utils.spring.SpringUtils;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 获取所有Url配置
 * 继承InitializingBean，用来管理 Bean 的生命周期
 * 在 AllUrlHandler 这个 Bean 初始化后（因为@Component，在使用的时候会初始化），就会调用下边的 afterPropertiesSet 方法
 */
@Data
@Component
public class AllUrlHandler implements InitializingBean {
    //匹配 {xxx}，即请求路径参数
    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

    //收集所有使用@RequestMapping的控制器映射的地址，即后端所有可用请求路径
    private List<String> urls = new ArrayList<>();

    /**
     * Spring Bean 初始化后的处理方法
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Set<String> set = new HashSet<>();
        //搜集所有控制器（带 @RequestMapping 注解的）
        RequestMappingHandlerMapping mapping = SpringUtils.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        map.keySet().forEach(info -> {
            // 获取注解上边的 path 替代 path variable 为 *，方便去重
            Objects.requireNonNull(info.getPathPatternsCondition().getPatterns())
                .forEach(url -> set.add(ReUtil.replaceAll(url.getPatternString(), PATTERN, "*")));
        });
        urls.addAll(set);
    }
}
