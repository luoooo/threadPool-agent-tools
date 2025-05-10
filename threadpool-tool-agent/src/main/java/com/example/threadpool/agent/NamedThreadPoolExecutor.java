package com.example.threadpool.agent;

import java.util.concurrent.*;


public class NamedThreadPoolExecutor extends ThreadPoolExecutor {
    private final String poolName;

    public NamedThreadPoolExecutor(String poolName, int corePoolSize, int maximumPoolSize, 
                                 long keepAliveTime, TimeUnit unit,
                                 BlockingQueue<Runnable> workQueue,
                                 ThreadFactory threadFactory,
                                 RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.poolName = poolName;
    }

    // 提供名称访问方法
    public String getPoolName() {
        return poolName;
    }
}
