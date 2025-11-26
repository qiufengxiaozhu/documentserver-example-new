package com.filez.demo.service.impl;

import com.filez.demo.common.constant.DocConstant;
import com.filez.demo.common.context.UserContext;
import com.filez.demo.common.listener.FileMonitor;
import com.filez.demo.entity.DocMetaEntity;
import com.filez.demo.entity.SysUserEntity;
import com.filez.demo.model.DocControl;
import com.filez.demo.model.DocExtension;
import com.filez.demo.model.DocMeta;
import com.filez.demo.model.DocPermission;
import com.filez.demo.model.DocWaterMark;
import com.filez.demo.model.Profile;
import com.filez.demo.service.DocControlService;
import com.filez.demo.service.DocMetaService;
import com.filez.demo.service.DocService;
import com.filez.demo.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Document service core implementation
 */
@Service
@Slf4j
@DependsOn("databaseConfig")
public class DocServiceImpl implements FileAlterationListener, DocService {

	/** Local file storage path */
	private final String localDir = System.getProperty("user.dir") + File.separator + DocConstant.LOCAL_FILE_DIR;
	/** File monitor */
    private FileMonitor monitor;
	/** Local file list */
    private File local;

    @Resource
    private SysUserService sysUserService;
    @Resource
    private DocMetaService docMetaService;
    @Resource
    private DocControlService docControlService;

    @PostConstruct
    public void init() {
        local = new File(localDir);
        if (!local.exists() && !local.mkdirs()) {
            log.error("Failed to create directory {}", local.getAbsolutePath());
            return;
        }
        copyInternalLocalFileTo(local);
        refreshFile();
        if (monitor == null) {
            startFileMonitor(local);
        }
    }

    private void startFileMonitor(File file) {
        monitor = new FileMonitor(1000);
        monitor.monitor(file.getAbsolutePath(), this);
        try {
            monitor.start();
        } catch (Exception e) {
            log.error("start monitor error {}", e.getMessage());
        }
    }

    private void refreshFile() {
        try {
            File[] files = local.listFiles();
            if (files == null) return;
            
            for (File file : files) {
                if (file.isDirectory()) {
                    syncUserDirectory(file);
                } else {
                    syncFile(file);
                }
            }
            
            // Clean up file records that don't exist in database
            cleanupDeletedFiles();
        } catch (Exception e) {
            log.warn("File synchronization failed: {}", e.getMessage());
        }
    }

    /**
     * Synchronize single file to database
     */
    private void syncFile(File file) {
        String fileName = file.getName();
        DocMetaEntity existing = docMetaService.getDocMetaByName(fileName);
        
        if (existing != null) {
            // Check if file has been modified
            if (file.lastModified() > existing.getModifiedAt().getTime()) {
                DocMeta docMeta = docMetaService.convertToDocMeta(existing);
                docMeta.setModifiedAt(new Date(file.lastModified()));
                docMeta.setSize(file.length());
                docMetaService.updateDocMeta(docMeta);
                log.debug("Update file metadata: {}", fileName);
            }
        } else {
            // New file, create record
            createDocMetaForFile(file);
        }
    }

    /**
     * Synchronize user directory
     */
    private void syncUserDirectory(File userDir) {
        String userId = userDir.getName();
        SysUserEntity user = sysUserService.getUserById(userId);
        
        if (user == null) {
            // User does not exist, delete directory
            try {
                FileUtils.deleteDirectory(userDir);
                log.info("Delete invalid user directory: {}", userDir.getName());
            } catch (IOException e) {
                log.error("Failed to delete directory: {}", userDir.getAbsolutePath(), e);
            }
            return;
        }

        File[] userFiles = userDir.listFiles(File::isFile);
        if (userFiles != null) {
            for (File file : userFiles) {
                syncFile(file);
            }
        }
    }

