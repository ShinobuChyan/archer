package com.archer.server.core.service.cluster;

import com.archer.server.core.bean.AppInfo;
import com.archer.server.core.model.ArcherMessage;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 *
 * @author Shinobu
 * @since 2019/5/31
 */
public interface MessageCacheService {

    /**
     * 存储：消息id和app实例的关系
     * 此行为需要是原子操作
     *
     * @param messageId {@link ArcherMessage#getId()}
     * @param appId     {@link AppInfo#getAppId()}
     * @return {@code true} messageId不存在且put成功
     *         {@code false} messageId已存在或put不成功
     */
    boolean putIfAbsent(@NotNull String messageId, String appId);

    /**
     * 获取消息所属app实例id
     *
     * @param messageId {@link ArcherMessage#getId()}
     * @return appId
     */
    @Nullable String getAppIdOfMessage(@NotNull String messageId);

    /**
     * 获取所有缓存中的消息id
     *
     * @return messageIdList
     */
    @NotNull List<String> allMessageId();

    /**
     * 删除消息id对应的记录
     *
     * @param messageId {@link ArcherMessage#getId()}
     */
    void delete(@NotNull String messageId);

}
