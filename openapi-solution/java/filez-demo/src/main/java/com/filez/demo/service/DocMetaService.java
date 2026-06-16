package com.filez.demo.service;

import com.filez.demo.entity.DocMetaEntity;
import com.filez.demo.model.DocMeta;

import java.util.List;

/**
 * 文档元数据服务接口
 */
public interface DocMetaService {

    /**
     * 保存文档元数据
     * @param docMeta 文档元数据
     * @return 保存的文档元数据
     */
    DocMetaEntity saveDocMeta(DocMeta docMeta);

    /**
     * 根据ID获取文档元数据
     * @param id 文档ID
     * @return 文档元数据
     */
    DocMetaEntity getDocMetaById(String id);

    /**
     * 根据文件名获取文档元数据
     * @param name 文件名
     * @return 文档元数据
     */
    DocMetaEntity getDocMetaByName(String name);

    /**
     * 根据文件路径获取文档元数据
     * @param filepath 文件路径
     * @return 文档元数据
     */
    DocMetaEntity getDocMetaByFilepath(String filepath);

    /**
     * 获取所有文档元数据
     * @return 文档元数据列表
     */
    List<DocMetaEntity> getAllDocMetas();

    /**
     * 更新文档元数据
     * @param docMeta 文档元数据
     * @return 更新的文档元数据
     */
    DocMetaEntity updateDocMeta(DocMeta docMeta);

    /**
     * 根据ID删除文档元数据
     * @param id 文档ID
     * @return 是否删除成功
     */
    boolean deleteDocMetaById(String id);

    /**
     * 将数据库实体转换为业务模型
     * @param entity 数据库实体
     * @return 业务模型
     */
    DocMeta convertToDocMeta(DocMetaEntity entity);

    /**
     * 将业务模型转换为数据库实体
     * @param docMeta 业务模型
     * @return 数据库实体
     */
    DocMetaEntity convertToEntity(DocMeta docMeta);

    /**
     * 根据创建人ID获取文档元数据
     * @param createdById 创建人ID
     * @return 文档元数据列表
     */
    List<DocMetaEntity> getDocMetasByCreatedById(String createdById);
}
