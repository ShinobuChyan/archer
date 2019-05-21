package com.archer.server.common.exception;

/**
 * 业务控制抛出的异常
 *
 * @author Shinobu
 * @since 2018/2/27
 */
public class RestRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1233968128635674081L;

    private Object data;

    public RestRuntimeException(String message) {
        super(message);
    }

    public RestRuntimeException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public RestRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestRuntimeException(String message, Object data, Throwable cause) {
        super(message, cause);
        this.data = data;
    }

    public Object getData() {
        return data;
    }

}
