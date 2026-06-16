package com.filez.demo.controller;

import com.filez.demo.common.aspect.Log;
import com.filez.demo.common.context.UserContext;
import com.filez.demo.config.DemoConfig;
import com.filez.demo.entity.SysUserEntity;
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

/**
 * JSON 转 Excel 控制器
 * 对接 luoshu-server 的 PublicAPI /convert 接口（jsonToExcelOptions）
 * 支持 JSON → XLSX，可配置表头样式、列顺序、类型转换等参数
 */
@Api(tags = "JSON转Excel控制器")
@Controller
@Slf4j
@RequestMapping("/home/jsonToExcel")
public class Json2ExcelController {

    @Resource
    private OpenApiTaskService openApiTaskService;

    @Resource
    private DocService docService;

    @Resource
    private DemoConfig demoConfig;

    @Value("${server.port:8000}")
    private int serverPort;

    /**
     * JSON 转 Excel 页面
     */
    @Log("JSON转Excel页面")
    @ApiOperation(value = "/home/jsonToExcel：JSON转Excel页面")
    @GetMapping("")
    public String json2ExcelPage(Model model) {
        model.addAttribute("files", docService.listFiles());
        String baseUrl = "http://" + demoConfig.getHost() + ":" + serverPort + demoConfig.getContext();
        model.addAttribute("demoBaseUrl", baseUrl);
        return "convert/json2excel";
    }

    /**
     * 提交 JSON 转 Excel 任务
     * <p>
     * 参数说明：
     * - docId: 必填，JSON 源文件 ID
     * - docName: 必填，JSON 源文件名
     * - sheetName: 可选，工作表名称，不传则服务端使用 "Sheet1"
     * - columnOrder: 可选，逗号分隔的列名列表，不传则按 JSON key 出现顺序
     */
    @Log("提交JSON转Excel任务")
    @ApiOperation(value = "/home/jsonToExcel/submit：提交JSON转Excel任务")
    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submitTask(@RequestBody Map<String, String> params) {
        String docId = params.get("docId");
        String docName = params.get("docName");
        String sheetName = params.get("sheetName");
        String columnOrder = params.get("columnOrder");

        SysUserEntity currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            log.error("[Json2ExcelController] UserContext.getCurrentUser() is null");
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
            error.put("error", "缺少必要参数: docId 和 docName");
            return error;
        }

        return openApiTaskService.submitJson2ExcelTask(docId, docName, sheetName, columnOrder, userId, userToken);
    }
}
