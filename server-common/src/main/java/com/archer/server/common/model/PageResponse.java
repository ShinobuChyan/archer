package com.archer.server.common.model;

import java.util.List;

/**
 * 通用分页查询返回体
 *
 * @author Shinobu
 * @since 2017/10/25
 */
public class PageResponse<T> {

    /**
     * 总记录数
     */
    private Long total;
    /**
     * 总页数
     */
    private Integer pages;
    /**
     * 数据
     */
    private List<T> data;

    public PageResponse(Long total, Integer pages, List<T> data) {
        this.total = total;
        this.pages = pages;
        this.data = data;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
