package com.filez.demo.service;

import com.filez.demo.entity.DocMetaEntity;
import com.filez.demo.model.DocMeta;

import java.util.List;

/**
 * Document metadata service interface
 */
public interface DocMetaService {

    /**
     * Save document metadata
     * @param docMeta Document metadata
     * @return Saved document metadata
     */
    DocMetaEntity saveDocMeta(DocMeta docMeta);

    /**
     * Get document metadata by ID
     * @param id Document ID
     * @return Document metadata
     */
    DocMetaEntity getDocMetaById(String id);

    /**
     * Get document metadata by filename
     * @param name Filename
     * @return Document metadata
     */
    DocMetaEntity getDocMetaByName(String name);

    /**
     * Get document metadata by file path
     * @param filepath File path
     * @return Document metadata
     */
    DocMetaEntity getDocMetaByFilepath(String filepath);

    /**
     * Get all document metadata
     * @return Document metadata list
     */
    List<DocMetaEntity> getAllDocMetas();

    /**
     * Update document metadata
     * @param docMeta Document metadata
     * @return Updated document metadata
     */
    DocMetaEntity updateDocMeta(DocMeta docMeta);

    /**
     * Delete document metadata by ID
     * @param id Document ID
     * @return Whether deletion was successful
     */
    boolean deleteDocMetaById(String id);

    /**
     * Convert database entity to business model
     * @param entity Database entity
     * @return Business model
     */
    DocMeta convertToDocMeta(DocMetaEntity entity);

    /**
     * Convert business model to database entity
     * @param docMeta Business model
     * @return Database entity
     */
    DocMetaEntity convertToEntity(DocMeta docMeta);

    /**
     * Get document metadata by creator ID
     * @param createdById Creator ID
     * @return Document metadata list
     */
    List<DocMetaEntity> getDocMetasByCreatedById(String createdById);
}
