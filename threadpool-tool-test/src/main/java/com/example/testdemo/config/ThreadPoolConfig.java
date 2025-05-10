package com.example.testdemo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池配置类
 * 创建多个不同配置的线程池，用于测试ThreadPoolToolAgent的监控和修改功能
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    /**
     * 创建一个固定大小的线程池
     */
    @Bean(name = "fixedThreadPool")
    public ExecutorService fixedThreadPool() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5, // 核心线程数
                10, // 最大线程数
                60L, // 空闲线程存活时间
                TimeUnit.SECONDS, // 时间单位
                new LinkedBlockingQueue<>(100), // 工作队列
                Executors.defaultThreadFactory(), // 线程工厂
                new ThreadPoolExecutor.AbortPolicy() // 拒绝策略
        );
        log.info("创建固定大小线程池: corePoolSize={}, maximumPoolSize={}",
                executor.getCorePoolSize(), executor.getMaximumPoolSize());
        return executor;
    }

    /**
     * 创建一个可缓存的线程池
     */
    // @Bean(name = "cachedThreadPool")
    // public ExecutorService cachedThreadPool() {
    // ThreadPoolExecutor executor = new ThreadPoolExecutor(
    // 0, // 核心线程数
    // 20, // 最大线程数
    // 30L, // 空闲线程存活时间
    // TimeUnit.SECONDS, // 时间单位
    // new SynchronousQueue<>(), // 工作队列
    // Executors.defaultThreadFactory(), // 线程工厂
    // new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
    // );
    // log.info("创建可缓存线程池: corePoolSize={}, maximumPoolSize={}",
    // executor.getCorePoolSize(), executor.getMaximumPoolSize());
    // return executor;
    // }

    @Bean(name = "cachedThreadPool")
    public ExecutorService cachedThreadPool() {
        return new NamedThreadPoolExecutor(
                "cachedThreadPool", // 显式传递名称
                5, 20, 30L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 创建一个定时任务线程池
     */
    @Bean(name = "scheduledThreadPool")
    public ScheduledExecutorService scheduledThreadPool() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                3, // 核心线程数
                Executors.defaultThreadFactory(), // 线程工厂
                new ThreadPoolExecutor.DiscardPolicy() // 拒绝策略
        );
        executor.setMaximumPoolSize(5); // 设置最大线程数
        executor.setKeepAliveTime(120L, TimeUnit.SECONDS); // 设置空闲线程存活时间
        log.info("创建定时任务线程池: corePoolSize={}, maximumPoolSize={}",
                executor.getCorePoolSize(), executor.getMaximumPoolSize());
        return executor;
    }

    class NamedThreadFactory implements ThreadFactory {
        private final String poolName;
        private final AtomicInteger counter = new AtomicInteger(1);

        NamedThreadFactory(String poolName) {
            this.poolName = poolName;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, poolName + "-thread-" + counter.getAndIncrement());
        }
    }
}