    /**
     * Clean up deleted file records in database
     */
    private void cleanupDeletedFiles() {
        try {
            List<DocMetaEntity> allDocs = docMetaService.getAllDocMetas();
            for (DocMetaEntity doc : allDocs) {
                File file = new File(doc.getFilepath());
                if (!file.exists()) {
                    docMetaService.deleteDocMetaById(doc.getId());
                    docControlService.deleteByDocId(doc.getId());
                    log.info("Clean up deleted file record: {}", doc.getName());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to clean up deleted file records: {}", e.getMessage());
        }
    }

    /**
     * Create DocMeta record for file
     */
    private void createDocMetaForFile(File file) {
        SysUserEntity currentUser = UserContext.getCurrentUser();
        SysUserEntity user;
        
        if (currentUser == null) {
            // If there is no current user (e.g., during application startup), use default admin user
            List<SysUserEntity> allUsers = sysUserService.getAllUser();
            if (allUsers.isEmpty()) {
                log.warn("No users in system, skip file: {}", file.getName());
                return;
            }
            user = allUsers.get(0); // Use first user as default user
            log.info("Using default user {} to create file record: {}", user.getName(), file.getName());
        } else {
            user = sysUserService.getUserById(currentUser.getId());
        }

        String docId = getDocId(file.getName());
        
        DocMeta docMeta = DocMeta.builder()
                .id(docId)
                .name(file.getName())
                .createdBy(Profile.convertUserToProfile(user))
                .modifiedAt(new Date(file.lastModified()))
                .permissions(new DocPermission())
                .filepath(file.getAbsolutePath())
                .size(file.length())
                .build();

        docMetaService.saveDocMeta(docMeta);
        log.info("Create new file record: {}", file.getName());
    }


    /**
     * Built-in some files for testing convenience
     */
    private void copyInternalLocalFileTo(File destDir) {
        String[] buildFiles = {
                "local-docx.docx",
                "local-pptx.pptx",
                "local-xlsx.xlsx"
        };
        try {
            for (String fileName : buildFiles) {
                // File path under classpath, classpath file paths should always use "/" separator
                String sourceFilePath = DocConstant.INTERNAL_LOCAL_FILE_DIR + "/" + fileName;
                File destFile = new File(destDir, fileName);
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                try (InputStream resourceAsStream = classLoader.getResourceAsStream(sourceFilePath)) {
		            FileUtils.copyInputStreamToFile(Objects.requireNonNull(resourceAsStream), destFile);
	            }
            }
        } catch (Exception e) {
            log.error("copy internal local file error: {}", e.getMessage());
        }
    }

    @Override
    public DocMeta findDocMetaById(String docId) {
        DocMetaEntity entity = docMetaService.getDocMetaById(docId);
        if (entity == null) {
            return null;
        }
        
        DocMeta docMeta = docMetaService.convertToDocMeta(entity);
        SysUserEntity currentUser = UserContext.getCurrentUser();
        if (currentUser == null) return docMeta;

        // Get user control configuration and merge directly into DocMeta
        DocControl control = this.getControl(currentUser.getId(), docId);
        if (control != null) {
            docMeta.setPermissions(control.getDocPermission());
            docMeta.setWaterMark(control.getDocWaterMark());
            docMeta.setExtension(control.getExtension());
            docMeta.setRole(control.getRole());
        }
        return docMeta;
    }

    @Override
    public InputStream getDocById(String docId) {
        DocMetaEntity entity = docMetaService.getDocMetaById(docId);
        if (entity == null) {
            log.warn("can not find docMeta of {}", docId);
            return null;
        }
        File file = new File(entity.getFilepath());
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                log.error("File does not exist {}", file.getAbsolutePath());
            }
        }
        return null;
    }

    @Override
    public DocMeta findDocMetaWithControlById(String docId, String userId) {
        // Get basic document metadata
        DocMeta docMeta = findDocMetaById(docId);
        if (docMeta == null) {
            return null;
        }

        // Get user's control configuration
        DocControl control = docControlService.getDocControl(userId, docId);
        if (control != null) {
            // Merge control configuration information into DocMeta
            if (control.getDocPermission() != null) {
                docMeta.setPermissions(control.getDocPermission());
            }
            if (control.getExtension() != null) {
                docMeta.setExtension(control.getExtension());
            }
            if (control.getDocWaterMark() != null) {
                docMeta.setWaterMark(control.getDocWaterMark());
            }
            if (control.getRole() != null) {
                docMeta.setRole(control.getRole());
            }
        }

        return docMeta;
    }

