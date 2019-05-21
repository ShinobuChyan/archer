package com.archer.server.core.dao;

import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * 为Mapper提供基础数据库操作的实现
 *
 * @author Shinobu
 */
@Repository
public interface BaseMapper<T> extends Mapper<T>, MySqlMapper<T> {
}
