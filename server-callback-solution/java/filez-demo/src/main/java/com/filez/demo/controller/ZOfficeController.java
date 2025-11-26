package com.filez.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.filez.demo.common.aspect.Log;
import com.filez.demo.common.context.UserContext;
import com.filez.demo.common.utils.PropFilterUtil;
import com.filez.demo.config.DemoConfig;
import com.filez.demo.config.ZOfficeConfig;
import com.filez.demo.model.DocMeta;
import com.filez.demo.model.Mention;
import com.filez.demo.model.Notify;
import com.filez.demo.model.Profile;
import com.filez.demo.service.DocService;
import com.filez.demo.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Log("Build integration URL")
    @ApiOperation(value = "Build integration URL for requesting ZOffice service")
    @GetMapping(value = "/openDoc", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> getOpenDocUrl(@RequestParam(name = "docId") String docId,
                                                 @RequestParam(name = "action", defaultValue = "view") String action,
                                                 @RequestParam(defaultValue = "false") boolean isInFrame) throws Exception {

        URIBuilder builder = new URIBuilder().setScheme(zOfficeConfig.getSchema()).setHost(zOfficeConfig.getHost());
        if (zOfficeConfig.getPort() != 80) {
            builder.setPort(zOfficeConfig.getPort());
        }
        builder.setPath(zOfficeConfig.getContext() + "/" + demoConfig.getRepoId() + "/" + docId + "/" + action + "/" + "content");
        builder.setParameter(demoConfig.getTokenName(), UserContext.getCurrentUserToken());

        log.info("Integration complete URL: {}", builder);
        
        if (isInFrame) {
	        return ResponseEntity.ok().body(String.format("/home/iframe?url=%s", URLEncoder.encode(builder.toString(), "UTF-8")));
        }
	    return ResponseEntity.ok().body(builder.toString());
    }

    @Log("Document platform requests file download")
    @ApiOperation(value = "/v2/context/{docId}/content: Download specified file interface; if there is a custom download interface implementation, put it in the integration URL above, no additional implementation is needed")
    @GetMapping(path = "/{docId}/content")
    public void getDocContent(@PathVariable @ApiParam(value = "Specified file ID. Note that the file ID cannot contain colon ':'") String docId,
                              @RequestParam(defaultValue = "latest") @ApiParam(value = "File version, if not filled, it is the latest version") String version,
                              @RequestParam(defaultValue = "false") @ApiParam(value = "Whether it needs to be downloaded as an attachment (rather than online preview)") boolean download,
                              HttpServletResponse response) {

        // The logic of whether to allow download is implemented by the business system itself
        if (!docService.isAllowedAccess(docId)) {
            log.error("{} has no permission to access file {}", Objects.requireNonNull(UserContext.getCurrentUser()).getEmail(), docId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

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
    @ApiOperation(value = "/v2/context/{docId}/content: ZOffice returns user-edited files; if there is a custom upload interface implementation, put it in the integration URL above, no additional implementation is needed")
    @ResponseBody
    @PostMapping(path = "/{docId}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String publishDoc(@PathVariable String docId,
                             @RequestParam("file") @ApiParam(value = "User-edited file") MultipartFile multipartFile,
                             HttpServletResponse response) {

        // The logic of whether to allow download is implemented by the business system itself
        if (!docService.isAllowedAccess(docId)) {
            log.error("User {} has no permission to access file {}", Objects.requireNonNull(UserContext.getCurrentUser()).getEmail(), docId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

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

	@Log("Get document information, this API is used to query document meta information")
	@ApiOperation(value = "/v2/context/{docId}/meta: Get document information interface; if provided in advance in the URL, this method is not needed")
	@GetMapping(path = "/{docId}/meta", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getDocMeta(@PathVariable String docId,
	                         @RequestParam(defaultValue = "latest") String version,
	                         @RequestParam(defaultValue = "edit") String action,
	                         HttpServletResponse response) {
		// The logic of whether to allow query is implemented by the business system itself
		if (!docService.isAllowedAccess(docId)) {
			log.error("User {} has no permission to query file {}", Objects.requireNonNull(UserContext.getCurrentUser()).getEmail(), docId);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}

		DocMeta docMeta = docService.findDocMetaById(docId);

		// Convert document metadata to JSON string and return
		return JSON.toJSONString(docMeta, new PropFilterUtil());
	}

    @Log("Get user information interface, this API is used to query which users have editing permissions, etc.")
    @ApiOperation(value = "/v2/context/profiles: Get user information interface; if you don't need to specify which people have editing permissions, this method is not needed")
    @ResponseBody
    @GetMapping(path = "/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getUserProfile(
            @RequestParam(required = false) @ApiParam(value = "User ID") String userid,
            @RequestParam(required = false) @ApiParam(value = "User ID array") String[] users,
            @RequestParam(required = false) @ApiParam(value = "Keyword") String keyword,
            @RequestParam(required = false) @ApiParam(value = "Page number") String page_num,
            @RequestParam(required = false) @ApiParam(value = "Page size") String[] page_limit) {

        // Query user information by username with pagination, simplified query logic here
        if (Objects.nonNull(keyword) || Objects.nonNull(page_num) || Objects.nonNull(page_limit)) {
            List<Profile> profiles = sysUserService.getAllUser()
                    .stream()
                    .map(Profile::convertUserToProfile)
                    .collect(Collectors.toList());

            JSONObject result = new JSONObject();
            result.put("total", profiles.size());
            result.put("items", profiles);
            return result.toJSONString();
        }

        if (Objects.isNull(users)) {
            return JSON.toJSONString(Profile.convertUserToProfile(UserContext.getCurrentUser()));
        }

        List<Profile> profileLists = Arrays.stream(users)
                .map(sysUserService::getUserById)
                .filter(Objects::nonNull)
                .map(Profile::convertUserToProfile)
                .collect(Collectors.toList());

        JSONObject result = new JSONObject();
        result.put("total", profileLists.size());
        result.put("list", profileLists);
        return result.toJSONString();
    }

    /**
     * TODO [Optional Interface-1]
     * When a document changes from no one editing to someone editing, or from someone editing to everyone exiting editing, tell the third-party service the status. The document is specified by docId.
     */
    @Log("Document open status notification interface")
    @ApiOperation(value = "/v2/context/${docId}/notify: Receive ZOffice's edit or close document reminders; if not needed, this method does not need to be implemented")
    @ResponseBody
    @PostMapping(path = "/{docId}/notify", produces = MediaType.APPLICATION_JSON_VALUE)
    public String docsNotify(@PathVariable String docId, HttpServletRequest request) {

        String line, body = "";
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                body += line;
            }
        } catch (IOException e) {
            log.error("Failed to read body", e);
        }

        // Body string example: '{"docId":"demo-doc","repoId":"thirdparty-rest","type":"edit.session.close"}'
        Notify notify = JSON.parseObject(body, Notify.class);
        log.info("Notify from ZOffice: {}", notify);
        return notify.toString();
    }

    /**
     * TODO [Optional Interface-2]
     * When someone adds and modifies comments to a document, tell the third-party service. The document is specified by docId.
     */
    @Log("Document comment notification interface")
    @ApiOperation(value = "/v2/context/{docId}/mention: Receive ZOffice's comment change notifications; if not needed, this method does not need to be implemented")
    @PostMapping("/{docId}/mention")
    @ResponseBody
    public String mention(@PathVariable String docId, HttpServletRequest request) throws IOException {
        // Note: body is passed as a string
        String body = IOUtils.toString(request.getReader());
        Mention mention = JSON.parseObject(body, Mention.class);
        log.info("Reminder from ZOffice: docId:{} mention from ZOffice {} ", docId, mention);
        return "success";
    }

    /**
     * TODO [Optional Interface-3]
     * When calling the jsSDK's save as method, check whether this file can be accepted at the specified location
     */
    @Log("Document save as preflight interface")
    @ApiOperation(value = "/v2/context/files/content: ZOffice calls jsSDK's save as method to check whether the business system supports save as before; if not needed, this method does not need to be implemented")
    @RequestMapping(value = "/files/content", method = RequestMethod.OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String preflightCheck(@RequestBody String msg, HttpServletResponse response) throws IOException {

        JSONObject saveAsMsg = JSON.parseObject(msg);
        String name = (String) saveAsMsg.get("name");
        String path = (String) saveAsMsg.get("parentPathName");
        log.info("name: {}, path: {}", name, path);

        DocMeta docMeta = docService.makeNewFile(name, path);
        JSONObject jsonObject = new JSONObject();
        if (docMeta == null) {
            response.setStatus(409);
            jsonObject.put("error", "preflight check fail");
            return jsonObject.toJSONString();
        }

        return JSON.toJSONString(docMeta);
    }

    /**
     * TODO [Optional Interface-4]
     * Document comparison API is used to compare the differences between two text documents with similar content. Users need to provide two text documents with similar content.
     * For example, document A and document B, the comparison results are provided in HTML page format. The HTML page displays the original document and the comparison document, as well as their differences.
     */
    @Log("Build document comparison URL")
    @ApiOperation(value = "/v2/context/compareDoc: Build document comparison URL")
    @GetMapping(value = "/compareDoc", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> compareDoc(@ApiParam(value = "Document A ID") String docAid,
                                             @ApiParam(value = "Document B ID") String docBid) throws Exception {

        URIBuilder builder = new URIBuilder().setScheme(zOfficeConfig.getSchema()).setHost(zOfficeConfig.getHost());
        if (zOfficeConfig.getPort() != 80) {
            builder.setPort(zOfficeConfig.getPort());
        }
        builder.setPath(zOfficeConfig.getContext() + "/" + demoConfig.getRepoId() + "/compare");
        builder.addParameter("docA", docAid);
        builder.addParameter("docB", docBid);
        builder.addParameter(demoConfig.getTokenName(), UserContext.getCurrentUserToken());

        log.info("Document comparison complete URL: {}", builder);

        return ResponseEntity.ok().body(builder.toString());
    }
}
