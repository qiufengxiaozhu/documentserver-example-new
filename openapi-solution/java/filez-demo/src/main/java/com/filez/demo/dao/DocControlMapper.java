package com.filez.demo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.filez.demo.entity.DocControlEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文档控制配置Mapper接口
 */
@Mapper
public interface DocControlMapper extends BaseMapper<DocControlEntity> {

    /**
     * 根据用户ID和文档ID查询控制配置
     * @param userId 用户ID
     * @param docId 文档ID
     * @return 控制配置
     */
    DocControlEntity selectByUserIdAndDocId(@Param("userId") String userId, @Param("docId") String docId);

    /**
     * 根据用户ID删除控制配置
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteByUserId(@Param("userId") String userId);

    /**
     * 根据文档ID删除控制配置
     * @param docId 文档ID
     * @return 删除数量
     */
    int deleteByDocId(@Param("docId") String docId);
}
