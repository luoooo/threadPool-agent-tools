package com.example.testdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 任务服务类
 * 用于测试不同线程池的执行情况
 */
@Slf4j
@Service
public class TaskService {

    private final ExecutorService fixedThreadPool;
    private final ExecutorService cachedThreadPool;
    private final ScheduledExecutorService scheduledThreadPool;

    public TaskService(
            @Qualifier("fixedThreadPool") ExecutorService fixedThreadPool,
            @Qualifier("cachedThreadPool") ExecutorService cachedThreadPool,
            @Qualifier("scheduledThreadPool") ScheduledExecutorService scheduledThreadPool) {
        this.fixedThreadPool = fixedThreadPool;
        this.cachedThreadPool = cachedThreadPool;
        this.scheduledThreadPool = scheduledThreadPool;
    }

    /**
     * 在固定大小线程池中执行任务
     * @param taskCount 任务数量
     */
    public void executeTasksInFixedThreadPool(int taskCount) {
        log.info("开始在固定大小线程池中执行{}个任务", taskCount);
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            fixedThreadPool.execute(() -> {
                log.info("固定线程池-任务{}开始执行", taskId);
                try {
                    // 模拟任务执行时间
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("固定线程池-任务{}执行完成", taskId);
            });
        }
    }

    /**
     * 在可缓存线程池中执行任务
     * @param taskCount 任务数量
     */
    public void executeTasksInCachedThreadPool(int taskCount) {
        log.info("开始在可缓存线程池中执行{}个任务", taskCount);
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            cachedThreadPool.execute(() -> {
                log.info("缓存线程池-任务{}开始执行", taskId);
                try {
                    // 模拟任务执行时间
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("缓存线程池-任务{}执行完成", taskId);
            });
        }
    }

    /**
     * 在定时任务线程池中执行任务
     * @param taskCount 任务数量
     */
    public void executeTasksInScheduledThreadPool(int taskCount) {
        log.info("开始在定时任务线程池中执行{}个任务", taskCount);
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            scheduledThreadPool.schedule(() -> {
                log.info("定时任务线程池-任务{}开始执行", taskId);
                try {
                    // 模拟任务执行时间
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("定时任务线程池-任务{}执行完成", taskId);
            }, i % 3, TimeUnit.SECONDS); // 延迟不同时间执行
        }
    }
}