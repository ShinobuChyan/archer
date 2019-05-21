package com.archer.server.core.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.archer.server.core.entity.ArcherMessageEntity;
import com.archer.server.common.constant.ValueConstants;
import com.archer.server.common.util.CommonUtil;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Shinobu
 * @since 2018/11/27
 */
public class ArcherMessage {

    private final transient ReadWriteLock lock = new ReentrantReadWriteLock();

    private String id;

    private String refId;

    private String extraInfo;

    private String topic;

    private String tag;

    private String source;

    private String url;

    private String method;

    private String contentType;

    private Map<String, String> headers;

    private String content;

    private List<Integer> intervalList;

    private Date firstTime;

    /**
     * 消息状态：-1 待发送，0 发送完毕，1 发送中，2 自然结束，3 主动停止
     */
    private String status;

    private int intervalIndex;

    private Date lastTime;

    private Date nextTime;

    private String stoppedKeyWords;

    private ArcherMessage() {}

    public static ArcherMessage initMessage(@NotNull String id, @NotNull String refId, @NotNull String extraInfo, @NotNull String source,
                         String topic, String tag, @NotNull String status, Date nextTime,
                         @NotNull String url, @NotNull String method, @NotNull String contentType,
                         String headers, String content,
                         @NotNull List<Integer> intervalList, @NotNull Date firstTime, @NotNull String stoppedKeyWords) {
        var message = new ArcherMessage();
        message.id = id;
        message.refId = refId;
        message.extraInfo = extraInfo;
        message.topic = topic;
        message.tag = tag;
        message.source = source;
        message.url = url;
        message.method = method;
        message.contentType = contentType;
        message.content = content;
        message.stoppedKeyWords = stoppedKeyWords;
        message.status = status;
        message.nextTime = nextTime;

        message.intervalIndex = 0;
        message.lastTime = null;

        message.headers = new HashMap<>(16);
        if (headers != null) {
            var headerJson = JSON.parseObject(headers);
            for (Map.Entry<String, Object> entry : headerJson.entrySet()) {
                if (entry.getValue() instanceof String) {
                    message.headers.put(entry.getKey(), (String) entry.getValue());
                }
            }
        }
        message.intervalList = new ArrayList<>(intervalList);
        message.firstTime = new Date(firstTime.getTime());
        return message;
    }

