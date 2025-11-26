package com.filez.demo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.filez.demo.entity.DocControlEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Document control configuration Mapper interface
 */
@Mapper
public interface DocControlMapper extends BaseMapper<DocControlEntity> {

    /**
     * Query control configuration by user ID and document ID
     * @param userId User ID
     * @param docId Document ID
     * @return Control configuration
     */
    DocControlEntity selectByUserIdAndDocId(@Param("userId") String userId, @Param("docId") String docId);

    /**
     * Delete control configuration by user ID
     * @param userId User ID
     * @return Number of deleted records
     */
    int deleteByUserId(@Param("userId") String userId);

    /**
     * Delete control configuration by document ID
     * @param docId Document ID
     * @return Number of deleted records
     */
    int deleteByDocId(@Param("docId") String docId);
}
