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

@Api(tags = "文档拆分控制器")
@Controller
@Slf4j
@RequestMapping("/home/docSplit")
public class DocSplitController {

    @Resource
    private OpenApiTaskService openApiTaskService;

    @Resource
    private DocService docService;

    @Resource
    private DemoConfig demoConfig;

    @Value("${server.port:8000}")
    private int serverPort;

    @Log("文档拆分页面")
    @ApiOperation(value = "/home/docSplit：文档拆分页面")
    @GetMapping("")
    public String docSplitPage(Model model) {
        model.addAttribute("files", docService.listFiles());
        String baseUrl = "http://" + demoConfig.getHost() + ":" + serverPort + demoConfig.getContext();
        model.addAttribute("demoBaseUrl", baseUrl);
        return "convert/docsplit";
    }

    @Log("提交文档拆分任务")
    @ApiOperation(value = "/home/docSplit/submit：提交文档拆分任务")
    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submitTask(@RequestBody Map<String, String> params) {
        String docId = params.get("docId");
        String docName = params.get("docName");
        String type = params.get("type");
        String ranges = params.get("ranges");
        String fixedPages = params.get("fixedPages");
        String fileCount = params.get("fileCount");
        String output = params.get("output");
        String keyword = params.get("keyword");

        SysUserEntity currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            log.error("[DocSplitController] UserContext.getCurrentUser() is null, token={}", UserContext.getCurrentUserToken());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "用户未登录");
            return error;
        }
        String userId = currentUser.getId();
        String userToken = UserContext.getCurrentUserToken();

        if (docId == null || docName == null || type == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "缺少必要参数");
            return error;
        }

        return openApiTaskService.submitDocSplitTask(docId, docName, type, ranges, fixedPages, fileCount, output, keyword, userId, userToken);
    }
}
