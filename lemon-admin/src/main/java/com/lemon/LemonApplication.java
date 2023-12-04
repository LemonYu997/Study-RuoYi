package com.lemon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动程序
 */
@SpringBootApplication
public class LemonApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(LemonApplication.class);
        application.run(args);

        System.out.println("Lemon-admin启动成功！");
    }
}
