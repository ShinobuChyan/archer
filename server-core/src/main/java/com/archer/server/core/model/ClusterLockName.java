package com.archer.server.core.model;

import java.util.Map;

/**
 * @author Shinobu
 * @since 2018/10/9
 */
public class ClusterLockName {

    private final String lockName;

    private final Map<String, String> params;

    private ClusterLockName(String lockName, Map<String, String> params) {
        this.lockName = lockName;
        this.params = params;
    }

    /**
     * 生成锁名对象
     *
     * @param lockEnum 锁类型
     * @param params   自定义锁参数，用于控制锁粒度
     */
    public static ClusterLockName newLockName(ClusterLockEnum lockEnum, Map<String, String> params) {
        return new ClusterLockName(lockEnum.name(), params);
    }

    /**
     * @return lockName&paramK1=paramV1&paramK2=paramV2...
     */
    @Override
    public String toString() {
        StringBuilder lockNameParams = new StringBuilder();
        if (this.params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                lockNameParams.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return lockName + lockNameParams.toString();
    }
}
