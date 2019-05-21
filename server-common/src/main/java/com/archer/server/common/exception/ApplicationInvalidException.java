package com.archer.server.common.exception;

/**
 * 表示应用已不可用的异常
 *
 * @author Shinobu
 * @since 2018/2/27
 */
public class ApplicationInvalidException extends RuntimeException {

    private static final long serialVersionUID = 1233968128635674081L;

    private Object data;

    public ApplicationInvalidException(String message) {
        super(message);
    }

    public ApplicationInvalidException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public ApplicationInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationInvalidException(String message, Object data, Throwable cause) {
        super(message, cause);
        this.data = data;
    }

    public Object getData() {
        return data;
    }

}
