package com.example.threadpool.web.service;

import com.example.threadpool.web.model.ThreadPoolInfo;
import com.example.threadpool.web.repository.ThreadPoolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 线程池管理服务，提供查询和修改线程池参数的功能
 */
@Service
public class ThreadPoolService {

    @Autowired
    private ThreadPoolRepository threadPoolRepository;
    
    /**
     * 获取所有线程池信息
     */
    public List<ThreadPoolInfo> getAllThreadPools() {
        return threadPoolRepository.findAll();
    }
    
    /**
     * 获取指定线程池信息
     */
    public Optional<ThreadPoolInfo> getThreadPool(String threadPoolId) {
        return threadPoolRepository.findById(threadPoolId);
    }
    
    /**
     * 更新线程池参数
     * 此方法会同时更新数据库中的参数，并通过HTTP请求直接修改远程JVM中的线程池
     */
    @Transactional
    public boolean updateThreadPool(String threadPoolId, ThreadPoolInfo threadPoolInfo) {
        Optional<ThreadPoolInfo> existingPool = threadPoolRepository.findById(threadPoolId);
        if (existingPool.isPresent()) {
            ThreadPoolInfo pool = existingPool.get();
            
            // 只更新可以修改的参数
            pool.setCorePoolSize(threadPoolInfo.getCorePoolSize());
            pool.setMaximumPoolSize(threadPoolInfo.getMaximumPoolSize());
            pool.setKeepAliveTime(threadPoolInfo.getKeepAliveTime());
            pool.setLastUpdateTime(System.currentTimeMillis());
            
            threadPoolRepository.save(pool);
            
            // 通过HTTP请求修改远程JVM中的线程池
            boolean remoteUpdateSuccess = updateRemoteThreadPool(threadPoolId, threadPoolInfo);
            
            return remoteUpdateSuccess;
        }
        return false;
    }
    
    /**
     * 通过HTTP请求修改远程JVM中的线程池参数
     */
    private boolean updateRemoteThreadPool(String threadPoolId, ThreadPoolInfo threadPoolInfo) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("threadPoolId", threadPoolId);
            requestBody.put("corePoolSize", threadPoolInfo.getCorePoolSize());
            requestBody.put("maximumPoolSize", threadPoolInfo.getMaximumPoolSize());
            requestBody.put("keepAliveTime", threadPoolInfo.getKeepAliveTime());
            
            // 获取主机信息
            Optional<ThreadPoolInfo> existingPool = threadPoolRepository.findById(threadPoolId);
            String hostName = existingPool.isPresent() ? existingPool.get().getHostName() : "";
            
            // 构建Agent HTTP服务器URL
            // 默认使用9999端口，可以根据实际情况配置
            hostName="localhost";
            String agentUrl = "http://" + hostName + ":9999/api/threadpool/modify";
            
            // 发送HTTP请求
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(agentUrl, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("成功修改远程线程池参数: " + threadPoolId);
                return true;
            } else {
                System.err.println("修改远程线程池参数失败: " + response.getBody());
                return false;
            }
        } catch (Exception e) {
            System.err.println("修改远程线程池参数异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 批量更新线程池数据
     * 用于接收Agent上报的数据
     */
    @Transactional
    public void updateThreadPools(List<ThreadPoolInfo> threadPools) {
        if (threadPools == null || threadPools.isEmpty()) {
            return;
        }
        
        // 设置更新时间和主机信息
        long currentTime = System.currentTimeMillis();
        String hostName = getHostName();
        
        for (ThreadPoolInfo threadPool : threadPools) {
            threadPool.setLastUpdateTime(currentTime);
            
            // 如果没有设置主机名，则设置为当前主机名
            if (threadPool.getHostName() == null || threadPool.getHostName().isEmpty()) {
                threadPool.setHostName(hostName);
            }
            
            // 保存或更新线程池数据
            Optional<ThreadPoolInfo> existingPool = threadPoolRepository.findById(threadPool.getThreadPoolId());
            if (existingPool.isPresent()) {
                ThreadPoolInfo pool = existingPool.get();
                
                // 更新动态变化的参数
                pool.setActiveThreads(threadPool.getActiveThreads());
                pool.setQueueSize(threadPool.getQueueSize());
                pool.setQueueRemainingCapacity(threadPool.getQueueRemainingCapacity());
                pool.setTaskCount(threadPool.getTaskCount());
                pool.setCompletedTaskCount(threadPool.getCompletedTaskCount());
                pool.setCustomTaskCount(threadPool.getCustomTaskCount());
                pool.setLastUpdateTime(currentTime);
                
                // 如果Agent端修改了核心参数，也需要更新
                if (pool.getCorePoolSize() != threadPool.getCorePoolSize()) {
                    pool.setCorePoolSize(threadPool.getCorePoolSize());
                }
                if (pool.getMaximumPoolSize() != threadPool.getMaximumPoolSize()) {
                    pool.setMaximumPoolSize(threadPool.getMaximumPoolSize());
                }
                if (pool.getKeepAliveTime() != threadPool.getKeepAliveTime()) {
                    pool.setKeepAliveTime(threadPool.getKeepAliveTime());
                }
                
                threadPoolRepository.save(pool);
            } else {
                // 新线程池，直接保存
                threadPoolRepository.save(threadPool);
            }
        }
    }
    
    /**
     * 获取当前主机名
     */
    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}