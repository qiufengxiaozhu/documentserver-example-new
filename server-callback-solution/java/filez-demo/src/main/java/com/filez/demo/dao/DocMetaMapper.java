package com.filez.demo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.filez.demo.entity.DocMetaEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Document metadata Mapper interface
 */
@Mapper
public interface DocMetaMapper extends BaseMapper<DocMetaEntity> {

    /**
     * Query document metadata by filename
     * @param name Filename
     * @return Document metadata
     */
    DocMetaEntity selectByName(@Param("name") String name);

    /**
     * Query document metadata by file path
     * @param filepath File path
     * @return Document metadata
     */
    DocMetaEntity selectByFilepath(@Param("filepath") String filepath);

    /**
     * Query all document metadata ordered by modification time in descending order
     * @return Document metadata list
     */
    List<DocMetaEntity> selectAllOrderByModifiedAt();

    /**
     * Query document metadata by creator ID
     * @param createdById Creator ID
     * @return Document metadata list
     */
    List<DocMetaEntity> selectByCreatedById(@Param("createdById") String createdById);
}
