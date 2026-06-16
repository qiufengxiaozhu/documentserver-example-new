package com.filez.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.filez.demo.dao.DocControlMapper;
import com.filez.demo.entity.DocControlEntity;
import com.filez.demo.model.DocControl;
import com.filez.demo.model.DocExtension;
import com.filez.demo.model.DocPermission;
import com.filez.demo.model.DocWaterMark;
import com.filez.demo.service.DocControlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 文档控制配置服务实现类
 */
@Slf4j
@Service
public class DocControlServiceImpl implements DocControlService {

    @Resource
    private DocControlMapper docControlMapper;

    @Override
    public DocControlEntity saveDocControl(String userId, String docId, DocControl docControl) {
        DocControlEntity entity = convertToEntity(userId, docId, docControl);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        
        docControlMapper.insert(entity);
        log.debug("保存文档控制配置成功，用户: {}, 文档: {}", userId, docId);
        return entity;
    }

    @Override
    public DocControl getDocControl(String userId, String docId) {
        DocControlEntity entity = docControlMapper.selectByUserIdAndDocId(userId, docId);
        if (entity == null) {
            log.debug("未找到文档控制配置，用户: {}, 文档: {}", userId, docId);
            return null;
        }
        return convertToDocControl(entity);
    }

    @Override
    public DocControlEntity updateDocControl(String userId, String docId, DocControl docControl) {
        DocControlEntity existingEntity = docControlMapper.selectByUserIdAndDocId(userId, docId);
        
        if (existingEntity == null) {
            // 如果不存在，创建新的
            return saveDocControl(userId, docId, docControl);
        }
        
        // 更新现有配置
        DocControlEntity entity = convertToEntity(userId, docId, docControl);
        entity.setId(existingEntity.getId());
        entity.setCreateTime(existingEntity.getCreateTime());
        entity.setUpdateTime(new Date());
        
        docControlMapper.updateById(entity);
        log.debug("更新文档控制配置成功，用户: {}, 文档: {}", userId, docId);
        return entity;
    }

    @Override
    public boolean deleteByUserId(String userId) {
        int count = docControlMapper.deleteByUserId(userId);
        log.debug("删除用户控制配置，用户: {}, 删除数量: {}", userId, count);
        return count > 0;
    }

    @Override
    public boolean deleteByDocId(String docId) {
        int count = docControlMapper.deleteByDocId(docId);
        log.debug("删除文档控制配置，文档: {}, 删除数量: {}", docId, count);
        return count > 0;
    }

    @Override
    public DocControl convertToDocControl(DocControlEntity entity) {
        if (entity == null) {
            return null;
        }

        DocControl.DocControlBuilder builder = DocControl.builder();
        
        // 解析权限配置
        if (StringUtils.isNotBlank(entity.getPermissionsJson())) {
            try {
                DocPermission permissions = JSON.parseObject(entity.getPermissionsJson(), DocPermission.class);
                builder.docPermission(permissions);
            } catch (Exception e) {
                log.warn("解析权限配置失败: {}", e.getMessage());
            }
        }
        
        // 解析扩展配置
        if (StringUtils.isNotBlank(entity.getExtensionJson())) {
            try {
                DocExtension extension = JSON.parseObject(entity.getExtensionJson(), DocExtension.class);
                builder.extension(extension);
            } catch (Exception e) {
                log.warn("解析扩展配置失败: {}", e.getMessage());
            }
        }
        
        // 解析水印配置
        if (StringUtils.isNotBlank(entity.getWatermarkJson())) {
            try {
                DocWaterMark watermark = JSON.parseObject(entity.getWatermarkJson(), DocWaterMark.class);
                builder.docWaterMark(watermark);
            } catch (Exception e) {
                log.warn("解析水印配置失败: {}", e.getMessage());
            }
        }
        
        // 设置角色
        if (StringUtils.isNotBlank(entity.getRole())) {
            builder.role(entity.getRole());
        }
        
        return builder.build();
    }

    @Override
    public DocControlEntity convertToEntity(String userId, String docId, DocControl docControl) {
        if (docControl == null) {
            return DocControlEntity.builder()
                    .userId(userId)
                    .docId(docId)
                    .build();
        }

        DocControlEntity.DocControlEntityBuilder builder = DocControlEntity.builder()
                .userId(userId)
                .docId(docId);
        
        // 序列化权限配置
        if (docControl.getDocPermission() != null) {
            try {
                String permissionsJson = JSON.toJSONString(docControl.getDocPermission());
                builder.permissionsJson(permissionsJson);
            } catch (Exception e) {
                log.warn("序列化权限配置失败: {}", e.getMessage());
            }
        }
        
        // 序列化扩展配置
        if (docControl.getExtension() != null) {
            try {
                String extensionJson = JSON.toJSONString(docControl.getExtension());
                builder.extensionJson(extensionJson);
            } catch (Exception e) {
                log.warn("序列化扩展配置失败: {}", e.getMessage());
            }
        }
        
        // 序列化水印配置
        if (docControl.getDocWaterMark() != null) {
            try {
                String watermarkJson = JSON.toJSONString(docControl.getDocWaterMark());
                builder.watermarkJson(watermarkJson);
            } catch (Exception e) {
                log.warn("序列化水印配置失败: {}", e.getMessage());
            }
        }
        
        // 设置角色
        if (StringUtils.isNotBlank(docControl.getRole())) {
            builder.role(docControl.getRole());
        }
        
        return builder.build();
    }
}
