package com.archer.server.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * grayLog属性
 *
 * @author Shinobu
 * @since 2018/10/31
 */
@Component
@ConfigurationProperties(prefix = "gray-log")
public class GrayLogProperties {

    private String host;

    private Integer port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
