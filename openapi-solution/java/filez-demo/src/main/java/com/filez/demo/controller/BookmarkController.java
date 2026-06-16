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
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 文档套红（书签内容替换）控制器
 * 通过 OpenAPI content/update 接口的 UpdateBookmarkRef 操作实现
 */
@Api(tags = "文档套红控制器")
@Controller
@Slf4j
@RequestMapping("/home/bookmark")
public class BookmarkController {

    @Resource
    private OpenApiTaskService openApiTaskService;

    @Resource
    private DocService docService;

    @Resource
    private DemoConfig demoConfig;

    @Value("${server.port:8000}")
    private int serverPort;

    /**
     * 文档套红页面
     */
    @Log("文档套红页面")
    @ApiOperation(value = "/home/bookmark：文档套红页面")
    @GetMapping("")
    public String bookmarkPage(Model model) {
        model.addAttribute("files", docService.listFiles());
        String baseUrl = "http://" + demoConfig.getHost() + ":" + serverPort + demoConfig.getContext();
        model.addAttribute("demoBaseUrl", baseUrl);
        return "bookmark/bookmark";
    }

    /**
     * 提交文档套红任务
     * 底层复用 content/update 接口，actId 为 UpdateBookmarkRef
     */
    @Log("提交文档套红任务")
    @ApiOperation(value = "/home/bookmark/submit：提交文档套红任务")
    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submitTask(@RequestBody Map<String, String> params) {
        String docId = params.get("docId");
        String docName = params.get("docName");
        String opsJson = params.get("opsJson");
        String userId = Objects.requireNonNull(UserContext.getCurrentUser()).getId();
        String userToken = UserContext.getCurrentUserToken();

        if (docId == null || docName == null || opsJson == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "缺少必要参数");
            return error;
        }

        return openApiTaskService.submitBookmarkTask(docId, docName, opsJson, userId, userToken);
    }
}
