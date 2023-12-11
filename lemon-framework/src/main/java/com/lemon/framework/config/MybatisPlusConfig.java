package com.lemon.framework.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("${mybatis-plus.mapperPackage}")    //包扫描
public class MybatisPlusConfig {
}
