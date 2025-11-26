package com.filez.demo.service;

import com.filez.demo.model.DocControl;
import com.filez.demo.model.DocMeta;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DocService {

    /**
     * Download file by file ID
     */
    InputStream getDocById(String docId);

    /**
     * Get document information by document ID
     */
    DocMeta findDocMetaById(String docId);

    /**
     * Get complete document information by document ID and user ID (including user control configuration)
     */
    DocMeta findDocMetaWithControlById(String docId, String userId);

    /**
     * Update document metadata
     */
    DocMeta updateDocMeta(DocMeta docMeta);

    /**
     * Get file list
     */
    List<DocMeta> listFiles();

    /**
     * Upload file
     */
    DocMeta uploadFile(String docId, InputStream inputStream);
    /**
     * Delete file by document ID
     */
    DocMeta deleteFileByDocId(String docId);

    /**
     * Create empty file at specified path
     */
    DocMeta makeNewFile(String name, String path) throws IOException;

    /**
     * Check if user has permission to access the corresponding file ID
     */
    boolean isAllowedAccess(String docId);

    /**
     * Get user control functions
     */
    DocControl getControl(String userId, String fileId);

    /**
     * Update user control functions
     */
    void updateControl(String userId, String fileId, DocControl controlVO);
}
