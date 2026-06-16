package com.filez.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.filez.demo.common.utils.PublicApiAuthUtil;
import com.filez.demo.config.DemoConfig;
import com.filez.demo.config.PublicApiConfig;
import com.filez.demo.config.ZOfficeConfig;
import com.filez.demo.dao.OpenApiTaskMapper;
import com.filez.demo.entity.OpenApiTaskEntity;
import com.filez.demo.model.DocMeta;
import com.filez.demo.service.DocService;
import com.filez.demo.service.OpenApiTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAPI 通用任务服务实现
 * 负责对接 luoshu-server 的 PublicAPI 接口
 */
@Slf4j
@Service
public class OpenApiTaskServiceImpl implements OpenApiTaskService {

    @Resource
    private OpenApiTaskMapper openApiTaskMapper;

    @Resource
    private ZOfficeConfig zOfficeConfig;

    @Resource
    private DemoConfig demoConfig;

    @Resource
    private PublicApiConfig publicApiConfig;

    @Resource
    private DocService docService;

    @Value("${server.port:8000}")
    private int serverPort;

    /**
     * 构造 luoshu-server PublicAPI 的完整 URL
     */
    private String buildPublicApiUrl(String path) {
        return String.format("%s://%s:%d/docs%s%s",
                zOfficeConfig.getSchema(), zOfficeConfig.getHost(), zOfficeConfig.getPort(),
                publicApiConfig.getContext(), path);
    }

    /**
     * 构造 demo 端文件下载 URL（供 luoshu-server 回调下载源文件）
     */
    private String buildFileUrl(String docId) {
        return String.format("http://%s:%d%s/%s/content", demoConfig.getHost(), serverPort, demoConfig.getContext(), docId);
    }

    /**
     * 构造 callback URL（供 luoshu-server 任务完成后回调）
     */
    private String buildCallbackUrl() {
        return String.format("http://%s:%d/home/tasks/callback", demoConfig.getHost(), serverPort);
    }

    /**
     * 设置 PublicAPI 鉴权请求头
     */
    private void setAuthHeaders(HttpRequestBase request, String requestBody) {
        PublicApiAuthUtil.AuthHeaders authHeaders = PublicApiAuthUtil.buildAuthHeaders(demoConfig.getRepoId(), publicApiConfig.getSecret(), requestBody);
        request.setHeader(PublicApiAuthUtil.HEADER_AUTH_TYPE, authHeaders.getAuthType());
        request.setHeader(PublicApiAuthUtil.HEADER_TIMESTAMP, authHeaders.getTimestamp());
        request.setHeader(PublicApiAuthUtil.HEADER_NONCE, authHeaders.getNonce());
        request.setHeader("Authorization", authHeaders.getAuthorization());
    }

