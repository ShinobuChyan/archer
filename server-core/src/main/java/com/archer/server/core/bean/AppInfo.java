package com.archer.server.core.bean;

import org.springframework.stereotype.Component;

/**
 * @author Shinobu
 * @since 2018/9/28
 */
@Component
public class AppInfo {

    private volatile String appId;

    private volatile boolean isRunning = false;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
    }
}
