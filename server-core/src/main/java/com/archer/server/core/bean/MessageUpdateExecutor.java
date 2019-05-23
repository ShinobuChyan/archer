package com.archer.server.core.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 数据库操作串行线程池
 *
 * @author Shinobu
 * @since 2019/1/18
 */
@Component
public class MessageUpdateExecutor {

    private final static Logger LOGGER = LoggerFactory.getLogger(MessageUpdateExecutor.class);

    /**
     * 最大任务等待队列长度
     */
    private static final int TASK_QUEUE_MAX_LENGTH = 1 << 22;

    private ThreadPoolExecutor updateExecutor = newExecutor();

    private static ThreadPoolExecutor newExecutor() {
        return new ThreadPoolExecutor(
                1,
                1,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(TASK_QUEUE_MAX_LENGTH),
                new CustomizableThreadFactory("UpdateExecutor"));
    }

    public void execute(Runnable command) {
        updateExecutor.execute(command);
    }

    public List<Runnable> shutdownNow() {
        synchronized (this) {
            var runnableList = updateExecutor.shutdownNow();
            LOGGER.info("MessageUpdateExecutor 已停止");
            return runnableList;
        }
    }

    public void restart() {
        synchronized (this) {
            if (updateExecutor.isShutdown()) {
                updateExecutor = newExecutor();
                LOGGER.info("MessageUpdateExecutor 已重新启动");
            }
        }
    }

    public boolean isRunning() {
        return !updateExecutor.isShutdown();
    }

    public long getTaskCount() {
        return updateExecutor.getTaskCount();
    }

}
