package com.archer.server.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 通用未分类方法
 *
 * @author Shinobu
 * @since 2018/3/6
 */
public class CommonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtil.class);

    private static final int MAX_PARAM_STR_LENGTH = 500;

    /**
     * 实体类同名属性互传
     *
     * @param source 源实体
     * @param target 目标实体
     */
    public static void attrTransfer(Object source, Object target) {

        Class sClass = source.getClass();
        Class tClass = target.getClass();

        Field[] sFields = sClass.getDeclaredFields();

        for (Field sField : sFields) {

            if ("serialVersionUID".equals(sField.getName())) {
                continue;
            }

            try {
                Field tField = tClass.getDeclaredField(sField.getName());
                sField.setAccessible(true);
                tField.setAccessible(true);
                tField.set(target, sField.get(source));
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * 获取当前网络ip
     *
     * 网上抄来的
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
                //根据网卡取本机配置的IP
                InetAddress iNet = null;
                try {
                    iNet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                if (iNet != null) {
                    ipAddress = iNet.getHostAddress();
                }
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        //"***.***.***.***".length() = 15
        if (ipAddress != null && ipAddress.length() > 15) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

    /**
     * 提取请求参数
     */
    public static Map<String, Object> extractRequestParams(@NotNull HttpServletRequest request) {

        var result = new LinkedHashMap<String, Object>();

        result.put("paramMap", request.getParameterMap());
        // json请求
        try {
            var br = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));
            var sb = new AtomicReference<>(new StringBuffer());
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.get().append(temp);
            }
            br.close();
            JSONObject bodyJson;
            try {
                bodyJson = JSON.parseObject(sb.toString());
            } catch (Exception e) {
                return result;
            }
            bodyJson.forEach((key, value) -> {
                if (value instanceof String && ((String) value).length() > MAX_PARAM_STR_LENGTH) {
                    bodyJson.put(key, ((String) value).substring(0, 50));
                }
                else if (value instanceof List) {
                    var copy = new ArrayList<>();
                    for (Object o : (List) value) {
                        if (o instanceof String) {
                            copy.add(((String) o).length() > MAX_PARAM_STR_LENGTH ? ((String) o).substring(0, 50) : o);
                        } else {
                            copy.add(o);
                        }
                    }
                    bodyJson.put(key, copy);
                }
            });

            result.put("body", bodyJson);
            return result;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return result;
        }
    }

}
