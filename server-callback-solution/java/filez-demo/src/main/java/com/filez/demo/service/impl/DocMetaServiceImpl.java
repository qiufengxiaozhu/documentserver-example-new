package com.filez.demo.service.impl;

import com.filez.demo.dao.DocMetaMapper;
import com.filez.demo.entity.DocMetaEntity;
import com.filez.demo.model.*;
import com.filez.demo.service.DocMetaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Document metadata service implementation class
 */
@Slf4j
@Service
public class DocMetaServiceImpl implements DocMetaService {

    @Resource
    private DocMetaMapper docMetaMapper;

    @Override
    public DocMetaEntity saveDocMeta(DocMeta docMeta) {
        DocMetaEntity entity = convertToEntity(docMeta);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        
        docMetaMapper.insert(entity);
        log.info("Document metadata saved successfully, ID: {}", entity.getId());
        return entity;
    }

    @Override
    public DocMetaEntity getDocMetaById(String id) {
        return docMetaMapper.selectById(id);
    }

    @Override
    public DocMetaEntity getDocMetaByName(String name) {
        return docMetaMapper.selectByName(name);
    }

    @Override
    public DocMetaEntity getDocMetaByFilepath(String filepath) {
        return docMetaMapper.selectByFilepath(filepath);
    }

    @Override
    public List<DocMetaEntity> getAllDocMetas() {
        return docMetaMapper.selectAllOrderByModifiedAt();
    }

    @Override
    public DocMetaEntity updateDocMeta(DocMeta docMeta) {
        DocMetaEntity entity = convertToEntity(docMeta);
        entity.setUpdateTime(new Date());
        
        docMetaMapper.updateById(entity);
        log.info("Document metadata updated successfully, ID: {}", entity.getId());
        return entity;
    }

    @Override
    public boolean deleteDocMetaById(String id) {
        int result = docMetaMapper.deleteById(id);
        log.info("Delete document metadata, ID: {}, Result: {}", id, result > 0 ? "Success" : "Failed");
        return result > 0;
    }

    @Override
    public List<DocMetaEntity> getDocMetasByCreatedById(String createdById) {
        return docMetaMapper.selectByCreatedById(createdById);
    }

    @Override
    public DocMeta convertToDocMeta(DocMetaEntity entity) {
        if (entity == null) {
            return null;
        }

        DocMeta.DocMetaBuilder builder = DocMeta.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .modifiedAt(entity.getModifiedAt())
                .size(entity.getSize())
                .version(entity.getVersion())
                .filepath(entity.getFilepath())
                .role(entity.getRole());

        // Build creator information
        if (entity.getCreatedById() != null) {
            Profile createdBy = Profile.builder()
                    .id(entity.getCreatedById())
                    .name(entity.getCreatedByName())
                    .displayName(entity.getCreatedByName())
                    .email(entity.getCreatedByEmail())
                    .build();
            builder.createdBy(createdBy);
        }

        // Build modifier information
        if (entity.getModifiedById() != null) {
            Profile modifiedBy = Profile.builder()
                    .id(entity.getModifiedById())
                    .name(entity.getModifiedByName())
                    .displayName(entity.getModifiedByName())
                    .email(entity.getModifiedByEmail())
                    .build();
            builder.modifiedBy(modifiedBy);
        }

        // Build owner information
        if (entity.getOwnerId() != null) {
            Profile owner = Profile.builder()
                    .id(entity.getOwnerId())
                    .name(entity.getOwnerName())
                    .displayName(entity.getOwnerName())
                    .email(entity.getOwnerEmail())
                    .build();
            builder.owner(owner);
        }

        // Note: permissions, extension, watermark configurations are now stored in DocControl table
        // If you need this information, please use DocControlService for associated queries

        return builder.build();
    }

    @Override
    public DocMetaEntity convertToEntity(DocMeta docMeta) {
        if (docMeta == null) {
            return null;
        }

        DocMetaEntity.DocMetaEntityBuilder builder = DocMetaEntity.builder()
                .id(docMeta.getId())
                .name(docMeta.getName())
                .description(docMeta.getDescription())
                .createdAt(docMeta.getCreatedAt())
                .modifiedAt(docMeta.getModifiedAt())
                .size(docMeta.getSize())
                .version(docMeta.getVersion())
                .filepath(docMeta.getFilepath())
                .role(docMeta.getRole());

        // Handle creator information
        if (docMeta.getCreatedBy() != null) {
            builder.createdById(docMeta.getCreatedBy().getId())
                    .createdByName(docMeta.getCreatedBy().getName())
                    .createdByEmail(docMeta.getCreatedBy().getEmail());
        }

        // Handle modifier information
        if (docMeta.getModifiedBy() != null) {
            builder.modifiedById(docMeta.getModifiedBy().getId())
                    .modifiedByName(docMeta.getModifiedBy().getName())
                    .modifiedByEmail(docMeta.getModifiedBy().getEmail());
        }

        // Handle owner information
        if (docMeta.getOwner() != null) {
            builder.ownerId(docMeta.getOwner().getId())
                    .ownerName(docMeta.getOwner().getName())
                    .ownerEmail(docMeta.getOwner().getEmail());
        }

        // Note: permissions, extension, watermark configurations are now stored in DocControl table, no longer stored in DocMeta

        return builder.build();
    }
}
