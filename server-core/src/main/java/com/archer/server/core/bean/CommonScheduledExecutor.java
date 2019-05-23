package com.archer.server.core.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 通用定时任务线程池
 *
 * @author Shinobu
 * @since 2019/1/18
 */
@Component
public class CommonScheduledExecutor {

    private final static Logger LOGGER = LoggerFactory.getLogger(CommonScheduledExecutor.class);

    private volatile ScheduledThreadPoolExecutor commonScheduledExecutor = newExecutor();

    private static ScheduledThreadPoolExecutor newExecutor() {
        return new ScheduledThreadPoolExecutor(
                1,
                new CustomizableThreadFactory("CommonScheduledExecutor"));
    }

    public void schedule(Runnable command, long delay, TimeUnit unit) {
        commonScheduledExecutor.schedule(command, delay, unit);
    }

    public List<Runnable> shutdownNow() {
        synchronized (this) {
            var runnableList = commonScheduledExecutor.shutdownNow();
            LOGGER.info("CommonScheduledExecutor 已停止");
            return runnableList;
        }
    }

    public void restart() {
        synchronized (this) {
            if (commonScheduledExecutor.isShutdown()) {
                commonScheduledExecutor = newExecutor();
                LOGGER.info("CommonScheduledExecutor 已重新启动");
            }
        }
    }

    public boolean isRunning() {
        return !commonScheduledExecutor.isShutdown();
    }

    public long getTaskCount() {
        return commonScheduledExecutor.getTaskCount();
    }

}
