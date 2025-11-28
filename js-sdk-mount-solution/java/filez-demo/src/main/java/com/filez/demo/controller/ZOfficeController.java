package com.filez.demo.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.filez.demo.common.aspect.Log;
import com.filez.demo.common.context.UserContext;
import com.filez.demo.common.utils.JwtUtil;
import com.filez.demo.common.utils.PropFilterUtil;
import com.filez.demo.config.DemoConfig;
import com.filez.demo.config.ZOfficeConfig;
import com.filez.demo.model.DocMeta;
import com.filez.demo.service.DocService;
import com.filez.demo.service.SysUserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("${demo.context}") // Default value: /v2/context
@Api(tags = "Integration Controller")
public class ZOfficeController {

    @Resource
    private ZOfficeConfig zOfficeConfig;
    @Resource
    private DemoConfig demoConfig;
    @Resource
    private DocService docService;
    @Resource
    private SysUserService sysUserService;

    @Value("${server.port}")
    private Integer serverPort;

    @Log("Open Document Page")
    @ApiOperation(value = "Open Document Page to requesting ZOffice service")
    @GetMapping(value = "/openDoc", produces = "text/plain;charset=UTF-8")
    public String getOpenDocUrl(@RequestParam(name = "docId") String docId,
                                                 @RequestParam(name = "action", defaultValue = "view") String action, Model model) throws Exception {

        log.info("open doc: {}", docId);
        DocMeta docMeta = docService.findDocMetaById(docId);
        Map<String, Object> config = new HashMap<>();
        
        Map<String, Object> userinfo = new HashMap<>();
        userinfo.put("id", UserContext.getCurrentUser().getId());
        userinfo.put("display_name", UserContext.getCurrentUser().getName());
        config.put("userinfo", userinfo);
        
        config.put("meta",  JSON.parse(JSON.toJSONString(docMeta, new PropFilterUtil())));

        Map<String, Object> repoConfig = new HashMap<>();
        repoConfig.put("id", demoConfig.getRepoId());
        String fileUrl = this.getFileUrl(docMeta.getId());
        repoConfig.put("downloadUrl", fileUrl);
        repoConfig.put("uploadUrl", fileUrl);
        Map<String, String> params = new HashMap<>();
        params.put(demoConfig.getTokenName(), UserContext.getCurrentUserToken());
        repoConfig.put("params", params);
        config.put("repoConfig", repoConfig);
        
        Map<String, String> openConfig = new HashMap<>();
        openConfig.put("action", action);
        config.put("openConfig", openConfig);
        config.put("documentServerAddr", this.getDocumentServerAddr());
        config.put("token", this.createToken(config));
        
        model.addAttribute("config", JSON.toJSONString(config));
	    return "/zOffice";
    }

    private String createToken(Map<String, Object> payloadClaims) {
        try {
            String secret = zOfficeConfig.getApp().getSecret();
            Date expireDate = new Date(System.currentTimeMillis() + Duration.ofHours(2).toMillis());
            return Jwts.builder()
            .setClaims(payloadClaims)
            .setIssuedAt(new Date())
            .setExpiration(expireDate)
            .signWith(SignatureAlgorithm.HS256, secret.getBytes())
            .compact();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getDocumentServerAddr() {
        URIBuilder builder = new URIBuilder().setScheme(zOfficeConfig.getSchema()).setHost(zOfficeConfig.getHost());
        if (zOfficeConfig.getPort() != 80) {
            builder.setPort(zOfficeConfig.getPort());
        }
        return builder.toString();
    }


    private String getFileUrl(String docId) {
        URIBuilder builder = new URIBuilder().setScheme(demoConfig.getSchema()).setHost(demoConfig.getHost());
        if (serverPort != 80) {
            builder.setPort(serverPort);
        }
        builder.setPath(demoConfig.getContext() + "/" + docId + "/content");
        return builder.toString();
    }

    @Log("Document platform requests file download")
    @ApiOperation(value = "/v2/context/{docId}/content: Download specified file interface;")
    @GetMapping(path = "/{docId}/content")
    public void getDocContent(@PathVariable @ApiParam(value = "Specified file ID. Note that the file ID cannot contain colon ':'") String docId,
                              @RequestParam(defaultValue = "latest") @ApiParam(value = "File version, if not filled, it is the latest version") String version,
                              @RequestParam(defaultValue = "false") @ApiParam(value = "Whether it needs to be downloaded as an attachment (rather than online preview)") boolean download,
                              HttpServletResponse response, HttpServletRequest request) throws Exception {

        // The logic of whether to allow download is implemented by the business system itself
        if (!docService.isAllowedAccess(docId)) {
            log.error("{} has no permission to access file {}", Objects.requireNonNull(UserContext.getCurrentUser()).getEmail(), docId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        this.checkFilezOfficeToken(request.getHeader("Authorization"));

        // Get file by docId and version
        try (InputStream inputStream = docService.getDocById(docId);
             ServletOutputStream outputStream = response.getOutputStream()) {
            if (inputStream == null) {
                log.error("File does not exist, download failed");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setStatus(200);
            if (download) {
                ContentDisposition attachment = ContentDisposition
                        .builder("attachment")
                        .filename(docService.findDocMetaById(docId).getName(), StandardCharsets.UTF_8)
                        .build();
                log.info("Attachment info: {}", attachment);
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, attachment.toString());
            }
            IOUtils.copy(inputStream, outputStream);
            response.flushBuffer();
        } catch (IOException e) {
            log.error("Download failed", e);
        }
    }

    @Log("Document platform returns latest document interface")
    @ApiOperation(value = "/v2/context/{docId}/content: ZOffice returns user-edited files;")
    @ResponseBody
    @PostMapping(path = "/{docId}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String publishDoc(@PathVariable String docId,
                             @RequestParam("file") @ApiParam(value = "User-edited file") MultipartFile multipartFile,
                             HttpServletResponse response, HttpServletRequest request) {

        // The logic of whether to allow download is implemented by the business system itself
        if (!docService.isAllowedAccess(docId)) {
            log.error("User {} has no permission to access file {}", Objects.requireNonNull(UserContext.getCurrentUser()).getEmail(), docId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        this.checkFilezOfficeToken(request.getHeader("Authorization"));

        DocMeta docMeta = docService.findDocMetaById(docId);
        if (multipartFile == null) {
            return JSON.toJSONString(docMeta);
        }

        try {
            DocMeta newDocMeta = docService.uploadFile(docId, multipartFile.getInputStream());
            long time = docMeta.getModifiedAt().getTime();
            if (time < newDocMeta.getModifiedAt().getTime()) {
                log.info("File upload successful, save time: {}", newDocMeta.getModifiedAt());
                return JSON.toJSONString(newDocMeta);
            }
            log.info("File save failed");
        } catch (IOException e) {
            log.error("Upload file failed", e);
        }

        return JSON.toJSONString(docMeta);
    }

    /**
     * The three-party system can verify whether the Filez token is valid, or rely on its own token
     * @param token
     */
    private void checkFilezOfficeToken(String token) {
        if (token != null) {
            String[] array = token.split(" ");
            if (array.length > 1) {
                Claims claims = JwtUtil.parseToken(array[1], zOfficeConfig.getApp().getSecret());
                if (claims != null) {
                    log.info("Get Filez token, claims " + JSON.toJSONString(claims));
                } else {
                    log.warn("Invalid Filez token");
                }
            }
        }
    }
}
