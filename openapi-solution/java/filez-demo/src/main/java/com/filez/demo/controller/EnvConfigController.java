package com.filez.demo.controller;

import com.filez.demo.common.aspect.Log;
import com.filez.demo.config.DemoConfig;
import com.filez.demo.config.PublicApiConfig;
import com.filez.demo.config.ZOfficeConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 环境配置控制器
 * 支持在页面上动态查看和修改运行时配置，无需重启
 * 修改仅影响当前运行实例的内存状态，不会持久化到 yml 文件
 */
@Api(tags = "环境配置控制器")
@Controller
@Slf4j
@RequestMapping("/home/envConfig")
public class EnvConfigController {

    @Resource
    private ZOfficeConfig zOfficeConfig;

    @Resource
    private DemoConfig demoConfig;

    @Resource
    private PublicApiConfig publicApiConfig;

    @Value("${server.port:8000}")
    private int serverPort;

    /**
     * 环境配置页面
     */
    @Log("环境配置页面")
    @ApiOperation(value = "/home/envConfig：环境配置页面")
    @GetMapping("")
    public String envConfigPage() {
        return "config/envConfig";
    }

    /**
     * 获取当前所有运行时配置
     */
    @ApiOperation(value = "/home/envConfig/get：获取当前运行时配置")
    @GetMapping("/get")
    @ResponseBody
    public Map<String, Object> getConfig() {
        Map<String, Object> result = new LinkedHashMap<>();

        Map<String, Object> zoffice = new LinkedHashMap<>();
        zoffice.put("schema", zOfficeConfig.getSchema());
        zoffice.put("host", zOfficeConfig.getHost());
        zoffice.put("port", zOfficeConfig.getPort());
        zoffice.put("context", zOfficeConfig.getContext());
        zoffice.put("cors", zOfficeConfig.isCors());
        zoffice.put("appSecret", zOfficeConfig.getApp() != null ? zOfficeConfig.getApp().getSecret() : "");
        zoffice.put("feIntegrationEnable", zOfficeConfig.getApp() != null
                && zOfficeConfig.getApp().getFeIntegration() != null
                && zOfficeConfig.getApp().getFeIntegration().isEnable());
        result.put("zoffice", zoffice);

        Map<String, Object> demo = new LinkedHashMap<>();
        demo.put("host", demoConfig.getHost());
        demo.put("context", demoConfig.getContext());
        demo.put("repoId", demoConfig.getRepoId());
        demo.put("tokenName", demoConfig.getTokenName());
        demo.put("serverPort", serverPort);
        result.put("demo", demo);

        Map<String, Object> publicapi = new LinkedHashMap<>();
        publicapi.put("appId", publicApiConfig.getAppId());
        publicapi.put("secret", publicApiConfig.getSecret());
        publicapi.put("context", publicApiConfig.getContext());
        result.put("publicapi", publicapi);

        return result;
    }

    /**
     * 动态更新运行时配置
     * 仅修改内存中的 Bean 属性，不持久化到文件
     */
    @Log("动态更新运行时配置")
    @ApiOperation(value = "/home/envConfig/save：动态更新运行时配置")
    @PostMapping("/save")
    @ResponseBody
    public Map<String, Object> saveConfig(@RequestBody Map<String, Map<String, Object>> payload) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (payload.containsKey("zoffice")) {
                Map<String, Object> z = payload.get("zoffice");
                applyZOfficeConfig(z);
            }
            if (payload.containsKey("demo")) {
                Map<String, Object> d = payload.get("demo");
                applyDemoConfig(d);
            }
            if (payload.containsKey("publicapi")) {
                Map<String, Object> p = payload.get("publicapi");
                applyPublicApiConfig(p);
            }
            log.info("[EnvConfigController] config updated successfully");
            result.put("success", true);
        } catch (Exception e) {
            log.error("[EnvConfigController] config update failed", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    private void applyZOfficeConfig(Map<String, Object> z) {
        if (z.containsKey("schema")) zOfficeConfig.setSchema(str(z.get("schema")));
        if (z.containsKey("host")) zOfficeConfig.setHost(str(z.get("host")));
        if (z.containsKey("port")) zOfficeConfig.setPort(toInt(z.get("port")));
        if (z.containsKey("context")) zOfficeConfig.setContext(str(z.get("context")));
        if (z.containsKey("cors")) zOfficeConfig.setCors(toBool(z.get("cors")));
        if (z.containsKey("appSecret") && zOfficeConfig.getApp() != null) {
            zOfficeConfig.getApp().setSecret(str(z.get("appSecret")));
        }
        if (z.containsKey("feIntegrationEnable") && zOfficeConfig.getApp() != null
                && zOfficeConfig.getApp().getFeIntegration() != null) {
            zOfficeConfig.getApp().getFeIntegration().setEnable(toBool(z.get("feIntegrationEnable")));
        }
    }

    private void applyDemoConfig(Map<String, Object> d) {
        if (d.containsKey("host")) demoConfig.setHost(str(d.get("host")));
        if (d.containsKey("context")) demoConfig.setContext(str(d.get("context")));
        if (d.containsKey("repoId")) demoConfig.setRepoId(str(d.get("repoId")));
        if (d.containsKey("tokenName")) demoConfig.setTokenName(str(d.get("tokenName")));
    }

    private void applyPublicApiConfig(Map<String, Object> p) {
        if (p.containsKey("appId")) publicApiConfig.setAppId(str(p.get("appId")));
        if (p.containsKey("secret")) publicApiConfig.setSecret(str(p.get("secret")));
        if (p.containsKey("context")) publicApiConfig.setContext(str(p.get("context")));
    }

    private String str(Object v) {
        return v != null ? v.toString() : "";
    }

    private int toInt(Object v) {
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; }
    }

    private boolean toBool(Object v) {
        if (v instanceof Boolean) return (Boolean) v;
        return "true".equalsIgnoreCase(String.valueOf(v));
    }
}
