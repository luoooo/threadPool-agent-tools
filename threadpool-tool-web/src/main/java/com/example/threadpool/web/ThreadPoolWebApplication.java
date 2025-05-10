package com.example.threadpool.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * 线程池监控Web应用入口类
 */
@SpringBootApplication
public class ThreadPoolWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThreadPoolWebApplication.class, args);
    }
    
    /**
     * 配置RestTemplate Bean，用于发送HTTP请求
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}