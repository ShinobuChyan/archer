package com.archer.server.common.exception;

/**
 * 断言失败异常
 *
 * @author Shinobu
 * @since 2018/3/23
 */
public class AssertFailedException extends RuntimeException {

    private static final long serialVersionUID = 6730172134206394634L;

    public AssertFailedException(String message) {
        super(message);
    }

}
