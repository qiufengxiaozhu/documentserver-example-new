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
@RequestMapping("${demo.context}/file") // 默认值：/v2/context/file
@Api(tags = "文件控制器控制器")
public class FileController {

    @Resource
    private DocService docService;

    @Log("上传文件")
    @ApiOperation(value = "/v2/context/file/upload,上传文件")
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
            return ResponseUtil.badRequest("该文件已存在，请勿重复上传！");
        }
        DocMeta meta = docService.uploadFile(docMeta.getId(), file.getInputStream());
        if (meta == null) {
            return ResponseUtil.badRequest("upload fail");
        }

        return ResponseEntity.ok(docMeta);
    }

    @Log("删除指定文件")
    @ApiOperation(value = "/v2/context/file/delete/{docId},删除指定文件")
    @RequestMapping("/delete/{docId}")
    public ResponseEntity<?> deleteDoc(@PathVariable String docId) {

        DocMeta docMeta = docService.findDocMetaById(docId);
        if (docMeta == null) {
            return ResponseUtil.badRequest("can not find file");
        }
        DocMeta deleteDocMeta = docService.deleteFileByDocId(docId);
        return ResponseEntity.ok(deleteDocMeta);
    }

    @Log("批量删除指定文件")
    @ApiOperation(value = "/v2/context/file/batchOp/delete,批量删除指定文件")
    @PostMapping("/batchOp/delete")
    public ResponseEntity<List<String>> batchDelete(@RequestBody String[] fileIds) {
        // List集合记录每次删除的结果
        List<String> deleteResults = new ArrayList<>();
        // 遍历fileIds，删除文件
        for (String fileId : fileIds) {
            DocMeta docMeta = docService.findDocMetaById(fileId);
            if (docMeta == null) {
                deleteResults.add("删除" + fileId + "失败");
                continue;
            }
            DocMeta meta = docService.deleteFileByDocId(fileId);
            // meta不为空，说明删除成功，否则删除失败
            deleteResults.add(String.format("删除%s(id:%s)%s", meta.getName(), fileId, Objects.nonNull(meta) ? "成功" : "失败"));
        }
        return ResponseEntity.ok(deleteResults);
    }

    @Log("批量上传文件")
    @ApiOperation(value = "/v2/context/file/batchOp/upload,批量上传文件")
    @PostMapping("/batchOp/upload")
    public ResponseEntity<List<String>> batchUpload(@RequestParam("files") MultipartFile[] files) throws IOException {

        // List集合记录每次上传的结果
        List<String> uploadResults = new ArrayList<>();
        // 遍历files，上传文件
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                uploadResults.add("上传" + file.getOriginalFilename() + "失败");
                continue;
            }
            log.info("upload doc {}", file.getOriginalFilename());
            String name = file.getOriginalFilename();
            String path = "";
            DocMeta docMeta = docService.makeNewFile(name, path);
            if (docMeta == null) {
                uploadResults.add("上传" + file.getOriginalFilename() + "失败");
                continue;
            }
            docService.uploadFile(docMeta.getId(), file.getInputStream());
            uploadResults.add("上传" + file.getOriginalFilename() + "成功");
        }
        return ResponseEntity.ok(uploadResults);
    }

    @Log("新建文件")
    @ApiOperation(value = "/v2/context/file/new,新建文件")
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
        // 强制刷新缓存
        docService.listFiles();
        DocMeta docMeta = docService.makeNewFile(name, "");
        if (docMeta == null) {
            log.warn("preflight make new file fail");
            return ResponseEntity.badRequest().body("创建失败，文件名冲突");
        }
        String filepath = DocConstant.TEMPLATE_FILE_DIR + File.separator + templateName;
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filepath)) {
            DocMeta meta = docService.uploadFile(docMeta.getId(), is);
            if (meta == null) {
                return ResponseEntity.badRequest().body("创建新文件失败");
            }
        }

        return ResponseEntity.ok(docMeta);
    }
}
