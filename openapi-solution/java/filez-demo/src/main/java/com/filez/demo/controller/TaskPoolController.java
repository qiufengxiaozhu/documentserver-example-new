package com.filez.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.filez.demo.common.aspect.Log;
import com.filez.demo.common.context.UserContext;
import com.filez.demo.common.utils.PublicApiAuthUtil;
import com.filez.demo.config.DemoConfig;
import com.filez.demo.config.PublicApiConfig;
import com.filez.demo.config.ZOfficeConfig;
import com.filez.demo.entity.OpenApiTaskEntity;
import com.filez.demo.service.OpenApiTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 通用 OpenAPI 任务池控制器
 * 负责展示所有 OpenAPI 异步任务（水印、转换、合并、拆分等）的运行状态
 */
@Api(tags = "任务池控制器")
@Controller
@Slf4j
@RequestMapping("/home/tasks")
public class TaskPoolController {

    @Resource
    private OpenApiTaskService openApiTaskService;

    @Resource
    private DemoConfig demoConfig;

    @Resource
    private ZOfficeConfig zOfficeConfig;

    @Resource
    private PublicApiConfig publicApiConfig;

    /**
     * 任务池页面
     */
    @Log("任务池页面")
    @ApiOperation(value = "/home/tasks：任务池页面")
    @GetMapping("")
    public String tasksPage() {
        return "tasks/taskPool";
    }

    /**
     * 查询任务状态（前端轮询用）
     */
    @ApiOperation(value = "/home/tasks/taskStatus：查询任务状态")
    @GetMapping("/taskStatus")
    @ResponseBody
    public Map<String, Object> queryTaskStatus(@RequestParam String taskId) {
        return openApiTaskService.queryTaskStatus(taskId);
    }

