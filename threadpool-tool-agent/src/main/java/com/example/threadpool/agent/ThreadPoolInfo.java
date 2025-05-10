package com.example.threadpool.agent;

import java.io.Serializable;

/**
 * 线程池信息模型类，用于存储线程池的各种参数
 */
public class ThreadPoolInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 线程池ID（唯一标识）
     */
    private String threadPoolId;
    
    /**
     * 线程池名称
     */
    private String threadPoolName;
    
    /**
     * 核心线程数
     */
    private int corePoolSize;
    
    /**
     * 最大线程数
     */
    private int maximumPoolSize;
    
    /**
     * 当前活跃线程数
     */
    private int activeThreads;
    
    /**
     * 队列大小
     */
    private int queueSize;
    
    /**
     * 队列剩余容量
     */
    private int queueRemainingCapacity;
    
    /**
     * 线程存活时间（毫秒）
     */
    private long keepAliveTime;
    
    /**
     * 任务总数
     */
    private long taskCount;
    
    /**
     * 已完成任务数
     */
    private long completedTaskCount;
    
    /**
     * 自定义任务计数
     */
    private long customTaskCount;

    // Getters and Setters
    public String getThreadPoolId() {
        return threadPoolId;
    }

    public void setThreadPoolId(String threadPoolId) {
        this.threadPoolId = threadPoolId;
    }

    public String getThreadPoolName() {
        return threadPoolName;
    }

    public void setThreadPoolName(String threadPoolName) {
        this.threadPoolName = threadPoolName;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getActiveThreads() {
        return activeThreads;
    }

    public void setActiveThreads(int activeThreads) {
        this.activeThreads = activeThreads;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getQueueRemainingCapacity() {
        return queueRemainingCapacity;
    }

    public void setQueueRemainingCapacity(int queueRemainingCapacity) {
        this.queueRemainingCapacity = queueRemainingCapacity;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(long taskCount) {
        this.taskCount = taskCount;
    }

    public long getCompletedTaskCount() {
        return completedTaskCount;
    }

    public void setCompletedTaskCount(long completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
    }

    public long getCustomTaskCount() {
        return customTaskCount;
    }

    public void setCustomTaskCount(long customTaskCount) {
        this.customTaskCount = customTaskCount;
    }
}