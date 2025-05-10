# 线程池监控Web应用模块

## 简介

这是线程池监控工具的Web应用模块，负责接收Agent模块上报的线程池数据，提供数据存储、查询和管理功能，以及可视化界面。该模块是一个独立的Spring Boot应用，可以与Agent模块分开部署。

## 功能特点

- 接收Agent上报的线程池数据
- 持久化存储线程池信息
- 提供REST API接口查询线程池状态
- 支持修改线程池参数
- 提供Web界面可视化展示线程池运行情况
- 支持多应用、多实例线程池监控

## 使用方法

### 构建

```bash
mvn clean package
```

构建完成后，在target目录下会生成`threadpool-tool-web-1.0.0.jar`文件。

### 启动应用

```bash
java -jar threadpool-tool-web-1.0.0.jar
```

默认情况下，应用将在8080端口启动。可以通过以下方式修改配置：

```bash
java -jar threadpool-tool-web-1.0.0.jar --server.port=8081
```

### 访问界面

启动成功后，可以通过浏览器访问：

```
http://localhost:8080/
```

### H2数据库控制台

应用内置了H2数据库，可以通过以下地址访问数据库控制台：

```
http://localhost:8080/h2-console
```

连接信息：
- JDBC URL: `jdbc:h2:file:./threadpool-db`
- 用户名: `sa`
- 密码: (空)

## API接口

### 获取所有线程池信息

```
GET /api/threadpool/list
```

### 获取指定线程池信息

```
GET /api/threadpool/{threadPoolId}
```

### 修改线程池参数

```
PUT /api/threadpool/{threadPoolId}
```

请求体示例：

```json
{
  "corePoolSize": 10,
  "maximumPoolSize": 20,
  "keepAliveTime": 60000
}
```

### 接收Agent上报数据

```
POST /api/threadpool/upload
```

## 与Agent模块通信

Web应用模块通过HTTP接口接收Agent模块上报的数据。Agent会定期将收集到的线程池信息通过POST请求发送到Web应用的`/api/threadpool/upload`接口。

数据格式为JSON数组，每个元素包含一个线程池的完整信息。Web应用接收到数据后，会更新数据库中的线程池信息。

## 部署建议

- 建议将Web应用部署在固定IP的服务器上，方便Agent模块连接
- 可以配置反向代理（如Nginx）提供更好的安全性和负载均衡
- 生产环境建议使用外部数据库（如MySQL、PostgreSQL）替代内置的H2数据库
- 可以使用Docker容器化部署，简化环境配置

## 配置说明

主要配置项在`application.properties`文件中：

- `server.port`: 应用端口，默认8080
- `spring.datasource.*`: 数据库连接配置
- `spring.jpa.hibernate.ddl-auto`: 数据库表结构更新策略，默认update
- `logging.level.*`: 日志级别配置

## 注意事项

- 确保Agent模块配置了正确的Web应用URL地址
- 如果修改了应用端口，需要同步更新Agent的上报地址
- 数据库文件默认保存在应用运行目录下，注意备份