package com.archer.server.common.exception;

/**
 * 当需要response.http-status不为200时使用
 *
 * @author Shinobu
 * @since 2018/9/20
 */
public class ServerErrorException extends RuntimeException  {

    private static final long serialVersionUID = 1233968128635674081L;

    private Integer code;

    public ServerErrorException(String message) {
        super(message);
    }

    public ServerErrorException(int code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
