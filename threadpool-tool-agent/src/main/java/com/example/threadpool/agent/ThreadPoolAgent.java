package com.example.threadpool.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Java Agent 入口类，用于监控线程池并收集数据
 */
public class ThreadPoolAgent {

    private static Instrumentation instrumentation;
    private static final String DEFAULT_WEB_URL = "http://localhost:8080/api/threadpool/upload";
    private static String webAppUrl;
    private static int reportIntervalSeconds = 10;
    private static ScheduledExecutorService scheduler;
    private static final int DEFAULT_HTTP_PORT = 9999;
    private static int httpPort = DEFAULT_HTTP_PORT;
    private static com.sun.net.httpserver.HttpServer httpServer;
    /**
     * JVM 启动时调用的 premain 方法
     */
    public static void premain(String args, Instrumentation inst) {
        System.out.println("ThreadPool Agent 已启动，开始监控线程池");
        parseArgs(args);
        instrumentation = inst;
        initAgent(inst, "Launch-Time");
        startReportScheduler();
        startHttpServer();

    }
    
    /**
     * JVM 运行时动态加载 Agent 时调用的 agentmain 方法
     */
    public static void agentmain(String args, Instrumentation inst) {
        System.out.println("ThreadPool Agent 已动态加载，开始监控线程池");
        parseArgs(args);
        instrumentation = inst;
        initAgent(inst, "Dynamic-Attach");
        startReportScheduler();
        startHttpServer();
    }

