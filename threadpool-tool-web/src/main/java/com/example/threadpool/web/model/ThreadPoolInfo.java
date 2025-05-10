package com.example.threadpool.web.model;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import java.io.Serializable;

/**
 * 线程池信息模型类，用于存储线程池的各种参数
 */
@Data
@Entity
public class ThreadPoolInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 线程池ID（唯一标识）
     */
    @Id
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
    
    /**
     * 最后更新时间
     */
    private long lastUpdateTime = System.currentTimeMillis();
    
    /**
     * 应用名称
     */
    private String applicationName;
    
    /**
     * 主机名
     */
    private String hostName;
}