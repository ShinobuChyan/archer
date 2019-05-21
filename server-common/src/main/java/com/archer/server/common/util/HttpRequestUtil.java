package com.archer.server.common.util;

import com.archer.server.common.exception.RestRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * 发送HTTP请求的
 *
 * @author Shinobu
 * @since 2018/10/8
 */
public class HttpRequestUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestUtil.class);

    public static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(60))
            .build();

    /**
     * 发送GET请求并记录来往信息
     *
     * @param title 发送请求的主题，用于日志记录
     * @param url   请求url
     * @return responseStr
     */
    public static String getAsync(String title, String url, Map<String, String> headers) {
        var builder = HttpRequest.newBuilder(URI.create(url))
                .GET();
        headers.forEach(builder::header);
        var request = builder.build();

        try {
            var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("[PostJsonForString] error, title: " + title + ", url: " + url + ", response: " + response);
            return response.body();
        } catch (Exception e) {
            LOGGER.error("[PostJsonForString] error, title: " + title + ", url: " + url);
            throw new RestRuntimeException("请求发送失败", e);
        }
    }

    /**
     * 发送JSON-POST请求并记录来往信息
     *
     * @param title    发送请求的主题，用于日志记录
     * @param url      请求url
     * @param jsonBody json字符串请求体
     * @return responseStr
     */
    public static String postJsonForString(String title, String url, String jsonBody) {
        jsonBody = jsonBody == null ? "" : jsonBody;
        var request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("[PostJsonForString] title: " + title + ", url: " + url +
                    ", request: " + (jsonBody.length() > 1000 ? jsonBody.substring(0, 1000) : jsonBody) +
                    ", response: " + (response.body().length() > 1000 ? response.body().substring(0, 1000) : response.body()));
            return response.body();
        } catch (Exception e) {
            LOGGER.error("[PostJsonForString] error, title: " + title + ", url: " + url + ", request: " + jsonBody);
            throw new RestRuntimeException("请求发送失败", e);
        }
    }

}
