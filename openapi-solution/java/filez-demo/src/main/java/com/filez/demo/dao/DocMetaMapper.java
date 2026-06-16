package com.filez.demo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.filez.demo.entity.DocMetaEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档元数据Mapper接口
 */
@Mapper
public interface DocMetaMapper extends BaseMapper<DocMetaEntity> {

    /**
     * 根据文件名查询文档元数据
     * @param name 文件名
     * @return 文档元数据
     */
    DocMetaEntity selectByName(@Param("name") String name);

    /**
     * 根据文件路径查询文档元数据
     * @param filepath 文件路径
     * @return 文档元数据
     */
    DocMetaEntity selectByFilepath(@Param("filepath") String filepath);

    /**
     * 查询所有文档元数据并按修改时间降序排列
     * @return 文档元数据列表
     */
    List<DocMetaEntity> selectAllOrderByModifiedAt();

    /**
     * 根据创建人ID查询文档元数据
     * @param createdById 创建人ID
     * @return 文档元数据列表
     */
    List<DocMetaEntity> selectByCreatedById(@Param("createdById") String createdById);
}
