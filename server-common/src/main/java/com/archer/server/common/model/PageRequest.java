package com.archer.server.common.model;

import com.alibaba.fastjson.JSON;
import com.archer.server.common.interfaces.Trimmable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author Shinobu
 * @since 2018/3/2
 */
public class PageRequest<T> implements Trimmable {

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;

    @NotNull
    @Valid
    private T dto;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public T getDto() {
        return dto;
    }

    public PageRequest<T> setDto(T dto) {
        this.dto = dto;
        return this;
    }

    public Integer getPage() {
        return page;
    }

    public PageRequest<T> setPage(Integer page) {
        this.page = page;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public PageRequest<T> setSize(Integer size) {
        this.size = size;
        return this;
    }
}
