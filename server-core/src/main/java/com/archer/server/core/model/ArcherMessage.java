package com.archer.server.core.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.archer.server.core.entity.ArcherMessageEntity;
import com.archer.server.common.constant.ValueConstants;
import com.archer.server.common.util.CommonUtil;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.locks.StampedLock;

/**
 * @author Shinobu
 * @since 2018/11/27
 */
public class ArcherMessage {

    private static final int DEFAULT_MAX_READ_SPINS = 1 << 8;

    private final transient StampedLock lock = new StampedLock();

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

    private ArcherMessage() {
    }

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
        this.headers = JSON.parseObject(entity.getHeaders(), new TypeReference<>() {
        });
        this.intervalList = JSON.parseArray(entity.getIntervalList(), Integer.class);
    }

    private interface ReadFunction<R> {

        /**
         * 实现此方法以在StampedLock模式下读取数据
         *
         * @return result
         */
        R execute();
    }

    /**
     * 使用StampedLock的先乐观后加锁的读取模板
     *
     * @param spins 乐观读最大自旋次数
     * @param func  读取逻辑
     * @param <R>   读取结果Type
     * @return result
     */
    private <R> R readWithStampedLock(int spins, ReadFunction<R> func) {
        final var lock = this.lock;
        long stamp = lock.tryOptimisticRead();
        for (int i = 0; i < spins; i++, stamp = lock.tryOptimisticRead()) {
            var r = func.execute();
            if (lock.validate(stamp)) {
                return r;
            }
        }

        stamp = lock.readLock();
        try {
            return func.execute();
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public String toString() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> JSON.toJSONString(this));
    }

    /**
     * 获取全部header
     */
    public @NotNull Map<String, String> getHeaders() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> {
            final var headers = this.headers;
            return headers == null ? new HashMap<>(2) : new HashMap<>(headers);
        });
    }

    /**
     * 获取下一个重发间隔
     *
     * @return 如果当前已到达间隔列表末位，则返回null
     */
    public @Nullable
    Integer nextInterval() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> {
            final var intervalIndex = this.intervalIndex;
            final var intervalList = this.intervalList;
            return intervalIndex + 1 >= intervalList.size() ? null : intervalList.get(intervalIndex + 1);
        });
    }

    /**
     * 获取距离下一次重发的毫秒数
     *
     * @return milliseconds
     */
    public long nextMillisCountdown() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> {
            final var lastTime = this.lastTime;
            final var intervalIndex = this.intervalIndex;
            final var intervalList = this.intervalList;
            return lastTime == null ?
                    firstTime.getTime() + intervalList.get(intervalIndex) * 1000 - System.currentTimeMillis() :
                    lastTime.getTime() + intervalList.get(intervalIndex) * 1000 - System.currentTimeMillis();
        });
    }

    /**
     * 提升重发等级，如果已到达末位，则不处理
     */
    public void levelUp() {
        final var lock = this.lock;
        long stamp = lock.writeLock();
        try {
            if (intervalIndex + 1 >= intervalList.size()) {
                return;
            }
            intervalIndex++;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public @NotNull ArcherMessageEntity toEntity() {
        return readWithStampedLock(3, () -> {
            var entity = new ArcherMessageEntity();
            CommonUtil.attrTransfer(this, entity);
            entity.setHeaders(JSON.toJSONString(this.headers));
            entity.setIntervalList(JSON.toJSONString(this.intervalList));
            entity.setIntervalIndex(this.intervalIndex);
            entity.setCreatorId(ValueConstants.CREATOR_SYSTEM);
            entity.setCreatorName(ValueConstants.CREATOR_SYSTEM);
            return entity;
        });
    }

    /**
     * 仅克隆部分重要属性
     */
    public ArcherMessage simplyClone() {
        return readWithStampedLock(64, () -> {
            var clone = new ArcherMessage();
            clone.id = this.id;
            clone.status = this.status;
            clone.nextTime = this.nextTime == null ? null : new Date(this.nextTime.getTime());
            clone.lastTime = this.lastTime == null ? null : new Date(this.lastTime.getTime());
            clone.intervalIndex = this.intervalIndex;
            return clone;
        });
    }

    /**
     * 克隆所有属性
     */
    public ArcherMessage deepClone() {
        return readWithStampedLock(64, () -> {
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
        });
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
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> id);
    }

    public String getRefId() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> refId);
    }

    public String getExtraInfo() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> extraInfo);
    }

    public String getTopic() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> topic);
    }

    public String getTag() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> tag);
    }

    public String getSource() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> source);
    }

    public String getStatus() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> status);
    }

    public String getUrl() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> url);
    }

    public String getMethod() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> method);
    }

    public String getContentType() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> contentType);
    }

    public String getContent() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> content);
    }

    public int getIntervalIndex() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> intervalIndex);
    }

    public Date getFirstTime() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> firstTime);
    }

    public Date getLastTime() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> lastTime);
    }

    public String getStoppedKeyWords() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> stoppedKeyWords);
    }

    public Date getNextTime() {
        return readWithStampedLock(DEFAULT_MAX_READ_SPINS, () -> nextTime);
    }

    public void setStatus(String status) {
        final var lock = this.lock;
        var stamp = lock.writeLock();
        try {
            this.status = status;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public void setLastTime(Date lastTime) {
        final var lock = this.lock;
        var stamp = lock.writeLock();
        try {
            this.lastTime = lastTime;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public void setNextTime(Date nextTime) {
        final var lock = this.lock;
        var stamp = lock.writeLock();
        try {
            this.nextTime = nextTime;
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}
