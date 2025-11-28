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
 * Document control configuration service implementation class
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
        log.debug("Document control configuration saved successfully, User: {}, Document: {}", userId, docId);
        return entity;
    }

    @Override
    public DocControl getDocControl(String userId, String docId) {
        DocControlEntity entity = docControlMapper.selectByUserIdAndDocId(userId, docId);
        if (entity == null) {
            log.debug("Document control configuration not found, User: {}, Document: {}", userId, docId);
            return null;
        }
        return convertToDocControl(entity);
    }

    @Override
    public DocControlEntity updateDocControl(String userId, String docId, DocControl docControl) {
        DocControlEntity existingEntity = docControlMapper.selectByUserIdAndDocId(userId, docId);
        
        if (existingEntity == null) {
            // If not exists, create new one
            return saveDocControl(userId, docId, docControl);
        }
        
        // Update existing configuration
        DocControlEntity entity = convertToEntity(userId, docId, docControl);
        entity.setId(existingEntity.getId());
        entity.setCreateTime(existingEntity.getCreateTime());
        entity.setUpdateTime(new Date());
        
        docControlMapper.updateById(entity);
        log.debug("Document control configuration updated successfully, User: {}, Document: {}", userId, docId);
        return entity;
    }

    @Override
    public boolean deleteByUserId(String userId) {
        int count = docControlMapper.deleteByUserId(userId);
        log.debug("Delete user control configuration, User: {}, Deleted count: {}", userId, count);
        return count > 0;
    }

    @Override
    public boolean deleteByDocId(String docId) {
        int count = docControlMapper.deleteByDocId(docId);
        log.debug("Delete document control configuration, Document: {}, Deleted count: {}", docId, count);
        return count > 0;
    }

    @Override
    public DocControl convertToDocControl(DocControlEntity entity) {
        if (entity == null) {
            return null;
        }

        DocControl.DocControlBuilder builder = DocControl.builder();
        
        // Parse permissions configuration
        if (StringUtils.isNotBlank(entity.getPermissionsJson())) {
            try {
                DocPermission permissions = JSON.parseObject(entity.getPermissionsJson(), DocPermission.class);
                builder.docPermission(permissions);
            } catch (Exception e) {
                log.warn("Failed to parse permissions configuration: {}", e.getMessage());
            }
        }
        
        // Parse extension configuration
        if (StringUtils.isNotBlank(entity.getExtensionJson())) {
            try {
                DocExtension extension = JSON.parseObject(entity.getExtensionJson(), DocExtension.class);
                builder.extension(extension);
            } catch (Exception e) {
                log.warn("Failed to parse extension configuration: {}", e.getMessage());
            }
        }
        
        // Parse watermark configuration
        if (StringUtils.isNotBlank(entity.getWatermarkJson())) {
            try {
                DocWaterMark watermark = JSON.parseObject(entity.getWatermarkJson(), DocWaterMark.class);
                builder.docWaterMark(watermark);
            } catch (Exception e) {
                log.warn("Failed to parse watermark configuration: {}", e.getMessage());
            }
        }
        
        // Set role
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
        
        // Serialize permissions configuration
        if (docControl.getDocPermission() != null) {
            try {
                String permissionsJson = JSON.toJSONString(docControl.getDocPermission());
                builder.permissionsJson(permissionsJson);
            } catch (Exception e) {
                log.warn("Failed to serialize permissions configuration: {}", e.getMessage());
            }
        }
        
        // Serialize extension configuration
        if (docControl.getExtension() != null) {
            try {
                String extensionJson = JSON.toJSONString(docControl.getExtension());
                builder.extensionJson(extensionJson);
            } catch (Exception e) {
                log.warn("Failed to serialize extension configuration: {}", e.getMessage());
            }
        }
        
        // Serialize watermark configuration
        if (docControl.getDocWaterMark() != null) {
            try {
                String watermarkJson = JSON.toJSONString(docControl.getDocWaterMark());
                builder.watermarkJson(watermarkJson);
            } catch (Exception e) {
                log.warn("Failed to serialize watermark configuration: {}", e.getMessage());
            }
        }
        
        // Set role
        if (StringUtils.isNotBlank(docControl.getRole())) {
            builder.role(docControl.getRole());
        }
        
        return builder.build();
    }
}
