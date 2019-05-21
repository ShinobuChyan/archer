package com.archer.server.core.dao.mapper;

import com.archer.server.core.dao.BaseMapper;
import com.archer.server.core.entity.ArcherMessageEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author Shinobu
 * @since 2018/11/27
 */
@Repository
public interface ArcherMessageMapper extends BaseMapper<ArcherMessageEntity> {

    /**
     * 根据主键计数
     *
     * @param id id
     * @return count
     */
    int countByPrimaryKey(@NotNull String id);

    /**
     * 根据唯一索引计数
     *
     * @param refId     关联业务ID
     * @param extraInfo 其他关联信息
     * @param source    消息来源
     * @return count
     */
    int countByUniqueIndex(@NotNull @Param("refId") String refId,
                           @NotNull @Param("extraInfo") String extraInfo,
                           @NotNull @Param("source") String source);

    /**
     * 根据主键更新【发送中消息的】主要信息
     *
     * @param id            id
     * @param status        消息状态
     * @param lastTime      最后发送时间
     * @param nextTime      闲置消息下次发送时间
     * @param intervalIndex 当前发送间隔索引
     * @return count
     */
    int updateSendingMainColumnsById(@NotNull @Param("id") String id, @NotNull @Param("status") String status,
                                     @Param("lastTime") Date lastTime, @Param("nextTime") Date nextTime,
                                     @NotNull @Param("intervalIndex") int intervalIndex);

    /**
     * 根据主键查询消息状态
     *
     * @param id id
     * @return status
     */
    @Nullable
    String selectStatusByPrimaryKey(@NotNull String id);

    /**
     * 根据主键更新消息状态
     *
     * @param id     id
     * @param status status
     * @return count
     */
    int updateStatusByPrimaryKey(@NotNull @Param("id") String id, @NotNull @Param("status") String status);

    /**
     * 当应用关闭时，将idList中所有发送中的消息置为闲置状态
     *
     * @param idList idList
     * @return count
     */
    int updateSendingMessageToIdleByIdListWhenAppShutdown(@NotNull @Param("idList") List<String> idList);

    /**
     * 获取处于闲置状态且已可以发送的消息
     *
     * @return messages
     */
    List<ArcherMessageEntity> selectPreparedIdleMessages();

    /**
     * 更新拉取到的闲置消息
     *
     * @param idList idList
     * @return count
     */
    int updateFetchedIdleMessagesByIdList(@NotNull @Param("idList") List<String> idList);

}
