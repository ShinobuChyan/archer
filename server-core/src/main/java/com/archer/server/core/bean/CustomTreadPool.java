package com.archer.server.core.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.*;

/**
 * @author Shinobu
 * @since 2018/11/27
 */
@Configuration
public class CustomTreadPool {

    /**
     * 最大任务等待队列长度
     */
    private static final int TASK_QUEUE_MAX_LENGTH = 2 << 22;

    /**
     * 通用并行任务线程池
     */
    @Bean
    public ThreadPoolExecutor commonParallelExecutor() {
        return new ThreadPoolExecutor(
                8,
                1024,
                5L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(TASK_QUEUE_MAX_LENGTH),
                new CustomizableThreadFactory("commonParallelExecutor"));
    }

    /**
     * GrayLog串行线程池
     */
    @Bean
    public ThreadPoolExecutor grayLogExecutor() {
        return new ThreadPoolExecutor(
                1,
                1024,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(TASK_QUEUE_MAX_LENGTH),
                new CustomizableThreadFactory("GrayLogExecutor"));
    }

}
