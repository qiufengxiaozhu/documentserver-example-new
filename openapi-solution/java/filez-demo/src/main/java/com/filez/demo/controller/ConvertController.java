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

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 格式转换控制器
 * 对接 luoshu-server 的 PublicAPI /convert 接口
 * 支持 Word→PDF/OFD、PDF→OFD 并可选叠加水印
 */
@Api(tags = "格式转换控制器")
@Controller
@Slf4j
@RequestMapping("/home/convert")
public class ConvertController {

    @Resource
    private OpenApiTaskService openApiTaskService;

    @Resource
    private DocService docService;

    @Resource
    private DemoConfig demoConfig;

    @Value("${server.port:8000}")
    private int serverPort;

    /**
     * 格式转换页面
     */
    @Log("格式转换页面")
    @ApiOperation(value = "/home/convert：格式转换页面")
    @GetMapping("")
    public String convertPage(Model model) {
        model.addAttribute("files", docService.listFiles());
        String baseUrl = "http://" + demoConfig.getHost() + ":" + serverPort + demoConfig.getContext();
        model.addAttribute("demoBaseUrl", baseUrl);
        return "convert/formatConvert";
    }

    /**
     * 提交格式转换任务
     */
    @Log("提交格式转换任务")
    @ApiOperation(value = "/home/convert/submit：提交格式转换任务")
    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submitTask(@RequestBody Map<String, String> params) {
        String docId = params.get("docId");
        String docName = params.get("docName");
        String targetFilename = params.get("targetFilename");
        String watermarkJson = params.get("watermarkJson");
        String userId = Objects.requireNonNull(UserContext.getCurrentUser()).getId();
        String userToken = UserContext.getCurrentUserToken();

        if (docId == null || docName == null || targetFilename == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "缺少必要参数");
            return error;
        }

        return openApiTaskService.submitConvertTask(docId, docName, targetFilename, watermarkJson, userId, userToken);
    }
}
