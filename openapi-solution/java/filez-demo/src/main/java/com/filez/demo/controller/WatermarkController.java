package com.filez.demo.controller;

import com.filez.demo.common.aspect.Log;
import com.filez.demo.common.context.UserContext;
import com.filez.demo.config.DemoConfig;
import com.filez.demo.service.DocService;
import com.filez.demo.service.OpenApiTaskService;
import org.springframework.beans.factory.annotation.Value;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 水印功能控制器
 * 仅负责水印页面和提交水印任务，任务管理统一由 TaskPoolController 处理
 */
@Api(tags = "水印功能控制器")
@Controller
@Slf4j
@RequestMapping("/home/watermark")
public class WatermarkController {

    @Resource
    private OpenApiTaskService openApiTaskService;

    @Resource
    private DocService docService;

    @Resource
    private DemoConfig demoConfig;

    @Value("${server.port:8000}")
    private int serverPort;

    /**
     * 添加水印页面
     */
    @Log("水印功能页面")
    @ApiOperation(value = "/home/watermark：水印功能页面")
    @GetMapping("")
    public String watermarkPage(Model model) {
        model.addAttribute("files", docService.listFiles());
        String baseUrl = "http://" + demoConfig.getHost() + ":" + serverPort + demoConfig.getContext();
        model.addAttribute("demoBaseUrl", baseUrl);
        return "watermark/watermark";
    }

    /**
     * 图片水印POC页面（固定图片水印 + qrCode 附加图片）
     */
    @Log("图片水印POC页面")
    @ApiOperation(value = "/home/watermark/picPoc：图片水印POC页面")
    @GetMapping("/picPoc")
    public String picWatermarkPocPage(Model model) {
        model.addAttribute("files", docService.listFiles());
        String baseUrl = "http://" + demoConfig.getHost() + ":" + serverPort + demoConfig.getContext();
        model.addAttribute("demoBaseUrl", baseUrl);
        return "watermark/picWatermarkPoc";
    }

    /**
     * 提交水印任务
     */
    @Log("提交水印任务")
    @ApiOperation(value = "/home/watermark/submit：提交水印任务")
    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submitTask(@RequestBody Map<String, String> params) {
        String docId = params.get("docId");
        String docName = params.get("docName");
        String watermarkType = params.get("watermarkType");
        String opsJson = params.get("opsJson");
        String userId = Objects.requireNonNull(UserContext.getCurrentUser()).getId();
        String userToken = UserContext.getCurrentUserToken();

        if (docId == null || docName == null || watermarkType == null || opsJson == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "缺少必要参数");
            return error;
        }

        return openApiTaskService.submitWatermarkTask(docId, docName, watermarkType, opsJson, userId, userToken);
    }

    /**
     * 提交 PPT 加水印任务（串联：先转 PDF，再加水印）
     * 前端一次提交，后端分两步异步执行
     */
    @Log("提交PPT加水印任务")
    @ApiOperation(value = "/home/watermark/submitPptWatermark：PPT 转 PDF 后加水印")
    @PostMapping("/submitPptWatermark")
    @ResponseBody
    public Map<String, Object> submitPptWatermark(@RequestBody Map<String, String> params) {
        String docId = params.get("docId");
        String docName = params.get("docName");
        String watermarkType = params.get("watermarkType");
        String opsJson = params.get("opsJson");
        String userId = Objects.requireNonNull(UserContext.getCurrentUser()).getId();
        String userToken = UserContext.getCurrentUserToken();

        if (docId == null || docName == null || watermarkType == null || opsJson == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "缺少必要参数");
            return error;
        }

        return openApiTaskService.submitPptWatermarkTask(docId, docName, watermarkType, opsJson, userId, userToken);
    }

    /**
     * 获取文件列表（AJAX，按格式筛选）
     */
    @ApiOperation(value = "/home/watermark/files：获取文件列表")
    @GetMapping("/files")
    @ResponseBody
    public List<?> getFileList(@RequestParam(required = false) String format) {
        return docService.listFiles();
    }
}
