package com.example.testdemo.controller;

import lombok.extern.slf4j.Slf4j;
import com.example.testdemo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 线程池测试接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/threadpool")
public class ThreadPoolController {

    private final TaskService taskService;
    private final ExecutorService fixedThreadPool;
    private final ExecutorService cachedThreadPool;
    private final ScheduledExecutorService scheduledThreadPool;

    @Autowired
    public ThreadPoolController(TaskService taskService,
                                ExecutorService fixedThreadPool,
                                ExecutorService cachedThreadPool,
                                ScheduledExecutorService scheduledThreadPool) {
        this.taskService = taskService;
        this.fixedThreadPool = fixedThreadPool;
        this.cachedThreadPool = cachedThreadPool;
        this.scheduledThreadPool = scheduledThreadPool;
    }

    /**
     * 提交任务到固定线程池
     */
    @PostMapping("/fixed/submit")
    public String submitFixed(@RequestParam(defaultValue = "5") int count) {
        taskService.executeTasksInFixedThreadPool(count);
        return "已提交" + count + "个任务到固定线程池";
    }

    /**
     * 提交任务到可缓存线程池
     */
    @PostMapping("/cached/submit")
    public String submitCached(@RequestParam(defaultValue = "5") int count) {
        taskService.executeTasksInCachedThreadPool(count);
        return "已提交" + count + "个任务到可缓存线程池";
    }

    /**
     * 提交任务到定时线程池
     */
    @PostMapping("/scheduled/submit")
    public String submitScheduled(@RequestParam(defaultValue = "5") int count) {
        taskService.executeTasksInScheduledThreadPool(count);
        return "已提交" + count + "个任务到定时线程池";
    }

    /**
     * 查询线程池状态
     */
    @GetMapping("/status")
    public Map<String, Object> getThreadPoolStatus() {
        Map<String, Object> status = new HashMap<>();
        if (fixedThreadPool instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor exec = (ThreadPoolExecutor) fixedThreadPool;
            status.put("fixed_active_count", exec.getActiveCount());
            status.put("fixed_pool_size", exec.getPoolSize());
            status.put("fixed_queue_size", exec.getQueue().size());
            status.put("fixed_core_pool_size", exec.getCorePoolSize());
            status.put("fixed_max_pool_size", exec.getMaximumPoolSize());
        }
        if (cachedThreadPool instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor exec = (ThreadPoolExecutor) cachedThreadPool;
            status.put("cached_active_count", exec.getActiveCount());
            status.put("cached_pool_size", exec.getPoolSize());
            status.put("cached_queue_size", exec.getQueue().size());
            status.put("cached_core_pool_size", exec.getCorePoolSize());
            status.put("cached_max_pool_size", exec.getMaximumPoolSize());
        }
        if (scheduledThreadPool instanceof ScheduledThreadPoolExecutor) {
            ScheduledThreadPoolExecutor exec = (ScheduledThreadPoolExecutor) scheduledThreadPool;
            status.put("scheduled_active_count", exec.getActiveCount());
            status.put("scheduled_pool_size", exec.getPoolSize());
            status.put("scheduled_queue_size", exec.getQueue().size());
            status.put("scheduledcore_pool_size", exec.getCorePoolSize());
            status.put("scheduled_max_pool_size", exec.getMaximumPoolSize());
        }
        return status;
    }
}