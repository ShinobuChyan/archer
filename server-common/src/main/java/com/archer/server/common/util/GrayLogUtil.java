package com.archer.server.common.util;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GrayLog日志存储
 *
 * @author Shinobu
 * @since 2018/11/1
 */
public class GrayLogUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrayLogUtil.class);

    private static DatagramSocket socket = null;

    private static String host;

    private static Integer port;

    private static String env;

    private static ExecutorService executor;

    /**
     * 最大重发失败次数
     */
    private static final int MAX_FAILED_COUNT = 5;

    public static void init(String h, int p, String environment, ExecutorService commonSerialTaskPool) {
        if (host == null) {
            host = h;
        }
        if (port == null) {
            port = p;
        }
        if (env == null) {
            env = environment;
        }
        if (executor == null) {
            executor = commonSerialTaskPool;
        }
    }

    /**
     * 消息生命周期日志
     *
     * @param msgId 消息ID
     * @param event 事件
     * @param time  发生时间
     * @param more  额外信息
     */
    public static void messageLifecycleLog(@NotNull String msgId, @NotNull String event, @NotNull String time, String more) {
        var extraParams = new HashMap<String, Object>(8);
        extraParams.put("msgId", msgId);
        extraParams.put("event", event);
        extraParams.put("time", time);
        extraParams.put("more", more);
        newLog("lifecycle-log", extraParams);
    }

    /**
     * http报文日志
     *
     * @param msgId   消息ID
     * @param type    http报文类型：1 请求，2 响应
     * @param time    发生时间
     * @param title   request/response.toString()
     * @param headers JSON.toJSONString(request/response.headers.map())
     * @param content content/body
     */
    public static void httpPacketLog(@NotNull String msgId, @NotNull int type, @NotNull String time, @NotNull String title, String headers, String content) {
        var extraParams = new HashMap<String, Object>(8);
        extraParams.put("msgId", msgId);
        extraParams.put("type", type == 1 ? "REQUEST" : "RESPONSE");
        extraParams.put("time", time);
        extraParams.put("title", title);
        extraParams.put("headers", headers);
        extraParams.put("content", content);
        newLog("http-packet-log", extraParams);
    }

    private static void newLog(@NotNull String topic, @NotNull Map<String, Object> extraParams) {
        Map<String, Object> basicParams = basicParams(topic);
        appendParams(basicParams, extraParams);
        var failedCount = new AtomicInteger(0);
        executor.execute(() -> send(topic, basicParams, failedCount));
    }

    private static void send(@NotNull String topic, Map<String, Object> basicParams, AtomicInteger failedCount) {
        try {
            send(basicParams);
        } catch (Exception e) {
            if (failedCount.intValue() > MAX_FAILED_COUNT) {
                var paramStr = JSON.toJSONString(basicParams);
                LOGGER.warn("[GrayLog] -> error, topic: " + topic + "params: " +
                        (paramStr.length() > 1000 ? (paramStr.substring(0, 1000) + "...") : paramStr));
                return;
            }
            failedCount.getAndIncrement();
            executor.execute(() -> send(topic, basicParams, failedCount));
        }
    }

    private static Map<String, Object> basicParams(String topic) {
        Map<String, Object> params = new LinkedHashMap<>(32);
        params.put("version", "1.1");
        params.put("host", "archer-server");
        params.put("short_message", topic);
        params.put("_app_name", "archer-server-" + env);
        params.put("_topic", topic);
        return params;
    }

    private static void appendParams(Map<String, Object> basicParams, Map<String, Object> extraParams) {
        for (Map.Entry<String, Object> entry : extraParams.entrySet()) {
            var value = entry.getValue();
            if (value instanceof String) {
                value = ((String) value).length() > 1000 ? ((String) value).substring(0, 1000) : value;
            }
            basicParams.put("_" + entry.getKey(), value);
        }
    }

    private static void send(Map<String, Object> params) throws Exception {
        if (socket == null) {
            socket = new DatagramSocket();
        }
        var address = InetAddress.getByName(host);
        var content = JSON.toJSONString(params);
        var bytes = content.getBytes(StandardCharsets.UTF_8);
        var packet = new DatagramPacket(bytes, bytes.length, address, port);
        socket.send(packet);
    }

}