    /**
     * 安全解析 JSON 响应，非 JSON 格式返回包含原始文本的错误对象
     */
    private JSONObject safeParseJson(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            JSONObject err = new JSONObject();
            err.put("code", "EmptyResponse");
            return err;
        }
        String trimmed = responseBody.trim();
        if (trimmed.startsWith("{")) {
            return JSON.parseObject(trimmed);
        }
        JSONObject err = new JSONObject();
        err.put("code", "NonJsonResponse");
        err.put("rawMessage", responseBody);
        return err;
    }

    @Override
    public Map<String, Object> submitWatermarkTask(String docId, String docName, String opType,
                                                    String paramsJson, String userId, String userToken) {
        Map<String, Object> result = new HashMap<>();
        String apiName = "content/update";
        String requestBody = buildRequestBody(docId, docName, paramsJson, userToken);
        try {
            String url = buildPublicApiUrl("/" + apiName);
            log.info("[OpenApiTaskService] submitTask url={}, docId={}, opType={}", url, docId, opType);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-Type", "application/json");
                setAuthHeaders(httpPost, requestBody);
                httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    int statusCode = response.getStatusLine().getStatusCode();
                    log.info("[OpenApiTaskService] submitTask response status={}, body={}", statusCode, responseBody);

                    if (statusCode == 401) {
                        saveFailedTask(apiName, docId, docName, opType, requestBody, responseBody, userId);
                        result.put("success", false);
                        result.put("error", "认证失败: " + responseBody);
                        return result;
                    }

                    JSONObject json = safeParseJson(responseBody);
                    String taskId = json.getString("taskId");
                    String code = json.getString("code");

                    if ("Ok".equals(code) && taskId != null) {
                        OpenApiTaskEntity entity = OpenApiTaskEntity.builder()
                                .taskId(taskId)
                                .apiName(apiName)
                                .docId(docId)
                                .docName(docName)
                                .opType(opType)
                                .requestBody(requestBody)
                                .status("IN_QUEUE")
                                .serverResponse(responseBody)
                                .userId(userId)
                                .createTime(new Date())
                                .updateTime(new Date())
                                .build();
                        openApiTaskMapper.insert(entity);

                        result.put("success", true);
                        result.put("taskId", taskId);
                        result.put("status", "IN_QUEUE");
                    } else {
                        saveFailedTask(apiName, docId, docName, opType, requestBody, responseBody, userId);
                        result.put("success", false);
                        result.put("error", code != null ? code : "Unknown error");
                        result.put("detail", responseBody);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[OpenApiTaskService] submitTask failed", e);
            saveFailedTask(apiName, docId, docName, opType, requestBody, e.getMessage(), userId);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 保存失败的任务记录（便于在任务池中看到报错信息）
     */
    private void saveFailedTask(String apiName, String docId, String docName,
                                 String opType, String requestBody, String errorResponse, String userId) {
        try {
            OpenApiTaskEntity entity = OpenApiTaskEntity.builder()
                    .taskId("FAIL-" + System.currentTimeMillis())
                    .apiName(apiName)
                    .docId(docId)
                    .docName(docName)
                    .opType(opType)
                    .requestBody(requestBody)
                    .status("FAIL")
                    .errorMsg(errorResponse)
                    .serverResponse(errorResponse)
                    .userId(userId)
                    .createTime(new Date())
                    .updateTime(new Date())
                    .build();
            openApiTaskMapper.insert(entity);
        } catch (Exception e) {
            log.warn("[OpenApiTaskService] saveFailedTask failed", e);
        }
    }

    @Override
    public Map<String, Object> queryTaskStatus(String taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            String url = buildPublicApiUrl("/queryTaskStatus") + "?taskId=" + taskId;

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet httpGet = new HttpGet(url);
                setAuthHeaders(httpGet, null);

                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    log.debug("[OpenApiTaskService] queryTaskStatus taskId={} response={}", taskId, responseBody);

                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 401) {
                        result.put("error", "认证失败: " + responseBody);
                        return result;
                    }

                    JSONObject json = safeParseJson(responseBody);
                    result.put("taskId", json.getString("taskId"));
                    result.put("code", json.getString("code"));

                    JSONObject detail = json.getJSONObject("detail");
                    if (detail != null) {
                        String taskStatus = detail.getString("taskStatus");
                        result.put("taskStatus", taskStatus);
                        result.put("contentId", detail.getString("contentId"));
                        result.put("filename", detail.getString("filename"));
                        result.put("msg", detail.getString("msg"));

                        updateTaskStatusInDb(taskId, taskStatus,
                                detail.getString("contentId"), detail.getString("filename"),
                                detail.getString("msg"), responseBody);

                        if ("SUCCESS".equals(taskStatus) && detail.getString("contentId") != null) {
                            ensureResultDownloaded(taskId, detail.getString("contentId"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[OpenApiTaskService] queryTaskStatus failed for taskId={}", taskId, e);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 确保结果文件已下载并注册，若尚未注册则触发下载
     */
    private void ensureResultDownloaded(String taskId, String contentId) {
        try {
            OpenApiTaskEntity entity = openApiTaskMapper.selectByTaskId(taskId);
            if (entity != null && entity.getResultDocId() == null) {
                log.info("[OpenApiTaskService] ensureResultDownloaded: triggering download for taskId={}", taskId);
                downloadAndRegisterResult(taskId, contentId);
            }
        } catch (Exception e) {
            log.warn("[OpenApiTaskService] ensureResultDownloaded failed for taskId={}", taskId, e);
        }
    }

    /**
     * 更新数据库中的任务状态
     */
    private void updateTaskStatusInDb(String taskId, String status, String contentId,
                                       String filename, String errorMsg, String serverResponse) {
        try {
            OpenApiTaskEntity entity = openApiTaskMapper.selectByTaskId(taskId);
            if (entity != null) {
                entity.setStatus(status != null ? status : entity.getStatus());
                if (contentId != null) entity.setContentId(contentId);
                if (filename != null) entity.setResultFilename(filename);
                if ("FAIL".equals(status) && errorMsg != null) entity.setErrorMsg(errorMsg);
                if (serverResponse != null) entity.setServerResponse(serverResponse);
                entity.setUpdateTime(new Date());
                openApiTaskMapper.updateById(entity);
            }
        } catch (Exception e) {
            log.warn("[OpenApiTaskService] updateTaskStatusInDb failed for taskId={}", taskId, e);
        }
    }

    @Override
    public Map<String, Object> getTaskList(String userId, int pageNo, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        int offset = (pageNo - 1) * pageSize;
        List<OpenApiTaskEntity> tasks = openApiTaskMapper.selectPageByUserId(userId, offset, pageSize);
        int total = openApiTaskMapper.countByUserId(userId);
        int totalPages = (total + pageSize - 1) / pageSize;

        result.put("tasks", tasks);
        result.put("total", total);
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        result.put("totalPages", totalPages);
        return result;
    }

    @Override
    public boolean deleteTask(String taskId) {
        return openApiTaskMapper.deleteByTaskId(taskId) > 0;
    }

    @Override
    public int batchDeleteTasks(List<String> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) return 0;
        return openApiTaskMapper.batchDeleteByTaskIds(taskIds);
    }

    @Override
    public OpenApiTaskEntity getTaskByTaskId(String taskId) {
        return openApiTaskMapper.selectByTaskId(taskId);
    }

    @Override
    public void updateTask(OpenApiTaskEntity entity) {
        openApiTaskMapper.updateById(entity);
    }

    private static final Map<String, String> OP_TYPE_CN = new HashMap<>();
    static {
        OP_TYPE_CN.put("ApplyWatermark", "固定文字水印");
        OP_TYPE_CN.put("ApplyPicWatermark", "固定图片水印");
        OP_TYPE_CN.put("ApplyWatermarkForFixed", "平铺文字水印");
        OP_TYPE_CN.put("ApplyTiledImgWatermarkForFixed", "平铺图片水印");
        OP_TYPE_CN.put("convert_to_pdf", "转PDF");
        OP_TYPE_CN.put("convert_to_ofd", "转OFD");
        OP_TYPE_CN.put("img_to_pdf", "图片转PDF");
        OP_TYPE_CN.put("json_to_excel", "JSON转Excel");
        OP_TYPE_CN.put("pdf_split", "PDF拆分");
        OP_TYPE_CN.put("doc_split", "文档拆分");
    }

    /**
     * 为结果文件生成友好名称：{源文档basename}_{操作类型中文}.{ext}
     * 如果同名文件已存在，则末尾追加序号
     */
    private String generateResultFileName(String srcDocName, String opType) {
        String baseName = FilenameUtils.getBaseName(srcDocName);
        String ext = FilenameUtils.getExtension(srcDocName);
        String opLabel = OP_TYPE_CN.getOrDefault(opType, opType);
        String localDir = System.getProperty("user.dir") + File.separator + "local-file";

        String candidate = baseName + "_" + opLabel + "." + ext;
        File file = new File(localDir, candidate);
        if (!file.exists()) {
            return candidate;
        }
        for (int i = 2; i <= 999; i++) {
            candidate = baseName + "_" + opLabel + "_" + i + "." + ext;
            file = new File(localDir, candidate);
            if (!file.exists()) {
                return candidate;
            }
        }
        return baseName + "_" + opLabel + "_" + System.currentTimeMillis() + "." + ext;
    }

    @Override
    public String downloadAndRegisterResult(String taskId, String contentId) {
        OpenApiTaskEntity task = openApiTaskMapper.selectByTaskId(taskId);
        if (task == null) {
            log.warn("[OpenApiTaskService] downloadAndRegisterResult: task not found {}", taskId);
            return null;
        }

        String url = String.format("%s://%s:%d/docs%s/download?taskId=%s&contentId=%s",
                zOfficeConfig.getSchema(), zOfficeConfig.getHost(), zOfficeConfig.getPort(),
                publicApiConfig.getContext(), taskId, contentId);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            PublicApiAuthUtil.AuthHeaders authHeaders = PublicApiAuthUtil.buildAuthHeaders(
                    demoConfig.getRepoId(), publicApiConfig.getSecret(), null);
            httpGet.setHeader(PublicApiAuthUtil.HEADER_AUTH_TYPE, authHeaders.getAuthType());
            httpGet.setHeader(PublicApiAuthUtil.HEADER_TIMESTAMP, authHeaders.getTimestamp());
            httpGet.setHeader(PublicApiAuthUtil.HEADER_NONCE, authHeaders.getNonce());
            httpGet.setHeader("Authorization", authHeaders.getAuthorization());

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    log.error("[OpenApiTaskService] download result failed, status={}", statusCode);
                    return null;
                }

                String resultFileName;
                if (task.getApiName() != null && task.getApiName().equals("convert")) {
                    String targetFilename = extractTargetFilename(task.getRequestBody());
                    resultFileName = generateConvertResultFileName(task.getDocName(), task.getOpType(), targetFilename);
                } else if (task.getApiName() != null && task.getApiName().equals("split")) {
                    String splitExt = determineSplitResultExt(task.getRequestBody());
                    resultFileName = generateSplitResultFileName(task.getDocName(), task.getOpType(), splitExt);
                } else {
                    resultFileName = generateResultFileName(task.getDocName(), task.getOpType());
                }
                try (InputStream is = response.getEntity().getContent()) {
                    DocMeta docMeta = docService.saveExternalFile(is, resultFileName, task.getUserId());
                    if (docMeta != null) {
                        task.setResultFilename(resultFileName);
                        task.setResultDocId(docMeta.getId());
                        task.setUpdateTime(new Date());
                        openApiTaskMapper.updateById(task);
                        log.info("[OpenApiTaskService] result saved as doc: id={}, name={}", docMeta.getId(), resultFileName);
                        return docMeta.getId();
                    }
                }
            }
        } catch (Exception e) {
            log.error("[OpenApiTaskService] downloadAndRegisterResult failed for taskId={}", taskId, e);
        }
        return null;
    }

    @Override
    public String getResultDocId(String taskId) {
        OpenApiTaskEntity task = openApiTaskMapper.selectByTaskId(taskId);
        return task != null ? task.getResultDocId() : null;
    }

    @Override
    public String buildRequestBody(String docId, String docName, String opsJson, String userToken) {
        JSONObject body = new JSONObject();
        body.put("filename", docName);
        body.put("fileUrl", buildFileUrl(docId));
        body.put("callback", buildCallbackUrl());
        body.put("tokenType", "cookie");
        body.put("tokenValue", demoConfig.getTokenName() + "=" + userToken);
        body.put("ops", JSON.parseArray(opsJson));
        return body.toJSONString();
    }

    @Override
    public Map<String, Object> submitConvertTask(String docId, String docName, String targetFilename,
                                                  String watermarkJson, String userId, String userToken) {
        Map<String, Object> result = new HashMap<>();
        String apiName = "convert";
        String targetExt = FilenameUtils.getExtension(targetFilename).toLowerCase();
        String opType = "convert_to_" + targetExt;
        String requestBody = buildConvertRequestBody(docId, docName, targetFilename, watermarkJson, userToken);
        try {
            String url = buildPublicApiUrl("/" + apiName);
            log.info("[OpenApiTaskService] submitConvertTask url={}, docId={}, target={}", url, docId, targetFilename);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-Type", "application/json");
                setAuthHeaders(httpPost, requestBody);
                httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    int statusCode = response.getStatusLine().getStatusCode();
                    log.info("[OpenApiTaskService] submitConvertTask response status={}, body={}", statusCode, responseBody);

                    if (statusCode == 401) {
                        saveFailedTask(apiName, docId, docName, opType, requestBody, responseBody, userId);
                        result.put("success", false);
                        result.put("error", "认证失败: " + responseBody);
                        return result;
                    }

                    JSONObject json = safeParseJson(responseBody);
                    String taskId = json.getString("taskId");
                    String code = json.getString("code");

                    if ("Ok".equals(code) && taskId != null) {
                        OpenApiTaskEntity entity = OpenApiTaskEntity.builder()
                                .taskId(taskId)
                                .apiName(apiName)
                                .docId(docId)
                                .docName(docName)
                                .opType(opType)
                                .requestBody(requestBody)
                                .status("IN_QUEUE")
                                .serverResponse(responseBody)
                                .userId(userId)
                                .createTime(new Date())
                                .updateTime(new Date())
                                .build();
                        openApiTaskMapper.insert(entity);

                        result.put("success", true);
                        result.put("taskId", taskId);
                        result.put("status", "IN_QUEUE");
                    } else {
                        saveFailedTask(apiName, docId, docName, opType, requestBody, responseBody, userId);
                        result.put("success", false);
                        result.put("error", code != null ? code : "Unknown error");
                        result.put("detail", responseBody);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[OpenApiTaskService] submitConvertTask failed", e);
            saveFailedTask(apiName, docId, docName, opType, requestBody, e.getMessage(), userId);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 从请求体中提取 targetFilename
     */
    private String extractTargetFilename(String requestBody) {
        try {
            JSONObject body = JSON.parseObject(requestBody);
            String target = body.getString("targetFilename");
            if (target != null && !target.isEmpty()) return target;
        } catch (Exception e) {
            log.warn("[OpenApiTaskService] extractTargetFilename failed", e);
        }
        return "result.pdf";
    }

    /**
     * 构造格式转换 API (convert) 的请求体
     * 与 content/update 不同：使用 targetFilename 替代 ops，水印通过独立字段传入
     */
    private String buildConvertRequestBody(String docId, String docName, String targetFilename,
                                            String watermarkJson, String userToken) {
        JSONObject body = new JSONObject();
        body.put("filename", docName);
        body.put("targetFilename", targetFilename);
        body.put("fileUrl", buildFileUrl(docId));
        body.put("callback", buildCallbackUrl());
        body.put("tokenType", "cookie");
        body.put("tokenValue", demoConfig.getTokenName() + "=" + userToken);

        if (watermarkJson != null && !watermarkJson.trim().isEmpty()) {
            JSONObject wmConfig = JSON.parseObject(watermarkJson);
            if (wmConfig.containsKey("tiledWatermark")) {
                body.put("tiledWatermark", wmConfig.getJSONObject("tiledWatermark"));
            }
            if (wmConfig.containsKey("msTextWatermark")) {
                body.put("msTextWatermark", wmConfig.getJSONObject("msTextWatermark"));
            }
            if (wmConfig.containsKey("msPicWatermark")) {
                body.put("msPicWatermark", wmConfig.getJSONObject("msPicWatermark"));
            }
        }
        return body.toJSONString();
    }

    @Override
    public Map<String, Object> submitDocSplitTask(String docId, String docName, String type,
                                                   String ranges, String fixedPages, String fileCount,
                                                   String output, String keyword,
                                                   String userId, String userToken) {
        Map<String, Object> result = new HashMap<>();
        String apiName = "split";
        String ext = FilenameUtils.getExtension(docName).toLowerCase();
        String opType = "pdf".equals(ext) ? "pdf_split" : "doc_split";
        String requestBody = buildDocSplitRequestBody(docId, docName, type, ranges, fixedPages, fileCount, output, keyword, userToken);
        try {
            String url = buildPublicApiUrl("/" + apiName);
            log.info("[OpenApiTaskService] submitDocSplitTask url={}, docId={}, type={}, ext={}", url, docId, type, ext);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-Type", "application/json");
                setAuthHeaders(httpPost, requestBody);
                httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    int statusCode = response.getStatusLine().getStatusCode();
                    log.info("[OpenApiTaskService] submitDocSplitTask response status={}, body={}", statusCode, responseBody);

                    if (statusCode == 401) {
                        saveFailedTask(apiName, docId, docName, opType, requestBody, responseBody, userId);
                        result.put("success", false);
                        result.put("error", "认证失败: " + responseBody);
                        return result;
                    }

                    JSONObject json = safeParseJson(responseBody);
                    String taskId = json.getString("taskId");
                    String code = json.getString("code");

                    if ("Ok".equals(code) && taskId != null) {
                        OpenApiTaskEntity entity = OpenApiTaskEntity.builder()
                                .taskId(taskId)
                                .apiName(apiName)
                                .docId(docId)
                                .docName(docName)
                                .opType(opType)
                                .requestBody(requestBody)
                                .status("IN_QUEUE")
                                .serverResponse(responseBody)
                                .userId(userId)
                                .createTime(new Date())
                                .updateTime(new Date())
                                .build();
                        openApiTaskMapper.insert(entity);

                        result.put("success", true);
                        result.put("taskId", taskId);
                        result.put("status", "IN_QUEUE");
                    } else {
                        saveFailedTask(apiName, docId, docName, opType, requestBody, responseBody, userId);
                        result.put("success", false);
                        result.put("error", code != null ? code : "Unknown error");
                        result.put("detail", responseBody);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[OpenApiTaskService] submitDocSplitTask failed", e);
            saveFailedTask(apiName, docId, docName, opType, requestBody, e.getMessage(), userId);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    private String buildDocSplitRequestBody(String docId, String docName, String type,
                                             String ranges, String fixedPages, String fileCount,
                                             String output, String keyword, String userToken) {
        JSONObject body = new JSONObject();
        body.put("filename", docName);
        body.put("fileUrl", buildFileUrl(docId));
        body.put("callback", buildCallbackUrl());
        body.put("tokenType", "header");
        body.put("tokenValue", userToken);
        body.put("type", type);

        if ("PAGERANGE".equals(type)) {
            if (ranges != null && !ranges.isEmpty()) {
                body.put("ranges", ranges);
            }
            if (output != null && !output.isEmpty()) {
                body.put("output", output);
            }
        } else if ("FIXEDPAGES".equals(type) && fixedPages != null && !fixedPages.isEmpty()) {
            body.put("fixedPages", Integer.parseInt(fixedPages));
        } else if ("FILECOUNT".equals(type) && fileCount != null && !fileCount.isEmpty()) {
            body.put("fileCount", Integer.parseInt(fileCount));
        } else if ("TEXT".equals(type) && keyword != null && !keyword.isEmpty()) {
            body.put("keyword", keyword);
        }
        return body.toJSONString();
    }

    @Override
    public Map<String, Object> submitImg2PdfTask(String docId, String docName, String pageSize,
                                                  String orientation, String margin,
                                                  String userId, String userToken) {
        Map<String, Object> result = new HashMap<>();
        String apiName = "convert";
        String opType = "img_to_pdf";
        String baseName = FilenameUtils.getBaseName(docName);
        String targetFilename = baseName + ".pdf";
        String requestBody = buildImg2PdfRequestBody(docId, docName, targetFilename, pageSize, orientation, margin, userToken);
        try {
            String url = buildPublicApiUrl("/" + apiName);
            log.info("[OpenApiTaskService] submitImg2PdfTask url={}, docId={}, pageSize={}", url, docId, pageSize);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-Type", "application/json");
                setAuthHeaders(httpPost, requestBody);
                httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    int statusCode = response.getStatusLine().getStatusCode();
                    log.info("[OpenApiTaskService] submitImg2PdfTask response status={}, body={}", statusCode, responseBody);

                    if (statusCode == 401) {
                        saveFailedTask(apiName, docId, docName, opType, requestBody, responseBody, userId);
                        result.put("success", false);
                        result.put("error", "认证失败: " + responseBody);
                        return result;
                    }

                    JSONObject json = safeParseJson(responseBody);
                    String taskId = json.getString("taskId");
                    String code = json.getString("code");

                    if ("Ok".equals(code) && taskId != null) {
                        OpenApiTaskEntity entity = OpenApiTaskEntity.builder()
                                .taskId(taskId)
                                .apiName(apiName)
                                .docId(docId)
                                .docName(docName)
                                .opType(opType)
                                .requestBody(requestBody)
                                .status("IN_QUEUE")
                                .serverResponse(responseBody)
                                .userId(userId)
                                .createTime(new Date())
                                .updateTime(new Date())
                                .build();
                        openApiTaskMapper.insert(entity);

                        result.put("success", true);
                        result.put("taskId", taskId);
                        result.put("status", "IN_QUEUE");
                    } else {
                        saveFailedTask(apiName, docId, docName, opType, requestBody, responseBody, userId);
                        result.put("success", false);
                        result.put("error", code != null ? code : "Unknown error");
                        result.put("detail", responseBody);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[OpenApiTaskService] submitImg2PdfTask failed", e);
            saveFailedTask(apiName, docId, docName, opType, requestBody, e.getMessage(), userId);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 构造图片转 PDF 的请求体
     * 与格式转换不同：额外包含 imgToPdfOptions 参数
     */
    private String buildImg2PdfRequestBody(String docId, String docName, String targetFilename,
                                            String pageSize, String orientation, String margin,
                                            String userToken) {
        JSONObject body = new JSONObject();
        body.put("filename", docName);
        body.put("targetFilename", targetFilename);
        body.put("fileUrl", buildFileUrl(docId));
        body.put("callback", buildCallbackUrl());
        // body.put("tokenType", "cookie");
        // body.put("tokenValue", demoConfig.getTokenName() + "=" + userToken);
        body.put("tokenType", "header");
        body.put("tokenValue",userToken);

        JSONObject imgOpts = new JSONObject();
        imgOpts.put("pageSize", pageSize);
        if (!"FIT_IMAGE".equals(pageSize)) {
            imgOpts.put("orientation", orientation);
            imgOpts.put("margin", margin);
        }
        body.put("imgToPdfOptions", imgOpts);
        return body.toJSONString();
    }

    @Override
    public Map<String, Object> submitJson2ExcelTask(String docId, String docName,
                                                     String sheetName, String columnOrder,
                                                     String userId, String userToken) {
        Map<String, Object> result = new HashMap<>();
        String apiName = "convert";
        String opType = "json_to_excel";
        String baseName = FilenameUtils.getBaseName(docName);
        String targetFilename = baseName + ".xlsx";
        String requestBody = buildJson2ExcelRequestBody(docId, docName, targetFilename, sheetName, columnOrder, userToken);
        try {
            String url = buildPublicApiUrl("/" + apiName);
            log.info("[OpenApiTaskService] submitJson2ExcelTask url={}, docId={}, sheetName={}", url, docId, sheetName);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-Type", "application/json");
                setAuthHeaders(httpPost, requestBody);
                httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    int statusCode = response.getStatusLine().getStatusCode();
                    log.info("[OpenApiTaskService] submitJson2ExcelTask response status={}, body={}", statusCode, responseBody);

                    if (statusCode == 401) {
                        saveFailedTask(apiName, docId, docName, opType, requestBody, responseBody, userId);
                        result.put("success", false);
                        result.put("error", "认证失败: " + responseBody);
                        return result;
                    }

                    JSONObject json = safeParseJson(responseBody);
                    String taskId = json.getString("taskId");
                    String code = json.getString("code");

                    if ("Ok".equals(code) && taskId != null) {
                        OpenApiTaskEntity entity = OpenApiTaskEntity.builder()
                                .taskId(taskId)
                                .apiName(apiName)
                                .docId(docId)
                                .docName(docName)
                                .opType(opType)
                                .requestBody(requestBody)
                                .status("IN_QUEUE")
                                .serverResponse(responseBody)
                                .userId(userId)
                                .createTime(new Date())
                                .updateTime(new Date())
                                .build();
                        openApiTaskMapper.insert(entity);

                        result.put("success", true);
                        result.put("taskId", taskId);
                        result.put("status", "IN_QUEUE");
                    } else {
                        saveFailedTask(apiName, docId, docName, opType, requestBody, responseBody, userId);
                        result.put("success", false);
                        result.put("error", code != null ? code : "Unknown error");
                        result.put("detail", responseBody);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[OpenApiTaskService] submitJson2ExcelTask failed", e);
            saveFailedTask(apiName, docId, docName, opType, requestBody, e.getMessage(), userId);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 构造 JSON 转 Excel 的请求体
     * <p>
     * 只有非 null 的选项参数才会加入 jsonToExcelOptions，
     * null 表示"不传该参数"，让服务端使用默认值。
     */
    private String buildJson2ExcelRequestBody(String docId, String docName, String targetFilename,
                                               String sheetName, String columnOrder, String userToken) {
        JSONObject body = new JSONObject();
        body.put("filename", docName);
        body.put("targetFilename", targetFilename);
        body.put("fileUrl", buildFileUrl(docId));
        body.put("callback", buildCallbackUrl());
        body.put("tokenType", "header");
        body.put("tokenValue", userToken);

        JSONObject opts = new JSONObject();
        if (sheetName != null && !sheetName.isEmpty()) {
            opts.put("sheetName", sheetName);
        }
        if (columnOrder != null && !columnOrder.isEmpty()) {
            String[] cols = columnOrder.split(",");
            com.alibaba.fastjson.JSONArray arr = new com.alibaba.fastjson.JSONArray();
            for (String col : cols) {
                String trimmed = col.trim();
                if (!trimmed.isEmpty()) arr.add(trimmed);
            }
            if (!arr.isEmpty()) opts.put("columnOrder", arr);
        }

        if (!opts.isEmpty()) {
            body.put("jsonToExcelOptions", opts);
        }
        return body.toJSONString();
    }

    /**
     * 根据 split 请求体中的 type 和 output 判断结果文件扩展名
     */
    private String determineSplitResultExt(String requestBody) {
        try {
            JSONObject body = JSON.parseObject(requestBody);
            String type = body.getString("type");
            String output = body.getString("output");
            if ("PAGERANGE".equals(type) && "singleFile".equals(output)) {
                return "pdf";
            }
        } catch (Exception e) {
            log.warn("[OpenApiTaskService] determineSplitResultExt failed", e);
        }
        return "zip";
    }

    private String generateSplitResultFileName(String srcDocName, String opType, String ext) {
        String baseName = FilenameUtils.getBaseName(srcDocName);
        String opLabel = OP_TYPE_CN.getOrDefault(opType, opType);
        String localDir = System.getProperty("user.dir") + File.separator + "local-file";

        String candidate = baseName + "_" + opLabel + "." + ext;
        File file = new File(localDir, candidate);
        if (!file.exists()) {
            return candidate;
        }
        for (int i = 2; i <= 999; i++) {
            candidate = baseName + "_" + opLabel + "_" + i + "." + ext;
            file = new File(localDir, candidate);
            if (!file.exists()) {
                return candidate;
            }
        }
        return baseName + "_" + opLabel + "_" + System.currentTimeMillis() + "." + ext;
    }

    /**
     * 生成转换任务的结果文件名
     * 结果扩展名取自 targetFilename，而非源文档
     */
    private String generateConvertResultFileName(String srcDocName, String opType, String targetFilename) {
        String baseName = FilenameUtils.getBaseName(srcDocName);
        String targetExt = FilenameUtils.getExtension(targetFilename);
        String opLabel = OP_TYPE_CN.getOrDefault(opType, opType);
        String localDir = System.getProperty("user.dir") + File.separator + "local-file";

        String candidate = baseName + "_" + opLabel + "." + targetExt;
        File file = new File(localDir, candidate);
        if (!file.exists()) {
            return candidate;
        }
        for (int i = 2; i <= 999; i++) {
            candidate = baseName + "_" + opLabel + "_" + i + "." + targetExt;
            file = new File(localDir, candidate);
            if (!file.exists()) {
                return candidate;
            }
        }
        return baseName + "_" + opLabel + "_" + System.currentTimeMillis() + "." + targetExt;
    }

    /**
     * 提交文档套红（书签内容替换）任务
     * 通过 content/update 接口，actId 为 UpdateBookmarkRef
     */
    @Override
    public Map<String, Object> submitBookmarkTask(String docId, String docName,
                                                   String opsJson, String userId, String userToken) {
        Map<String, Object> result = new HashMap<>();
        String apiName = "content/update";
        String opType = "bookmark_replace";
        String requestBody = buildRequestBody(docId, docName, opsJson, userToken);
        try {
            String url = buildPublicApiUrl("/" + apiName);
            log.info("[OpenApiTaskService] submitBookmarkTask url={}, docId={}", url, docId);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-Type", "application/json");
                setAuthHeaders(httpPost, requestBody);
                httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    int statusCode = response.getStatusLine().getStatusCode();
                    log.info("[OpenApiTaskService] submitBookmarkTask response status={}, body={}", statusCode, responseBody);

                    if (statusCode == 401) {
                        saveFailedTask(apiName, docId, docName, opType, requestBody, responseBody, userId);
                        result.put("success", false);
                        result.put("error", "认证失败: " + responseBody);
                        return result;
                    }

                    JSONObject json = safeParseJson(responseBody);
                    String taskId = json.getString("taskId");
                    String code = json.getString("code");

                    if ("Ok".equals(code) && taskId != null) {
                        OpenApiTaskEntity entity = OpenApiTaskEntity.builder()
                                .taskId(taskId)
                                .apiName(apiName)
                                .docId(docId)
                                .docName(docName)
                                .opType(opType)
                                .requestBody(requestBody)
                                .status("IN_QUEUE")
                                .serverResponse(responseBody)
                                .userId(userId)
                                .createTime(new Date())
                                .updateTime(new Date())
                                .build();
                        openApiTaskMapper.insert(entity);

                        result.put("success", true);
                        result.put("taskId", taskId);
                        result.put("status", "IN_QUEUE");
                    } else {
                        saveFailedTask(apiName, docId, docName, opType, requestBody, responseBody, userId);
                        result.put("success", false);
                        result.put("error", code != null ? code : "Unknown error");
                        result.put("detail", responseBody);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[OpenApiTaskService] submitBookmarkTask failed", e);
            saveFailedTask(apiName, docId, docName, opType, requestBody, e.getMessage(), userId);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * PPT 加水印：先转 PDF，再对 PDF 加水印（异步线程中串联两步）
     * 前端提交后立即返回，后台异步完成转换+加水印
     */
    @Override
    public Map<String, Object> submitPptWatermarkTask(String docId, String docName, String watermarkType,
                                                      String opsJson, String userId, String userToken) {
        Map<String, Object> result = new HashMap<>();
        String baseName = FilenameUtils.getBaseName(docName);
        String pdfFilename = baseName + ".pdf";

        // 步骤1：提交 PPT→PDF 转换
        Map<String, Object> convertResult = submitConvertTask(docId, docName, pdfFilename, null, userId, userToken);
        if (!Boolean.TRUE.equals(convertResult.get("success"))) {
            result.put("success", false);
            result.put("error", "PPT 转 PDF 提交失败: " + convertResult.get("error"));
            return result;
        }

        String convertTaskId = (String) convertResult.get("taskId");
        log.info("[OpenApiTaskService] submitPptWatermarkTask: convert task submitted, taskId={}", convertTaskId);

        // 异步等待转换完成后提交水印任务
        new Thread(() -> {
            try {
                String pdfDocId = waitForConvertAndGetPdfDocId(convertTaskId, 120);
                if (pdfDocId == null) {
                    log.error("[OpenApiTaskService] submitPptWatermarkTask: convert timeout or failed, taskId={}", convertTaskId);
                    return;
                }
                String pdfDocName = baseName + ".pdf";
                log.info("[OpenApiTaskService] submitPptWatermarkTask: convert done, pdfDocId={}, submitting watermark", pdfDocId);
                submitWatermarkTask(pdfDocId, pdfDocName, watermarkType, opsJson, userId, userToken);
            } catch (Exception e) {
                log.error("[OpenApiTaskService] submitPptWatermarkTask async failed", e);
            }
        }, "ppt-watermark-" + convertTaskId).start();

        // 返回转换任务的 taskId，前端先轮询转换状态
        // 转换成功后后端自动提交水印任务，前端可继续轮询水印任务
        result.put("success", true);
        result.put("taskId", convertTaskId);
        result.put("step", "convert");
        return result;
    }

    /**
     * 轮询等待转换任务完成，返回结果 PDF 在仓库中的 docId
     * @param convertTaskId 转换任务ID
     * @param timeoutSeconds 超时秒数
     * @return PDF 文件的 docId，超时或失败返回 null
     */
    private String waitForConvertAndGetPdfDocId(String convertTaskId, int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(2000);
                Map<String, Object> status = queryTaskStatus(convertTaskId);
                String taskStatus = (String) status.get("taskStatus");
                if ("SUCCESS".equals(taskStatus)) {
                    // 转换成功，获取注册到仓库的 docId
                    String resultDocId = getResultDocId(convertTaskId);
                    if (resultDocId != null) {
                        return resultDocId;
                    }
                    // 如果 resultDocId 还没注册好，再等一下
                    Thread.sleep(1000);
                    return getResultDocId(convertTaskId);
                } else if ("FAIL".equals(taskStatus)) {
                    log.warn("[OpenApiTaskService] waitForConvert: task failed, taskId={}", convertTaskId);
                    return null;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                log.warn("[OpenApiTaskService] waitForConvert: poll error for taskId={}", convertTaskId, e);
            }
        }
        log.warn("[OpenApiTaskService] waitForConvert: timeout for taskId={}", convertTaskId);
        return null;
    }
}