    @Override
    public List<DocMeta> listFiles() {
        // Simplified: query directly from database, no need for refreshFile
        List<DocMetaEntity> entities = docMetaService.getAllDocMetas();
        return entities.stream()
                .map(docMetaService::convertToDocMeta)
                .collect(Collectors.toList());
    }

    @Override
    public DocMeta uploadFile(String docId, InputStream inputStream) {

        String localSaveDir = System.getProperty("user.dir") + File.separator + DocConstant.LOCAL_FILE_SAVE_DIR;
        File file = new File(localSaveDir);
        if (!file.exists() && !file.mkdirs()) {
            log.error("Failed to create save directory when uploading file");
            return null;
        }

        DocMetaEntity entity = docMetaService.getDocMetaById(docId);
        if (entity == null) {
            log.error("Cannot find document metadata, docId: {}", docId);
            return null;
        }
        
        DocMeta docMeta = docMetaService.convertToDocMeta(entity);
        File doc = new File(docMeta.getFilepath());
        
        // If file already exists, backup old version first
        if (doc.exists() && doc.length() > 0) {
            File oldFile = new File(localSaveDir + File.separator + new Date().getTime() + DocConstant.DOC_ID_CONNECT + doc.getName());
            try {
                FileUtils.moveFile(doc, oldFile);
                log.info("Backup old version file: {}", oldFile.getName());
            } catch (IOException e) {
                log.error("Failed to backup file, reason: {}", e.getMessage());
            }
        }

        try (FileOutputStream fos = new FileOutputStream(doc)) {
            IOUtils.copy(inputStream, fos);
            docMeta.setModifiedAt(new Date(doc.lastModified()));
            docMeta.setSize(doc.length());
            // Update database
            docMetaService.updateDocMeta(docMeta);
        } catch (IOException e) {
            log.warn("File save failed, reason: {}", e.getMessage());
        }
        return docMeta;
    }

    /**
     * Create new file
     * @param name Filename
     * @param path Directory
     */
    @Override
    public DocMeta makeNewFile(String name, String path) throws IOException {

        // 1. Check if saving file at specified location is allowed
        SysUserEntity user = UserContext.getCurrentUser();
        if (user == null) {
            log.error("Currently unable to obtain your identity information, so cannot save file");
            return null;
        }
        if (StringUtils.isEmpty(path)) {
            path = UserContext.getCurrentUser().getId();
        }

        // Check if file with same name already exists in database
        DocMetaEntity existingEntity = docMetaService.getDocMetaByName(name);
        if (existingEntity != null) {
            log.warn("File with same name already exists in database: {}", name);
            return null;
        }

        // Create file under local-file/userId
        Path filePath = Paths.get(System.getProperty("user.dir"), DocConstant.LOCAL_FILE_DIR, path, name);
        File file = filePath.toFile();
        if (file.exists()) {
            log.warn("File already exists in filesystem: {}", file.getAbsolutePath());
            return null;
        }

        // 2. Create empty file
        String id = getDocId(name);
        Profile profile = Profile.convertUserToProfile(user);
        if (profile == null) {
            log.error("Unable to get current user's configuration information");
            return null;
        }
        DocMeta docMeta = new DocMeta(id, name, profile, new Date(), new DocPermission());
        docMeta.setFilepath(file.getAbsolutePath());

        FileUtils.writeStringToFile(file, "", "utf-8", false);
        docMeta.setSize(file.length());

        // Save to database
        docMetaService.saveDocMeta(docMeta);

        return docMeta;
    }

    private String getDocId(String name) {
        if (name.toLowerCase().startsWith("local")) {
            return FilenameUtils.getBaseName(name);
        }
        return "local-" + UUID.randomUUID();
    }

