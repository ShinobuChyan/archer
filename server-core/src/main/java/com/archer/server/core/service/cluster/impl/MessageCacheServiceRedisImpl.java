package com.archer.server.core.service.cluster.impl;

import com.archer.server.common.constant.RedisKey;
import com.archer.server.core.service.cluster.MessageCacheService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * redis默认实现
 *
 * @author Shinobu
 * @since 2019/6/3
 */
@Service
public class MessageCacheServiceRedisImpl implements MessageCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    public MessageCacheServiceRedisImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 存储：消息id和app实例的关系
     * 此行为需要是原子操作
     *
     * @param messageId {@link com.archer.server.core.model.ArcherMessage#getId()}
     * @param appId     {@link com.archer.server.core.bean.AppInfo#getAppId()}
     * @return {@code true} messageId不存在且put成功
     * {@code false} messageId已存在或put不成功
     */
    @Override
    public boolean putIfAbsent(@NotNull String messageId, @NotNull String appId) {
        return stringRedisTemplate.opsForHash().putIfAbsent(RedisKey.MESSAGE_APP, messageId, appId);
    }

    /**
     * 获取消息所属app实例id
     *
     * @param messageId {@link com.archer.server.core.model.ArcherMessage#getId()}
     * @return appId
     */
    @Override
    public String getAppIdOfMessage(@NotNull String messageId) {
        return (String) stringRedisTemplate.opsForHash().get(RedisKey.MESSAGE_APP, messageId);
    }

    /**
     * 获取所有缓存中的消息id
     *
     * @return messageIdList
     */
    @Override
    public @NotNull Set<Object> allMessageId() {
        return stringRedisTemplate.opsForHash().keys(RedisKey.MESSAGE_APP);
    }

    /**
     * 删除消息id对应的记录
     *
     * @param messageId {@link com.archer.server.core.model.ArcherMessage#getId()}
     */
    @Override
    public void delete(@NotNull String messageId) {
        stringRedisTemplate.opsForHash().delete(RedisKey.MESSAGE_APP, messageId);
    }
}
