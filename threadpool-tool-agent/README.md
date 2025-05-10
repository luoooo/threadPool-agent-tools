# 线程池监控Agent模块

## 简介

这是线程池监控工具的Agent模块，负责监控Java应用中的线程池并收集数据。该模块通过Java Agent技术，在不修改应用代码的情况下，动态监控线程池的创建和使用情况，并将数据上报到Web应用模块。

## 功能特点

- 自动监控应用中所有ThreadPoolExecutor实例
- 收集线程池核心参数（核心线程数、最大线程数等）
- 收集线程池运行时数据（活跃线程数、队列大小、任务数等）
- 定时将数据上报到Web应用模块
- 支持通过参数配置上报地址和上报间隔

## 使用方法

### 构建

```bash
mvn clean package
```

构建完成后，在target目录下会生成`threadpool-tool-agent-1.0.0.jar`文件。

### 启动方式

#### 方式一：JVM启动参数

```bash
java -javaagent:threadpool-tool-agent-1.0.0.jar=url=http://localhost:8080/api/threadpool/update,interval=10 -jar your-application.jar
```

参数说明：
- `url`: Web应用接收数据的URL地址，默认为`http://localhost:8080/api/threadpool/update`
- `interval`: 数据上报间隔（秒），默认为10秒

#### 方式二：动态加载

可以使用Java Attach API在运行时动态加载Agent：

```java
VirtualMachine vm = VirtualMachine.attach("pid");
vm.loadAgent("path/to/threadpool-tool-agent-1.0.0.jar", "url=http://localhost:8080/api/threadpool/update,interval=5");
vm.detach();
```

## 工作原理

1. Agent通过字节码增强技术，拦截ThreadPoolExecutor的构造方法和execute方法
2. 当线程池被创建时，自动注册到监控列表中
3. 定时收集所有注册线程池的运行数据
4. 通过HTTP请求将数据上报到Web应用模块

## 与Web应用通信

Agent模块通过HTTP POST请求，将收集到的线程池数据发送到Web应用的API接口。数据格式为JSON数组，每个元素包含一个线程池的完整信息。

示例报文：

```json
[
  {
    "threadPoolId": "123456789",
    "threadPoolName": "java.util.concurrent.ThreadPoolExecutor@123456789",
    "corePoolSize": 5,
    "maximumPoolSize": 10,
    "activeThreads": 3,
    "queueSize": 7,
    "queueRemainingCapacity": 93,
    "keepAliveTime": 60000,
    "taskCount": 100,
    "completedTaskCount": 90,
    "customTaskCount": 95
  }
]
```

## 注意事项

- 确保Web应用模块已启动并可访问
- 如果Web应用地址或端口有变化，需要在启动参数中指定正确的URL
- Agent会自动重试连接，但如果长时间无法连接，可能会导致数据丢失