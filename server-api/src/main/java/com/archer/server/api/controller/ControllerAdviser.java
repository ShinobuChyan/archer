package com.archer.server.api.controller;

import com.alibaba.fastjson.JSON;
import com.archer.server.common.exception.AssertFailedException;
import com.archer.server.common.exception.RequestRefusedException;
import com.archer.server.common.exception.RestRuntimeException;
import com.archer.server.common.exception.ServerErrorException;
import com.archer.server.common.model.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.stream.Collectors;

/**
 * 普通的ExceptionHandler
 *
 * @author Shinobu
 * @since 2018/2/27
 */
@ControllerAdvice
public class ControllerAdviser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerAdviser.class);

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object handle(Exception e) {

        LOGGER.error("[ERROR] ExceptionHandler捕获了异常", e);

        // 访问拒绝
        if (e instanceof RequestRefusedException) {
            LOGGER.warn(e.getMessage());
            return CommonResult.refusedResult(e.getMessage(), null);
        }
        // 断言失败
        if (e instanceof AssertFailedException) {
            LOGGER.warn(e.getMessage());
            return CommonResult.badResult(e.getMessage(), null);
        }
        // Redis连接超时
        if (e instanceof RedisConnectionFailureException) {
            LOGGER.warn("[Redis连接超时]: " + e.getMessage());
            return CommonResult.commonFailedResult("网络延迟，请稍后重试", null);
        }
        // api入参校验失败
        if (e instanceof MethodArgumentNotValidException) {
            var errorMsg = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .collect(Collectors.toList());
            var errStr = JSON.toJSONString(errorMsg);
            LOGGER.warn("参数校验失败: " + errStr);
            return CommonResult.badResult("参数校验失败", errStr);
        }
        // 业务控制抛错
        if (e instanceof RestRuntimeException) {
            String dataStr = JSON.toJSONString(((RestRuntimeException) e).getData());
            LOGGER.warn("[RestRuntimeException] data: " + dataStr);
            return CommonResult.commonFailedResult(e.getMessage(), null);
        }
        // 需要返回非200的抛错
        if (e instanceof ServerErrorException) {
            var ex = (ServerErrorException) e;
            return ResponseEntity
                    .status(ex.getCode() == null ? 500 : ex.getCode())
                    .body(CommonResult.commonFailedResult(e.getMessage(), null));
        }

        return CommonResult.commonErrorResult("系统错误，请联系相关人员", null);
    }

}
