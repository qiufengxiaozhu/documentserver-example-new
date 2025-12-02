package com.filez.demo.controller;

import com.filez.demo.common.aspect.Log;
import com.filez.demo.common.constant.DocConstant;
import com.filez.demo.common.utils.ResponseUtil;
import com.filez.demo.model.DocMeta;
import com.filez.demo.service.DocService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("${demo.context}/file") // Default value: /v2/context/file
@Api(tags = "File Controller")
public class FileController {

    @Resource
    private DocService docService;

    @Log("Upload file")
    @ApiOperation(value = "/v2/context/file/upload, Upload file")
    @RequestMapping("/upload")
    public ResponseEntity<?> uploadDoc( MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            log.info("empty file");
            return ResponseUtil.badRequest("empty file");
        }

        String name = file.getOriginalFilename();
        String path = "";
        DocMeta docMeta = docService.makeNewFile(name, path);
        if (docMeta == null) {
            return ResponseUtil.badRequest("File already exists, please do not upload duplicates!");
        }
        DocMeta meta = docService.uploadFile(docMeta.getId(), file.getInputStream());
        if (meta == null) {
            return ResponseUtil.badRequest("upload fail");
        }

        return ResponseEntity.ok(docMeta);
    }

    @Log("Delete specified file")
    @ApiOperation(value = "/v2/context/file/delete/{docId}, Delete specified file")
    @RequestMapping("/delete/{docId}")
    public ResponseEntity<?> deleteDoc(@PathVariable String docId) {

        DocMeta docMeta = docService.findDocMetaById(docId);
        if (docMeta == null) {
            return ResponseUtil.badRequest("can not find file");
        }
        DocMeta deleteDocMeta = docService.deleteFileByDocId(docId);
        return ResponseEntity.ok(deleteDocMeta);
    }

    @Log("Batch delete specified files")
    @ApiOperation(value = "/v2/context/file/batchOp/delete, Batch delete specified files")
    @PostMapping("/batchOp/delete")
    public ResponseEntity<List<String>> batchDelete(@RequestBody String[] fileIds) {
        // List collection records the result of each deletion
        List<String> deleteResults = new ArrayList<>();
        // Traverse fileIds and delete files
        for (String fileId : fileIds) {
            DocMeta docMeta = docService.findDocMetaById(fileId);
            if (docMeta == null) {
                deleteResults.add("Delete " + fileId + " failed");
                continue;
            }
            DocMeta meta = docService.deleteFileByDocId(fileId);
            // If meta is not empty, it means deletion is successful, otherwise deletion failed
            deleteResults.add(String.format("Delete %s(id:%s) %s", meta.getName(), fileId, Objects.nonNull(meta) ? "success" : "failed"));
        }
        return ResponseEntity.ok(deleteResults);
    }

    @Log("Batch upload files")
    @ApiOperation(value = "/v2/context/file/batchOp/upload, Batch upload files")
    @PostMapping("/batchOp/upload")
    public ResponseEntity<List<String>> batchUpload(@RequestParam("files") MultipartFile[] files) throws IOException {

        // List collection records the result of each upload
        List<String> uploadResults = new ArrayList<>();
        // Traverse files and upload files
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                uploadResults.add("Upload " + file.getOriginalFilename() + " failed");
                continue;
            }
            log.info("upload doc {}", file.getOriginalFilename());
            String name = file.getOriginalFilename();
            String path = "";
            DocMeta docMeta = docService.makeNewFile(name, path);
            if (docMeta == null) {
                uploadResults.add("Upload " + file.getOriginalFilename() + " failed");
                continue;
            }
            docService.uploadFile(docMeta.getId(), file.getInputStream());
            uploadResults.add("Upload " + file.getOriginalFilename() + " success");
        }
        return ResponseEntity.ok(uploadResults);
    }

    @Log("Create new file")
    @ApiOperation(value = "/v2/context/file/new, Create new file")
    @PostMapping("/new")
    public ResponseEntity<?> newFile(@RequestBody Map<String, String> map) throws IOException {
        String docType = map.get("docType");
        String templateName = map.get("templateName");
        String filename = map.get("filename");

        if (StringUtils.isEmpty(filename)) {
            filename = "new";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String name = String.format("%s-%s.%s", dateFormat.format(new Date()), filename, docType);
        // Force refresh cache
        docService.listFiles();
        DocMeta docMeta = docService.makeNewFile(name, "");
        if (docMeta == null) {
            log.warn("preflight make new file fail");
            return ResponseEntity.badRequest().body("Creation failed, filename conflict");
        }
        String filepath = DocConstant.TEMPLATE_FILE_DIR + File.separator + templateName;
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filepath)) {
            DocMeta meta = docService.uploadFile(docMeta.getId(), is);
            if (meta == null) {
                return ResponseEntity.badRequest().body("Failed to create new file");
            }
        }

        return ResponseEntity.ok(docMeta);
    }
}
