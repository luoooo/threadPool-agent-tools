#!/bin/bash

# 启动TestDemo应用并添加Java Agent参数
MVN_OPTS="-javaagent:/Users/aa/doc/workspace/threadpool-tool-agent/target/threadpool-tool-agent-1.0.0.jar -Dthread.pool.monitor.enabled=true"

echo "正在使用Java Agent启动TestDemo应用..."
echo "Java Agent路径: /Users/aa/doc/workspace/threadpool-tool-test/target/threadpool-tool-test-0.0.1-SNAPSHOT.jar"

# 使用Maven运行应用并添加Java Agent参数
cd "$(dirname "$0")"
mvn spring-boot:run -Dspring-boot.run.jvmArguments="${MVN_OPTS}"