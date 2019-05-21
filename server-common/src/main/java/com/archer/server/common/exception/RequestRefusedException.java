package com.archer.server.common.exception;

/**
 * 仅用于身份不合法类型的访问拒绝
 *
 * @author Shinobu
 * @since 2018/3/1
 */
public class RequestRefusedException extends RuntimeException {

    private static final long serialVersionUID = 6730172134206394634L;

    public RequestRefusedException(String message) {
        super(message);
    }

}
