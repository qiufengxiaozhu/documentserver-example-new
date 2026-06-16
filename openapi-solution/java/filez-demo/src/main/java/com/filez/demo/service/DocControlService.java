package com.filez.demo.service;

import com.filez.demo.entity.DocControlEntity;
import com.filez.demo.model.DocControl;

/**
 * 文档控制配置服务接口
 */
public interface DocControlService {

    /**
     * 保存文档控制配置
     * @param userId 用户ID
     * @param docId 文档ID
     * @param docControl 控制配置
     * @return 保存的配置实体
     */
    DocControlEntity saveDocControl(String userId, String docId, DocControl docControl);

    /**
     * 获取文档控制配置
     * @param userId 用户ID
     * @param docId 文档ID
     * @return 控制配置
     */
    DocControl getDocControl(String userId, String docId);

    /**
     * 更新文档控制配置
     * @param userId 用户ID
     * @param docId 文档ID
     * @param docControl 控制配置
     * @return 更新的配置实体
     */
    DocControlEntity updateDocControl(String userId, String docId, DocControl docControl);

    /**
     * 删除用户的所有控制配置
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteByUserId(String userId);

    /**
     * 删除文档的所有控制配置
     * @param docId 文档ID
     * @return 是否删除成功
     */
    boolean deleteByDocId(String docId);

    /**
     * 将数据库实体转换为业务模型
     * @param entity 数据库实体
     * @return 业务模型
     */
    DocControl convertToDocControl(DocControlEntity entity);

    /**
     * 将业务模型转换为数据库实体
     * @param userId 用户ID
     * @param docId 文档ID
     * @param docControl 业务模型
     * @return 数据库实体
     */
    DocControlEntity convertToEntity(String userId, String docId, DocControl docControl);
}
