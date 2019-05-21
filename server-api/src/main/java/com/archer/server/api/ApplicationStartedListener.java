package com.archer.server.api;

import com.archer.server.core.bean.AppInfo;
import com.archer.server.core.depot.MessageDepot;
import com.archer.server.core.service.cluster.ClusterService;
import com.archer.server.common.util.GrayLogUtil;
import com.archer.server.core.properties.GrayLogProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;

/**
 * 应用初始化触发器
 *
 * @author Shinobu
 * @since 2018/10/24
 */
@Component
public class ApplicationStartedListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationStartedListener.class);

    @Value("${spring.profiles.active}")
    private String env;

    @Resource
    private GrayLogProperties grayLogProperties;

    @Resource
    private ExecutorService grayLogExecutor;

    @Resource
    private ClusterService clusterService;

    @Resource
    private AppInfo appInfo;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        clusterService.register();
        LOGGER.info("实例注册成功，appId: " + appInfo.getAppId());
        GrayLogUtil.init(grayLogProperties.getHost(), grayLogProperties.getPort(), env, grayLogExecutor);
        LOGGER.info("GrayLogUtil初始化成功");
        MessageDepot.IdIndex.init(stringRedisTemplate, appInfo.getAppId());
        LOGGER.info("MessageDepot.IdIndex初始化成功");
        appInfo.setRunning(true);
        LOGGER.info("application started.");

    }
}
