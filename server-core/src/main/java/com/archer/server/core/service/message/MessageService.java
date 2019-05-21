package com.archer.server.core.service.message;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 消息服务
 *
 * @author Shinobu
 * @since 2018/11/28
 */
public interface MessageService {

    /**
     * 生成消息
     *
     * @param refId           关联业务ID
     * @param extraInfo       其他关联业务信息
     * @param source          消息来源
     * @param topic           topic
     * @param tag             tag
     * @param url             消息发送地址
     * @param method          请求方法：GET，POST
     * @param contentType     请求体类型：1 JSON形式，2 表单形式
     * @param headers         自定义请求头
     * @param content         自定义请求体
     * @param intervalList    重发间隔列表
     * @param firstTime       首次发送时间
     * @param stoppedKeyWords 停止发送关键词
     * @return id
     */
    @NotNull String insert(@NotNull String refId, @NotNull String extraInfo, @NotNull String source,
                           @NotNull String topic, @NotNull String tag,
                           @NotNull String url, @NotNull String method, @NotNull String contentType,
                           String headers, String content,
                           @NotNull List<Integer> intervalList, @NotNull Date firstTime, String stoppedKeyWords);

    /**
     * 处理消息收到正确响应的情况
     *
     * @param id 消息ID
     */
    void changeStatusToResponded(@NotNull String id);

    /**
     * 处理消息被外部主动停止的情况
     *
     * @param id 消息ID
     */
    void changeStatusToStopped(@NotNull String id);

}
