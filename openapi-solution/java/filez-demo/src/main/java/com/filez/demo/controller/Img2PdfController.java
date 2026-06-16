package com.filez.demo.controller;

import com.filez.demo.common.aspect.Log;
import com.filez.demo.common.context.UserContext;
import com.filez.demo.config.DemoConfig;
import com.filez.demo.service.DocService;
import com.filez.demo.service.OpenApiTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.filez.demo.entity.SysUserEntity;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片转 PDF 控制器
 * 对接 luoshu-server 的 PublicAPI /convert 接口（imgToPdfOptions）
 * 支持 JPG/JPEG/PNG/BMP/GIF/TIFF → PDF
 */
@Api(tags = "图片转PDF控制器")
@Controller
@Slf4j
@RequestMapping("/home/imageToPdf")
public class Img2PdfController {

    @Resource
    private OpenApiTaskService openApiTaskService;

    @Resource
    private DocService docService;

    @Resource
    private DemoConfig demoConfig;

    @Value("${server.port:8000}")
    private int serverPort;

    /**
     * 图片转 PDF 页面
     */
    @Log("图片转PDF页面")
    @ApiOperation(value = "/home/imageToPdf：图片转PDF页面")
    @GetMapping("")
    public String img2PdfPage(Model model) {
        model.addAttribute("files", docService.listFiles());
        String baseUrl = "http://" + demoConfig.getHost() + ":" + serverPort + demoConfig.getContext();
        model.addAttribute("demoBaseUrl", baseUrl);
        return "convert/img2pdf";
    }

    /**
     * 提交图片转 PDF 任务
     */
    @Log("提交图片转PDF任务")
    @ApiOperation(value = "/home/imageToPdf/submit：提交图片转PDF任务")
    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submitTask(@RequestBody Map<String, String> params) {
        String docId = params.get("docId");
        String docName = params.get("docName");
        String pageSize = params.getOrDefault("pageSize", "FIT_IMAGE");
        String orientation = params.getOrDefault("orientation", "auto");
        String margin = params.getOrDefault("margin", "none");
        SysUserEntity currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            log.error("[Img2PdfController] UserContext.getCurrentUser() is null, token={}", UserContext.getCurrentUserToken());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "用户未登录");
            return error;
        }
        String userId = currentUser.getId();
        String userToken = UserContext.getCurrentUserToken();

        if (docId == null || docName == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "缺少必要参数");
            return error;
        }

        return openApiTaskService.submitImg2PdfTask(docId, docName, pageSize, orientation, margin, userId, userToken);
    }
}
