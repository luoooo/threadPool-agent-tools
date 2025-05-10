package com.example.threadpool.agent;

import javassist.*;
import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class SpringBeanTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader,
                          String className,
                          Class<?> classBeingRedefined,
                          ProtectionDomain protectionDomain,
                          byte[] classfileBuffer) {
        
        // 1. 只处理 Spring 的 Bean 初始化类
        if (!"org/springframework/beans/factory/support/AbstractAutowireCapableBeanFactory"
                .equals(className)) {
            return null;
        }

        try {
            // 2. 配置 ClassPool（关键：添加应用类加载器和 Spring 包路径）
            ClassPool pool = ClassPool.getDefault();
            pool.insertClassPath(new LoaderClassPath(loader)); // 添加应用类路径
            pool.importPackage("org.springframework.beans.factory.support"); // 显式导入包

            // 3. 增强 initializeBean 方法
            CtClass ctClass = pool.makeClass(new ByteArrayInputStream(classfileBuffer));
            enhanceInitializeBeanMethod(pool, ctClass);
            
            return ctClass.toBytecode();
        } catch (Exception e) {
            System.err.println("[ThreadPoolAgent] 增强失败: " + className + ", 错误: " + e);
            return null;
        }
    }

    private void enhanceInitializeBeanMethod(ClassPool pool, CtClass ctClass) 
            throws NotFoundException, CannotCompileException {
        
        // 获取 initializeBean(String beanName, Object bean, RootBeanDefinition mbd)
        CtMethod method = ctClass.getDeclaredMethod(
            "initializeBean", 
            new CtClass[] {
                pool.get("java.lang.String"),
                pool.get("java.lang.Object"),
                pool.get("org.springframework.beans.factory.support.RootBeanDefinition")
            }
        );

        // 注入线程池注册逻辑，$1/2/3占位符，对应上面的三个参数
        method.insertAfter(
            "if ($2 != null && $2 instanceof java.util.concurrent.ThreadPoolExecutor) {" +
            "   com.example.threadpool.agent.ThreadPoolMonitor.registerThreadPool(" +
            "       (java.util.concurrent.ThreadPoolExecutor) $2);" +
            "   System.out.println(\"[Agent] 监控到线程池Bean: \" + $1 + \", 实例: \" + $2);" +
            "}"
        );
    }
}