    /**
     * 分页查询任务列表（AJAX）
     */
    @ApiOperation(value = "/home/tasks/list：分页查询任务列表")
    @GetMapping("/list")
    @ResponseBody
    public Map<String, Object> getTaskList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        String userId = Objects.requireNonNull(UserContext.getCurrentUser()).getId();
        return openApiTaskService.getTaskList(userId, pageNo, pageSize);
    }

    /**
     * 删除单个任务
     */
    @Log("删除任务")
    @ApiOperation(value = "/home/tasks/{taskId}：删除单个任务")
    @DeleteMapping("/{taskId}")
    @ResponseBody
    public Map<String, Object> deleteTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", openApiTaskService.deleteTask(taskId));
        return result;
    }

    /**
     * 批量删除任务
     */
    @Log("批量删除任务")
    @ApiOperation(value = "/home/tasks/batch-delete：批量删除任务")
    @PostMapping("/batch-delete")
    @ResponseBody
    public Map<String, Object> batchDeleteTasks(@RequestBody Map<String, List<String>> params) {
        List<String> taskIds = params.get("taskIds");
        Map<String, Object> result = new HashMap<>();
        result.put("deleted", openApiTaskService.batchDeleteTasks(taskIds));
        return result;
    }

    /**
     * 设置下载/预览请求的 PublicAPI 鉴权头
     */
    private void setAuthHeaders(HttpGet httpGet) {
        PublicApiAuthUtil.AuthHeaders authHeaders = PublicApiAuthUtil.buildAuthHeaders(
                demoConfig.getRepoId(), publicApiConfig.getSecret(), null);
        httpGet.setHeader(PublicApiAuthUtil.HEADER_AUTH_TYPE, authHeaders.getAuthType());
        httpGet.setHeader(PublicApiAuthUtil.HEADER_TIMESTAMP, authHeaders.getTimestamp());
        httpGet.setHeader(PublicApiAuthUtil.HEADER_NONCE, authHeaders.getNonce());
        httpGet.setHeader("Authorization", authHeaders.getAuthorization());
    }

    /**
     * 代理下载结果文件
     */
    @ApiOperation(value = "/home/tasks/download：代理下载结果文件")
    @GetMapping("/download")
    public void downloadResult(@RequestParam String taskId,
                                @RequestParam String contentId,
                                HttpServletResponse response) {
        String url = String.format("%s://%s:%d/docs%s/download?taskId=%s&contentId=%s",
                zOfficeConfig.getSchema(), zOfficeConfig.getHost(), zOfficeConfig.getPort(),
                publicApiConfig.getContext(), taskId, contentId);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            setAuthHeaders(httpGet);

            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
                String contentDisposition = null;
                if (httpResponse.getFirstHeader("Content-Disposition") != null) {
                    contentDisposition = httpResponse.getFirstHeader("Content-Disposition").getValue();
                }
                String contentType = "application/octet-stream";
                if (httpResponse.getFirstHeader("Content-Type") != null) {
                    contentType = httpResponse.getFirstHeader("Content-Type").getValue();
                }

                response.setContentType(contentType);
                if (contentDisposition != null) {
                    response.setHeader("Content-Disposition", contentDisposition);
                }

                try (InputStream is = httpResponse.getEntity().getContent();
                     OutputStream os = response.getOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    os.flush();
                }
            }
        } catch (Exception e) {
            log.error("[TaskPoolController] download failed, taskId={}, contentId={}", taskId, contentId, e);
            response.setStatus(500);
        }
    }

    /**
     * 获取任务结果文件的预览 URL（driver-callback / 标准集成）
     * 前端轮询到 SUCCESS 后调用此接口，将返回的 URL 设置为 iframe src
     */
    @ApiOperation(value = "/home/tasks/previewUrl：获取结果文件预览URL")
    @GetMapping("/previewUrl")
    @ResponseBody
    public Map<String, Object> getPreviewUrl(@RequestParam String taskId) {
        Map<String, Object> result = new HashMap<>();
        OpenApiTaskEntity task = openApiTaskService.getTaskByTaskId(taskId);
        if (task == null || task.getResultFilename() == null) {
            result.put("success", false);
            result.put("msg", "任务不存在或结果文件未就绪");
            return result;
        }
        String resultDocId = openApiTaskService.getResultDocId(taskId);
        if (resultDocId == null) {
            result.put("success", false);
            result.put("msg", "结果文件尚未注册到仓库");
            return result;
        }
        result.put("success", true);
        result.put("docId", resultDocId);
        return result;
    }

    /**
     * 接收 luoshu-server 任务完成回调
     */
    @ApiOperation(value = "/home/tasks/callback：接收任务完成回调")
    @PostMapping("/callback")
    @ResponseBody
    public Map<String, Object> handleCallback(@RequestBody String body) {
        log.info("[TaskPoolController] callback received: {}", body);
        Map<String, Object> result = new HashMap<>();
        try {
            JSONObject json = JSON.parseObject(body);
            String taskId = json.getString("taskId");
            JSONObject detail = json.getJSONObject("detail");

            if (detail != null) {
                String taskStatus = detail.getString("taskStatus");
                String contentId = detail.getString("contentId");
                String filename = detail.getString("filename");
                String msg = detail.getString("msg");

                OpenApiTaskEntity entity = openApiTaskService.getTaskByTaskId(taskId);
                if (entity != null) {
                    entity.setStatus(taskStatus != null ? taskStatus : entity.getStatus());
                    if (contentId != null) entity.setContentId(contentId);
                    if (filename != null) entity.setResultFilename(filename);
                    if ("FAIL".equals(taskStatus) && msg != null) entity.setErrorMsg(msg);
                    entity.setServerResponse(body);
                    entity.setUpdateTime(new Date());
                    openApiTaskService.updateTask(entity);
                    log.info("[TaskPoolController] callback updated task {} status to {}", taskId, taskStatus);

                    if ("SUCCESS".equals(taskStatus) && contentId != null) {
                        String resultDocId = openApiTaskService.downloadAndRegisterResult(taskId, contentId);
                        if (resultDocId != null) {
                            log.info("[TaskPoolController] result file registered as docId={}", resultDocId);
                        }
                    }
                }
            }

            result.put("code", "Ok");
            result.put("taskId", json.getString("taskId"));
        } catch (Exception e) {
            log.error("[TaskPoolController] callback processing failed", e);
            result.put("code", "Error");
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
