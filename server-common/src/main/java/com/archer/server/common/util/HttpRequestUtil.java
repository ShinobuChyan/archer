package com.archer.server.common.util;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * 发送HTTP请求的
 *
 * @author Shinobu
 * @since 2018/10/8
 */
public class HttpRequestUtil {

    public static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(60))
            .build();

}
