package com.example.threadpool.web.repository;

import com.example.threadpool.web.model.ThreadPoolInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 线程池数据访问层，提供数据持久化功能
 */
@Repository
public interface ThreadPoolRepository extends JpaRepository<ThreadPoolInfo, String> {
    // 继承JpaRepository已经提供了基本的CRUD操作
}