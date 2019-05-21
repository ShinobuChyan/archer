package com.archer.server.core.model;

import java.util.LinkedHashMap;

/**
 * @author Shinobu
 * @since 2019/1/30
 */
public class BasicAppStatusInfo {

    /**
     * 当前实例ID
     */
    private String appId;
    /**
     * 实例是否运行中
     */
    private Boolean running;

    /**
     * 重发任务执行器 - 是否运行中
     */
    private Boolean scheduledExecutorRunning;
    /**
     * 重发任务执行器 - 待执行任务数
     */
    private long scheduledExecutorTaskCount;

    /**
     * 数据库更新任务执行器 - 是否运行中
     */
    private Boolean updateExecutorRunning;
    /**
     * 数据库更新任务执行器 - 待执行任务数
     */
    private long updateExecutorTaskCount;

    /**
     * 处理中消息数量
     */
    private int processingMessageCount;

    /**
     * 消息生命周期信息发送器 - 待执行任务数
     */
    private long lifecycleExecutorTaskCount;

    /**
     * 计数信息 - 消息入界数量
     */
    private LinkedHashMap<String, Integer> inboundCount;
    /**
     * 计数信息 - http请求发送数量
     */
    private LinkedHashMap<String, Integer> sendCount;

    public String getAppId() {
        return appId;
    }

    public BasicAppStatusInfo setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public Boolean getRunning() {
        return running;
    }

    public BasicAppStatusInfo setRunning(Boolean running) {
        this.running = running;
        return this;
    }

    public Boolean getScheduledExecutorRunning() {
        return scheduledExecutorRunning;
    }

    public BasicAppStatusInfo setScheduledExecutorRunning(Boolean scheduledExecutorRunning) {
        this.scheduledExecutorRunning = scheduledExecutorRunning;
        return this;
    }

    public long getScheduledExecutorTaskCount() {
        return scheduledExecutorTaskCount;
    }

    public BasicAppStatusInfo setScheduledExecutorTaskCount(long scheduledExecutorTaskCount) {
        this.scheduledExecutorTaskCount = scheduledExecutorTaskCount;
        return this;
    }

    public Boolean getUpdateExecutorRunning() {
        return updateExecutorRunning;
    }

    public BasicAppStatusInfo setUpdateExecutorRunning(Boolean updateExecutorRunning) {
        this.updateExecutorRunning = updateExecutorRunning;
        return this;
    }

    public long getUpdateExecutorTaskCount() {
        return updateExecutorTaskCount;
    }

    public BasicAppStatusInfo setUpdateExecutorTaskCount(long updateExecutorTaskCount) {
        this.updateExecutorTaskCount = updateExecutorTaskCount;
        return this;
    }

    public int getProcessingMessageCount() {
        return processingMessageCount;
    }

    public BasicAppStatusInfo setProcessingMessageCount(int processingMessageCount) {
        this.processingMessageCount = processingMessageCount;
        return this;
    }

    public long getLifecycleExecutorTaskCount() {
        return lifecycleExecutorTaskCount;
    }

    public BasicAppStatusInfo setLifecycleExecutorTaskCount(long lifecycleExecutorTaskCount) {
        this.lifecycleExecutorTaskCount = lifecycleExecutorTaskCount;
        return this;
    }

    public LinkedHashMap<String, Integer> getInboundCount() {
        return inboundCount;
    }

    public BasicAppStatusInfo setInboundCount(LinkedHashMap<String, Integer> inboundCount) {
        this.inboundCount = inboundCount;
        return this;
    }

    public LinkedHashMap<String, Integer> getSendCount() {
        return sendCount;
    }

    public BasicAppStatusInfo setSendCount(LinkedHashMap<String, Integer> sendCount) {
        this.sendCount = sendCount;
        return this;
    }
}
