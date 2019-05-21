package com.archer.server.core.service.application;

/**
 * 应用实例相关服务
 *
 * @author Shinobu
 * @since 2019/1/17
 */
public interface ApplicationService {

    /**
     * 停止应用
     */
    void shutdown();

    /**
     * 重新启动
     */
    void restart();

    /**
     * 刷新redis中的应用基本信息
     */
    void refreshBasicAppStatusInfo();

}
