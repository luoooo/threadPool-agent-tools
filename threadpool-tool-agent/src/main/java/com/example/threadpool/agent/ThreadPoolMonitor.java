package com.example.threadpool.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池监控类，负责收集线程池数据并上报
 */
public class ThreadPoolMonitor {

    /**
     * 全局线程池注册表，用于存储所有被创建的线程池实例
     * 键为线程池的hashCode，值为线程池实例
     */
    private static final Map<String, ThreadPoolExecutor> THREAD_POOL_REGISTRY = new ConcurrentHashMap<>();

    /**
     * 根据线程池ID获取线程池实例
     * 
     * @param threadPoolId 线程池ID
     * @return 线程池实例，如果不存在则返回null
     */
    public static ThreadPoolExecutor getThreadPoolById(String threadPoolId) {
        return THREAD_POOL_REGISTRY.get(threadPoolId);
    }

    /**
     * 任务执行计数器
     */
    private static final Map<String, Long> TASK_COUNT_MAP = new ConcurrentHashMap<>();

    /**
     * 注册线程池实例
     */
    public static void registerThreadPool(ThreadPoolExecutor threadPool) {
        if (threadPool != null) {
            String poolId = String.valueOf(threadPool.hashCode());
            THREAD_POOL_REGISTRY.put(poolId, threadPool);
            System.out.println("ThreadPoolTool: 注册线程池，ID=" + poolId + ", 当前注册池数量: " + THREAD_POOL_REGISTRY.size());
        }
    }

   
    /**
     * 上报所有线程池数据到Web应用
     */
    public static void reportAllThreadPools(String webAppUrl) {
        try {
            List<ThreadPoolInfo> threadPools = getAllThreadPools();
            if (threadPools.isEmpty()) {
                System.out.println("ThreadPoolTool: 没有找到线程池，跳过上报");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(threadPools);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(webAppUrl);
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setEntity(new StringEntity(jsonData));

                httpClient.execute(httpPost);
                System.out.println("ThreadPoolTool: 成功上报" + threadPools.size() + "个线程池数据");
            }
        } catch (Exception e) {
            System.err.println("ThreadPoolTool: 上报线程池数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取所有线程池信息
     */
    private static List<ThreadPoolInfo> getAllThreadPools() {
        List<ThreadPoolInfo> threadPools = new ArrayList<>();

        try {
            // 从注册表中获取线程池
            for (Map.Entry<String, ThreadPoolExecutor> entry : THREAD_POOL_REGISTRY.entrySet()) {
                String poolId = entry.getKey();
                ThreadPoolExecutor threadPool = entry.getValue();

                if (threadPool != null) {
                    ThreadPoolInfo info = createThreadPoolInfo(poolId, threadPool);
                    threadPools.add(info);
                }
            }

            System.out.println("ThreadPoolTool: 获取到" + threadPools.size() + "个线程池信息");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return threadPools;
    }

    /**
     * 创建线程池信息对象
     */
    private static ThreadPoolInfo createThreadPoolInfo(String poolId, ThreadPoolExecutor threadPool) {
        ThreadPoolInfo info = new ThreadPoolInfo();
        info.setThreadPoolId(poolId);
        info.setThreadPoolName(threadPool.getClass().getName() + "@" + poolId);
        if (threadPool instanceof NamedThreadPoolExecutor) {
            System.err.println("[WARN] instanceof 判断对应类型: ");
            String name = ((NamedThreadPoolExecutor) threadPool).getPoolName();
            info.setThreadPoolName(name + "@" + poolId);
        }
        if (threadPool.getClass().getName().contains("NamedThreadPoolExecutor")) {
            try {
                Class<?> clazz = threadPool.getClass();
                Field field = clazz.getDeclaredField("poolName");
                field.setAccessible(true);
                String name = (String) field.get(threadPool);
                info.setThreadPoolName(name + "@" + poolId);
            } catch (Exception e) {
                System.err.println("[WARN] 无法通过反射获取线程池名称: " + e.getMessage());
            }
        }

        info.setCorePoolSize(threadPool.getCorePoolSize());
        info.setMaximumPoolSize(threadPool.getMaximumPoolSize());
        info.setActiveThreads(threadPool.getActiveCount());

        BlockingQueue<Runnable> queue = threadPool.getQueue();
        info.setQueueSize(queue.size());
        info.setQueueRemainingCapacity(queue.remainingCapacity());
        info.setKeepAliveTime(threadPool.getKeepAliveTime(java.util.concurrent.TimeUnit.MILLISECONDS));
        info.setTaskCount(threadPool.getTaskCount());
        info.setCompletedTaskCount(threadPool.getCompletedTaskCount());

        // 添加自定义计数器的数据
        Long customTaskCount = TASK_COUNT_MAP.getOrDefault(poolId, 0L);
        info.setCustomTaskCount(customTaskCount);

        return info;
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

        // 提供名称访问方法
        public String getPoolName() {
            return poolName;
        }
    }
}