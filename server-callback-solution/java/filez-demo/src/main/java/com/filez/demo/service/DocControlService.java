package com.filez.demo.service;

import com.filez.demo.entity.DocControlEntity;
import com.filez.demo.model.DocControl;

/**
 * Document control configuration service interface
 */
public interface DocControlService {

    /**
     * Save document control configuration
     * @param userId User ID
     * @param docId Document ID
     * @param docControl Control configuration
     * @return Saved configuration entity
     */
    DocControlEntity saveDocControl(String userId, String docId, DocControl docControl);

    /**
     * Get document control configuration
     * @param userId User ID
     * @param docId Document ID
     * @return Control configuration
     */
    DocControl getDocControl(String userId, String docId);

    /**
     * Update document control configuration
     * @param userId User ID
     * @param docId Document ID
     * @param docControl Control configuration
     * @return Updated configuration entity
     */
    DocControlEntity updateDocControl(String userId, String docId, DocControl docControl);

    /**
     * Delete all control configurations for user
     * @param userId User ID
     * @return Whether deletion was successful
     */
    boolean deleteByUserId(String userId);

    /**
     * Delete all control configurations for document
     * @param docId Document ID
     * @return Whether deletion was successful
     */
    boolean deleteByDocId(String docId);

    /**
     * Convert database entity to business model
     * @param entity Database entity
     * @return Business model
     */
    DocControl convertToDocControl(DocControlEntity entity);

    /**
     * Convert business model to database entity
     * @param userId User ID
     * @param docId Document ID
     * @param docControl Business model
     * @return Database entity
     */
    DocControlEntity convertToEntity(String userId, String docId, DocControl docControl);
}
