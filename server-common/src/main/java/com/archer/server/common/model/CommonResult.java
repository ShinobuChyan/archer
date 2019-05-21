package com.archer.server.common.model;

import java.io.Serializable;

/**
 * 通用Rest返回体
 *
 * @author Shinobu
 * @since 2018/9/18
 */
public class CommonResult implements Serializable {

    private static final long serialVersionUID = -4767051169345321109L;

    public enum StatusEnum {
        /**
         * 通用状态
         */
        SUCCESS, FAILED, ERROR
    }

    /**
     * 通用成功、失败、错误code
     */
    private static final transient String CODE_COMMON_SUCCESS = "0000";
    private static final transient String CODE_COMMON_FAILED = "0500";
    private static final transient String CODE_COMMON_ERROR = "9000";
    /**
     * 访问拒绝
     */
    private static final transient String CODE_REQ_REFUSED = "0403";
    /**
     * 断言失败
     */
    private static final transient String CODE_BAD = "0400";

    private String status;

    private String code;

    private String message;

    private Object meta;

    private Object data;

    private CommonResult(String status, String code, String message, Object meta, Object data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.meta = meta;
        this.data = data;
    }

    /**
     * 基础构造
     */
    public static CommonResult commonSuccessResult(String message, Object data) {
        return new CommonResult(StatusEnum.SUCCESS.name(), CODE_COMMON_SUCCESS, message, null, data);
    }
    public static CommonResult commonFailedResult(String message, Object data) {
        return new CommonResult(StatusEnum.FAILED.name(), CODE_COMMON_FAILED, message, null, data);
    }
    public static CommonResult commonErrorResult(String message, Object data) {
        return new CommonResult(StatusEnum.ERROR.name(), CODE_COMMON_ERROR, message, null, data);
    }

    /**
     * 访问拒绝
     */
    public static CommonResult refusedResult(String message, Object data) {
        return new CommonResult(StatusEnum.FAILED.name(), CODE_REQ_REFUSED, message, null, data);
    }
    /**
     * 断言失败
     */
    public static CommonResult badResult(String message, Object data) {
        return new CommonResult(StatusEnum.FAILED.name(), CODE_BAD, message, null, data);
    }

    /**
     * 自定义构造
     */
    public static CommonResult customResult(StatusEnum status, String code, String message, Object meta, Object data) {
        return new CommonResult(status.name(), code, message, meta, data);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getMeta() {
        return meta;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
