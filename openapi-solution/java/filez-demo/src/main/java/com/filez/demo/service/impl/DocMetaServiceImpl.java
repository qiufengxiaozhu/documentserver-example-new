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
 * 文档元数据服务实现类
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
        log.info("保存文档元数据成功，ID: {}", entity.getId());
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
        log.info("更新文档元数据成功，ID: {}", entity.getId());
        return entity;
    }

    @Override
    public boolean deleteDocMetaById(String id) {
        int result = docMetaMapper.deleteById(id);
        log.info("删除文档元数据，ID: {}, 结果: {}", id, result > 0 ? "成功" : "失败");
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

        // 构建创建人信息
        if (entity.getCreatedById() != null) {
            Profile createdBy = Profile.builder()
                    .id(entity.getCreatedById())
                    .name(entity.getCreatedByName())
                    .displayName(entity.getCreatedByName())
                    .email(entity.getCreatedByEmail())
                    .build();
            builder.createdBy(createdBy);
        }

        // 构建修改人信息
        if (entity.getModifiedById() != null) {
            Profile modifiedBy = Profile.builder()
                    .id(entity.getModifiedById())
                    .name(entity.getModifiedByName())
                    .displayName(entity.getModifiedByName())
                    .email(entity.getModifiedByEmail())
                    .build();
            builder.modifiedBy(modifiedBy);
        }

        // 构建拥有者信息
        if (entity.getOwnerId() != null) {
            Profile owner = Profile.builder()
                    .id(entity.getOwnerId())
                    .name(entity.getOwnerName())
                    .displayName(entity.getOwnerName())
                    .email(entity.getOwnerEmail())
                    .build();
            builder.owner(owner);
        }

        // 注意：权限、扩展、水印配置现在存储在DocControl表中
        // 如果需要这些信息，请使用DocControlService关联查询

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

        // 处理创建人信息
        if (docMeta.getCreatedBy() != null) {
            builder.createdById(docMeta.getCreatedBy().getId())
                    .createdByName(docMeta.getCreatedBy().getName())
                    .createdByEmail(docMeta.getCreatedBy().getEmail());
        }

        // 处理修改人信息
        if (docMeta.getModifiedBy() != null) {
            builder.modifiedById(docMeta.getModifiedBy().getId())
                    .modifiedByName(docMeta.getModifiedBy().getName())
                    .modifiedByEmail(docMeta.getModifiedBy().getEmail());
        }

        // 处理拥有者信息
        if (docMeta.getOwner() != null) {
            builder.ownerId(docMeta.getOwner().getId())
                    .ownerName(docMeta.getOwner().getName())
                    .ownerEmail(docMeta.getOwner().getEmail());
        }

        // 注意：权限、扩展、水印配置现在存储在DocControl表中，不再存储在DocMeta中

        return builder.build();
    }
}