    public ArcherMessage(@NotNull ArcherMessageEntity entity) {
        CommonUtil.attrTransfer(entity, this);
        this.headers = JSON.parseObject(entity.getHeaders(), new TypeReference<>() {});
        this.intervalList = JSON.parseArray(entity.getIntervalList(), Integer.class);
    }

    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            return JSON.toJSONString(this);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取全部header
     */
    public @NotNull Map<String, String> getHeaders() {
        lock.readLock().lock();
        try {
            return this.headers == null ? new HashMap<>(2) : new HashMap<>(headers);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取下一个重发间隔
     *
     * @return 如果当前已到达间隔列表末位，则返回null
     */
    public @Nullable
    Integer nextInterval() {
        lock.readLock().lock();
        try {
            if (intervalIndex + 1 >= this.intervalList.size()) {
                return null;
            }
            return this.intervalList.get(intervalIndex + 1);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取距离下一次重发的毫秒数
     * @return milliseconds
     */
    public long nextMillisCountdown() {
        lock.readLock().lock();
        long countdown;
        try {
            if (lastTime == null) {
                countdown = firstTime.getTime() + intervalList.get(intervalIndex) * 1000 - System.currentTimeMillis();
            } else {
                countdown = lastTime.getTime() + intervalList.get(intervalIndex) * 1000 - System.currentTimeMillis();
            }
            return countdown < 0 ? 0 : countdown;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 提升重发等级，如果已到达末位，则不处理
     */
    public void levelUp() {
        lock.writeLock().lock();
        try {
            if (intervalIndex + 1 >= intervalList.size()) {
                return;
            }
            intervalIndex++;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public @NotNull ArcherMessageEntity toEntity() {
        lock.readLock().lock();
        try {
            var entity = new ArcherMessageEntity();
            CommonUtil.attrTransfer(this, entity);
            entity.setHeaders(JSON.toJSONString(this.headers));
            entity.setIntervalList(JSON.toJSONString(this.intervalList));
            entity.setIntervalIndex(this.intervalIndex);
            entity.setCreatorId(ValueConstants.CREATOR_SYSTEM);
            entity.setCreatorName(ValueConstants.CREATOR_SYSTEM);
            return entity;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 仅克隆部分重要属性
     */
    public ArcherMessage simplyClone() {
        lock.readLock().lock();
        try {
            var clone = new ArcherMessage();
            clone.id = this.id;
            clone.status = this.status;
            clone.nextTime = this.nextTime == null ? null : new Date(this.nextTime.getTime());
            clone.lastTime = this.lastTime == null ? null : new Date(this.lastTime.getTime());
            clone.intervalIndex = this.intervalIndex;
            return clone;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 克隆所有属性
     */
    public ArcherMessage deepClone() {
        lock.readLock().lock();
        try {
            var clone = new ArcherMessage();
            clone.id = this.id;
            clone.refId = this.refId;
            clone.extraInfo = this.extraInfo;
            clone.topic = this.topic;
            clone.tag = this.tag;
            clone.source = this.source;
            clone.url = this.url;
            clone.method = this.method;
            clone.contentType = this.contentType;
            clone.content = this.content;
            clone.status = this.status;
            clone.intervalIndex = this.intervalIndex;
            clone.stoppedKeyWords = this.stoppedKeyWords;

            clone.firstTime = this.firstTime == null ? null : new Date(this.firstTime.getTime());
            clone.lastTime = this.lastTime == null ? null : new Date(this.lastTime.getTime());
            clone.nextTime = this.nextTime == null ? null : new Date(this.nextTime.getTime());
            clone.headers = new HashMap<>(this.headers);
            clone.intervalList = new ArrayList<>(intervalList);
            return clone;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @return 停止重发关键词列表
     */
    public List<String> getStoppedKeyWordsList() {
        return Arrays.asList(stoppedKeyWords.split(","));
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getId() {
        lock.readLock().lock();
        try {
            return id;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getRefId() {
        lock.readLock().lock();
        try {
            return refId;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getExtraInfo() {
        lock.readLock().lock();
        try {
            return extraInfo;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getTopic() {
        lock.readLock().lock();
        try {
            return topic;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getTag() {
        lock.readLock().lock();
        try {
            return tag;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getSource() {
        lock.readLock().lock();
        try {
            return source;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getStatus() {
        lock.readLock().lock();
        try {
            return status;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getUrl() {
        lock.readLock().lock();
        try {
            return url;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getMethod() {
        lock.readLock().lock();
        try {
            return method;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getContentType() {
        lock.readLock().lock();
        try {
            return contentType;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getContent() {
        lock.readLock().lock();
        try {
            return content;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getIntervalIndex() {
        lock.readLock().lock();
        try {
            return intervalIndex;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Date getFirstTime() {
        lock.readLock().lock();
        try {
            return firstTime;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Date getLastTime() {
        lock.readLock().lock();
        try {
            return lastTime;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setStatus(String status) {
        lock.writeLock().lock();
        try {
            this.status = status;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void setLastTime(Date lastTime) {
        lock.writeLock().lock();
        try {
            this.lastTime = lastTime;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getStoppedKeyWords() {
        lock.readLock().lock();
        try {
            return stoppedKeyWords;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setStoppedKeyWords(String stoppedKeyWords) {
        lock.writeLock().lock();
        try {
            this.stoppedKeyWords = stoppedKeyWords;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Date getNextTime() {
        lock.readLock().lock();
        try {
            return nextTime;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setNextTime(Date nextTime) {
        lock.writeLock().lock();
        try {
            this.nextTime = nextTime;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