    /**
     * Set document control permissions
     */
    public DocControl getControl(String userId, String fileId) {
        DocControl control = docControlService.getDocControl(userId, fileId);
        
        if (control == null) {
            // Create default configuration
            DocPermission defaultPermission = new DocPermission();
            // Set document download and print permissions
            // defaultPermission.setDownload(true);
            // defaultPermission.setPrint(true);
            DocWaterMark defaultWatermark = new DocWaterMark();
            DocExtension defaultDocExtension = new DocExtension();
            // Enable forced revision mode for doc documents
            // defaultDocExtension.setTrackChangeForceOn(true);
            DocControl defaultControl = new DocControl();
            defaultControl.setDocPermission(defaultPermission);
            defaultControl.setDocWaterMark(defaultWatermark);
            defaultControl.setExtension(defaultDocExtension);
            // defaultControl.setRole("commenter");
            
            // Save default configuration to database
            try {
                docControlService.saveDocControl(userId, fileId, defaultControl);
                log.debug("Create default control configuration for user {} document {}", userId, fileId);
            } catch (Exception e) {
                log.warn("Failed to save default control configuration: {}", e.getMessage());
            }
            
            return defaultControl;
        }
        
        return control;
    }

    public void updateControl(String userId, String fileId, DocControl controlVO) {
        try {
            docControlService.updateDocControl(userId, fileId, controlVO);
            log.debug("Successfully updated control configuration for user {} document {}", userId, fileId);
        } catch (Exception e) {
            log.error("Failed to update control configuration, user: {}, document: {}, error: {}", userId, fileId, e.getMessage());
        }
    }

    @Override
    public DocMeta deleteFileByDocId(String docId) {
        DocMetaEntity entity = docMetaService.getDocMetaById(docId);
        if (entity == null) {
            log.warn("Document metadata not found, docId: {}", docId);
            return null;
        }
        
        DocMeta docMeta = docMetaService.convertToDocMeta(entity);
        String filepath = docMeta.getFilepath();
        if (StringUtils.isEmpty(filepath)) {
            // Concatenate path for deletion, file path is localDir concatenated with filename
            filepath = localDir + File.separator + docMeta.getName();
        }
        File file = new File(filepath);
        if (!file.exists()) {
            log.info("File not found {}", file.getName());
            // Even if file doesn't exist, still delete record from database
            docMetaService.deleteDocMetaById(docId);
            return docMeta;
        }
        log.info("File found, preparing to delete file {}", file.getName());
        if (file.delete()) {
            log.info("File deleted successfully {}", file.getName());
        }
        // If file no longer exists, remove related data from database
        if (!file.exists()) {
            docMetaService.deleteDocMetaById(docId);
            // Also delete document control configuration
            try {
                docControlService.deleteByDocId(docId);
                log.debug("Document control configuration deleted successfully, ID: {}", docId);
            } catch (Exception e) {
                log.warn("Failed to delete document control configuration, ID: {}, error: {}", docId, e.getMessage());
            }
        }

        return docMeta;
    }

    /**
     * Create file
     * @param file The file created
     */
    @Override
    public void onFileCreate(File file) {
        log.info("File [{}] has been created", file.getName());
    }

    /***
     * File modification
     * @param file The file modified
     */
    @Override
    public void onFileChange(File file) {
        String fileName = file.getName();
        log.info("File [{}] was modified, path: {}, size: {} bytes", fileName, file.getAbsolutePath(), file.length());

        DocMetaEntity entity = docMetaService.getDocMetaByName(fileName);
        if (entity != null) {
            DocMeta docMeta = docMetaService.convertToDocMeta(entity);
            docMeta.setModifiedAt(new Date(file.lastModified()));
            docMeta.setSize(file.length());
            docMetaService.updateDocMeta(docMeta);
            log.info("File [{}] modification time updated", fileName);
        }
    }

    @Override
    public DocMeta updateDocMeta(DocMeta docMeta) {
        docMetaService.updateDocMeta(docMeta);
        return findDocMetaById(docMeta.getId());
    }

    /**
     * Delete file
     * @param file The file deleted
     */
    @Override
    public void onFileDelete(File file) {
        log.info("File [{}] has been deleted", file.getName());
        listFiles();
    }

    @Override
    public void onStart(FileAlterationObserver fileAlterationObserver) { }

    @Override
    public void onDirectoryCreate(File file) { }

    @Override
    public void onDirectoryChange(File file) { }

    @Override
    public void onDirectoryDelete(File file) { }

    @Override
    public void onStop(FileAlterationObserver fileAlterationObserver) { }

    @Override
    public boolean isAllowedAccess(String docId) {
        return true;
    }

}
