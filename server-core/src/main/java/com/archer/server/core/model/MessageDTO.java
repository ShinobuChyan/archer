package com.archer.server.core.model;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.archer.server.common.constant.PatternConstants;
import reactor.util.annotation.NonNull;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * @author Shinobu
 * @since 2018/11/27
 */
public class MessageDTO {

    @NonNull
    @Size(max = 200)
    private String refId;

    @Size(max = 200)
    private String extraInfo;

    @Size(max = 100)
    private String source;

    @Size(max = 100)
    private String topic;

    @Size(max = 100)
    private String tag;

    @NotNull
    @Pattern(regexp = PatternConstants.URL)
    private String url;

    @NotNull
    @Pattern(regexp = PatternConstants.METHOD)
    private String method;

    @Size(max = 50)
    private String contentType;

    private String headers;

    private String content;

    @NotNull
    private List<Integer> intervalList;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date firstTime;

    @Size(max = 200)
    private String stoppedKeyWords;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Integer> getIntervalList() {
        return intervalList;
    }

    public void setIntervalList(List<Integer> intervalList) {
        this.intervalList = intervalList;
    }

    public Date getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(Date firstTime) {
        this.firstTime = firstTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStoppedKeyWords() {
        return stoppedKeyWords;
    }

    public MessageDTO setStoppedKeyWords(String stoppedKeyWords) {
        this.stoppedKeyWords = stoppedKeyWords;
        return this;
    }
}
