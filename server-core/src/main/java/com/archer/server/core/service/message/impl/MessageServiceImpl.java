package com.archer.server.core.service.message.impl;

import com.archer.server.core.bean.AppInfo;
import com.archer.server.core.count.Counter;
import com.archer.server.core.dao.mapper.ArcherMessageMapper;
import com.archer.server.core.depot.MessageDepot;
import com.archer.server.core.model.ArcherMessage;
import com.archer.server.core.service.cluster.ClusterService;
import com.archer.server.core.service.message.MessageService;
import com.archer.server.common.constant.StatusConstants;
import com.archer.server.common.constant.ValueConstants;
import com.archer.server.common.exception.RestRuntimeException;
import com.archer.server.common.util.GrayLogUtil;
import com.archer.server.common.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Shinobu
 * @since 2018/11/28
 */
@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Resource
    private AppInfo appInfo;

    @Resource
    private MessageDepot messageDepot;

    @Resource
    private ArcherMessageMapper archerMessageMapper;

    @Resource
    private ClusterService clusterService;

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
    @Override
    public @NotNull String insert(@NotNull String refId, @NotNull String extraInfo, @NotNull String source,
                                  String topic, String tag,
                                  @NotNull String url, @NotNull String method, @NotNull String contentType,
                                  String headers, String content,
                                  @NotNull List<Integer> intervalList, @NotNull Date firstTime, String stoppedKeyWords) {
        if (archerMessageMapper.countByUniqueIndex(refId, extraInfo, source) > 0) {
            throw new RestRuntimeException("联合唯一主键重复");
        }

        var id = UUID.randomUUID().toString();
        var message = ArcherMessage.initMessage(id, refId, extraInfo, source, topic, tag,
                appInfo.isRunning() ? "1" : "-1", appInfo.isRunning() ? null : new Date(),
                url, method, contentType, headers, content, intervalList, firstTime,
                stoppedKeyWords == null ? ValueConstants.DEFAULT_STOPPED_KEY_WORDS : stoppedKeyWords);
        var entity = message.toEntity();
        int i = archerMessageMapper.insertSelective(entity);
        if (i != 1) {
            throw new RestRuntimeException("消息入库失败");
        }
        Counter.INBOUND_COUNTER.increase();
        clusterService.increaseInboundCount();
        GrayLogUtil.messageLifecycleLog(id, "saved.", TimeUtil.nowMillisStr(), message.toString());

        if (appInfo.isRunning()) {
            messageDepot.produce(message);
        }
        return message.getId();
    }

    /**
     * 处理消息收到正确响应的情况
     *
     * @param id 消息ID
     */
    @Override
    public void changeStatusToResponded(@NotNull String id) {
        handleRespondedOrStopped(id, StatusConstants.END_RESPONDED);
    }

    /**
     * 处理消息被外部主动停止的情况
     *
     * @param id 消息ID
     */
    @Override
    public void changeStatusToStopped(@NotNull String id) {
        handleRespondedOrStopped(id, StatusConstants.END_STOPPED);
    }

    private void handleRespondedOrStopped(@NotNull String id, @NotNull String status) {
        var s = archerMessageMapper.selectStatusByPrimaryKey(id);
        if (s == null || StatusConstants.END_STOPPED.equals(s) ||
                StatusConstants.END_COMPLETED.equals(s) || StatusConstants.END_RESPONDED.equals(s)) {
            LOGGER.info("消息（" + id + "）状态已不可被外部更改, 当前状态：" + s + "，目标状态：" + status);
            return;
        }

        int i = archerMessageMapper.updateStatusByPrimaryKey(id, status);
        if (i > 0) {
            GrayLogUtil.messageLifecycleLog(id, "status changed by outside, new status: " + status, TimeUtil.nowMillisStr(), null);
        }
    }

}
