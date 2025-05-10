package com.example.threadpool.web.controller;

import com.example.threadpool.web.model.ThreadPoolInfo;
import com.example.threadpool.web.repository.ThreadPoolRepository;
import com.example.threadpool.web.service.ThreadPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 线程池管理控制器，提供REST API接口
 */
@RestController
@RequestMapping("/api/threadpool")
public class ThreadPoolController {

    @Autowired
    private ThreadPoolService threadPoolService;
    
    /**
     * 获取所有线程池信息
     */
    @GetMapping("/list")
    public ResponseEntity<List<ThreadPoolInfo>> getAllThreadPools() {
        List<ThreadPoolInfo> threadPools = threadPoolService.getAllThreadPools();
        return ResponseEntity.ok(threadPools);
    }
    
    /**
     * 获取指定线程池信息
     */
    @GetMapping("/{threadPoolId}")
    public ResponseEntity<ThreadPoolInfo> getThreadPool(@PathVariable String threadPoolId) {
        return threadPoolService.getThreadPool(threadPoolId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 修改线程池参数
     * 此方法会同时更新数据库中的参数，并通过HTTP请求直接修改远程JVM中的线程池
     */
    @PutMapping("/{threadPoolId}")
    public ResponseEntity<String> updateThreadPool(
            @PathVariable String threadPoolId,
            @RequestBody ThreadPoolInfo threadPoolInfo) {
        boolean success = threadPoolService.updateThreadPool(threadPoolId, threadPoolInfo);
        if (success) {
            return ResponseEntity.ok("线程池参数修改成功，已同步到远程JVM");
        } else {
            return ResponseEntity.badRequest().body("线程池参数修改失败，可能找不到指定的线程池或远程JVM不可达");
        }
    }
    
    /**
     * 接收Agent上报的线程池数据
     */
    @PostMapping("/upload")
    public ResponseEntity<String> receiveThreadPoolData(@RequestBody List<ThreadPoolInfo> threadPools) {
        try {
            threadPoolService.updateThreadPools(threadPools);
            return ResponseEntity.ok("成功接收" + threadPools.size() + "个线程池数据");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("接收线程池数据失败: " + e.getMessage());
        }
    }
}