    private static void initAgent(Instrumentation inst, String mode) {
        System.out.printf("[ThreadPoolAgent] %s 初始化%n", mode);
        
        // 添加 ClassFileTransformer
        inst.addTransformer(new SpringBeanTransformer(), true);
        
        // 重转换已加载的类（对运行时附加的场景关键）
        try {
            Class<?>[] loadedClasses = inst.getAllLoadedClasses();
            for (Class<?> clazz : loadedClasses) {
                if (clazz.getName().equals("org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory")) {
                    inst.retransformClasses(clazz);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[ThreadPoolAgent] 重转换失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析Agent参数
     * 格式: url=http://localhost:8080/api/threadpool/upload,interval=10,port=9999
     */
    private static void parseArgs(String args) {
        if (args != null && !args.trim().isEmpty()) {
            String[] params = args.split(",");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    
                    if ("url".equals(key)) {
                        webAppUrl = value;
                    } else if ("interval".equals(key)) {
                        try {
                            reportIntervalSeconds = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid interval value: " + value + ", using default: " + reportIntervalSeconds);
                        }
                    } else if ("port".equals(key)) {
                        try {
                            httpPort = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid port value: " + value + ", using default: " + DEFAULT_HTTP_PORT);
                        }
                    }
                }
            }
        }
        
        // 使用默认值（如果未指定）
        if (webAppUrl == null || webAppUrl.trim().isEmpty()) {
            webAppUrl = DEFAULT_WEB_URL;
        }
        
        System.out.println("ThreadPool Agent 配置: Web应用URL=" + webAppUrl + ", 上报间隔=" + reportIntervalSeconds + "秒, HTTP端口=" + httpPort);
    }
    
    
    /**
     * 启动定时上报任务
     */
    private static void startReportScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ThreadPool-Monitor");
            t.setDaemon(true);
            return t;
        });
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                ThreadPoolMonitor.reportAllThreadPools(webAppUrl);
            } catch (Exception e) {
                System.err.println("上报线程池数据失败: " + e.getMessage());
            }
        }, reportIntervalSeconds, reportIntervalSeconds, TimeUnit.SECONDS);
        
        System.out.println("ThreadPool Agent 已启动定时上报任务，间隔" + reportIntervalSeconds + "秒");
    }

    
    
    /**
     * 获取 Instrumentation 实例
     */
    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }
    
    /**
     * 启动HTTP服务器，用于接收线程池参数修改请求
     */
    private static void startHttpServer() {
        try {
            httpServer = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(httpPort), 0);
            httpServer.createContext("/api/threadpool/modify", new ThreadPoolModifyHandler());
            httpServer.setExecutor(Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "ThreadPool-HTTP-Server");
                t.setDaemon(true);
                return t;
            }));
            httpServer.start();
            System.out.println("ThreadPool Agent HTTP服务器已启动，监听端口: " + httpPort);
        } catch (Exception e) {
            System.err.println("启动HTTP服务器失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理线程池参数修改请求的Handler
     */
    static class ThreadPoolModifyHandler implements com.sun.net.httpserver.HttpHandler {
        private final ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        
        @Override
        public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            try {
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendResponse(exchange, 405, "Method Not Allowed");
                    return;
                }
                
                // 解析请求体
                java.io.InputStream is = exchange.getRequestBody();
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int nRead;
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] requestBody = buffer.toByteArray();
                is.close();
                
                if (requestBody.length == 0) {
                    sendResponse(exchange, 400, "Request body is empty");
                    return;
                }
                
                // 解析JSON请求
                ThreadPoolModifyRequest request = objectMapper.readValue(requestBody, ThreadPoolModifyRequest.class);
                
                // 查找并修改线程池
                boolean success = modifyThreadPool(request);
                
                if (success) {
                    sendResponse(exchange, 200, "Thread pool parameters modified successfully");
                } else {
                    sendResponse(exchange, 404, "Thread pool not found");
                }
                
            } catch (Exception e) {
                System.err.println("处理线程池修改请求失败: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }
        
        private void sendResponse(com.sun.net.httpserver.HttpExchange exchange, int statusCode, String message) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = objectMapper.writeValueAsBytes(new ThreadPoolModifyResponse(statusCode, message));
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            java.io.OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
        
        private boolean modifyThreadPool(ThreadPoolModifyRequest request) {
            // 从ThreadPoolMonitor获取线程池
            ThreadPoolExecutor threadPool = ThreadPoolMonitor.getThreadPoolById(request.getThreadPoolId());
            
            if (threadPool == null) {
                return false;
            }
            
            // 修改线程池参数
            if (request.getCorePoolSize() > 0) {
                threadPool.setCorePoolSize(request.getCorePoolSize());
            }
            
            if (request.getMaximumPoolSize() > 0) {
                threadPool.setMaximumPoolSize(request.getMaximumPoolSize());
            }
            
            if (request.getKeepAliveTime() > 0) {
                threadPool.setKeepAliveTime(request.getKeepAliveTime(), TimeUnit.MILLISECONDS);
            }
            
            System.out.println("ThreadPool Agent: 已修改线程池参数，ID=" + request.getThreadPoolId() + 
                    ", 核心线程数=" + threadPool.getCorePoolSize() + 
                    ", 最大线程数=" + threadPool.getMaximumPoolSize() + 
                    ", 保持活跃时间=" + threadPool.getKeepAliveTime(TimeUnit.MILLISECONDS) + "ms");
            
            return true;
        }
    }
    
    /**
     * 线程池修改请求
     */
    static class ThreadPoolModifyRequest {
        private String threadPoolId;
        private int corePoolSize;
        private int maximumPoolSize;
        private long keepAliveTime;
        
        public String getThreadPoolId() { return threadPoolId; }
        public void setThreadPoolId(String threadPoolId) { this.threadPoolId = threadPoolId; }
        
        public int getCorePoolSize() { return corePoolSize; }
        public void setCorePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; }
        
        public int getMaximumPoolSize() { return maximumPoolSize; }
        public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }
        
        public long getKeepAliveTime() { return keepAliveTime; }
        public void setKeepAliveTime(long keepAliveTime) { this.keepAliveTime = keepAliveTime; }
    }
    
    /**
     * 线程池修改响应
     */
    static class ThreadPoolModifyResponse {
        private int code;
        private String message;
        
        public ThreadPoolModifyResponse(int code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}