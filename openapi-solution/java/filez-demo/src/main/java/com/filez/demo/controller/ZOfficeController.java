package com.filez.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.filez.demo.common.aspect.Log;
import com.filez.demo.common.context.UserContext;
import com.filez.demo.common.utils.HmacUtil;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping("${demo.context}") // 默认值：/v2/context
@Api(tags = "集成控制器")
public class ZOfficeController {

    @Resource
    private ZOfficeConfig zOfficeConfig;
    @Resource
    private DemoConfig demoConfig;
    @Resource
    private DocService docService;
    @Resource
    private SysUserService sysUserService;

    @Log("拼接前端集成url")
    @ApiOperation(value = "拼接前端集成url,用于请求zOffice服务")
    @GetMapping(value = "/driver-cb", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> getDriverCbUrl(@RequestParam(name = "docId") String docId,
                                                 @RequestParam(name = "action", defaultValue = "view") String action,
                                                 @RequestParam(defaultValue = "false") boolean isInFrame,
                                                 @RequestParam(required = false) String version) throws Exception {

	    if (!zOfficeConfig.getApp().getFeIntegration().isEnable()) {
		    log.info("使用标准集成方式");
		    return standardMethodOpenDoc(docId, action, version);
	    }

        URIBuilder builder = new URIBuilder().setScheme(zOfficeConfig.getSchema()).setHost(zOfficeConfig.getHost());
        if (zOfficeConfig.getPort() != 80) {
            builder.setPort(zOfficeConfig.getPort());
        }
        builder.setPath(zOfficeConfig.getContext());
        log.info("前端集成基础URL: {}, version: {}", builder, version);

        // 注意：请保证下面传参的顺序一致，否则会报错
        builder.addParameter("repoId", demoConfig.getRepoId());
        builder.addParameter("action", action);
        builder.addParameter("docId", docId);

        // 1、传输用户信息
        Encoder encoder = Base64.getEncoder();
        Profile profile = Profile.convertUserToProfile(Objects.requireNonNull(UserContext.getCurrentUser()));
        String userinfo = encoder.encodeToString((JSON.toJSONString(profile).getBytes()));
        builder.addParameter("userinfo", userinfo);

        // 2、传输文档元信息（版本预览时覆盖 modified_at 和 version）
        DocMeta docMeta = docService.findDocMetaById(docId);
        boolean hasVersion = version != null && !version.isEmpty() && !"latest".equalsIgnoreCase(version);
        if (hasVersion) {
            try {
                long versionTs = Long.parseLong(version);
                docMeta.setModifiedAt(new java.util.Date(versionTs));
                docMeta.setVersion(version);
            } catch (NumberFormatException e) {
                log.warn("[getDriverCbUrl] invalid version format: {}", version);
            }
        }
        String metaJson = JSON.toJSONString(docMeta, new PropFilterUtil());
        String metainfo = encoder.encodeToString(metaJson.getBytes());
        builder.addParameter("meta", metainfo);

        // 3、下载地址，版本预览时在 URL 上附加 version 参数
        String downloadOrUploadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(demoConfig.getContext().substring(1) + "/{docId}/content")
                .buildAndExpand(docId)
                .toUriString()
                .replace("localhost", demoConfig.getHost());
        if (hasVersion) {
            downloadOrUploadUrl += "?version=" + version;
        }
        // 4、文件上传、下载地址
        builder.addParameter("downloadUrl", downloadOrUploadUrl);
        builder.addParameter("uploadUrl", downloadOrUploadUrl);

        // 5、请求体的认证信息，还可附加额外参数
        String header = String.format("%s=%s;param-1=aaa;x-param-2=bbb", demoConfig.getTokenName(), UserContext.getCurrentUserToken());
        builder.addParameter("params", header);

        // 5.5、版本号参数，用于预览指定版本的文档（需在 HMAC 计算前添加）
        if (hasVersion) {
            builder.addParameter("version", version);
        }

        // 6、当前时间戳，zoffice会校验时效性
        builder.addParameter("ts", System.currentTimeMillis() + "");

        // 7、计算hmac
        String hmac = HmacUtil.hmac(builder.build(), zOfficeConfig.getApp().getSecret());
        builder.addParameter("HMAC", hmac);
        log.info("前端集成完整URL: {}", builder);

        if (isInFrame) {
	        return ResponseEntity.ok().body(String.format("/home/iframe?url=%s", URLEncoder.encode(builder.toString(), "UTF-8")));
        }
	    return ResponseEntity.ok().body(builder.toString());
    }

	/**
	 * 使用标准集成方式打开文件，支持 version 参数预览指定版本
	 */
	private ResponseEntity<String> standardMethodOpenDoc(String docId, String action, String version) {
		URIBuilder builder = new URIBuilder().setScheme(zOfficeConfig.getSchema()).setHost(zOfficeConfig.getHost());
		if (zOfficeConfig.getPort() != 80) {
			builder.setPort(zOfficeConfig.getPort());
		}
		builder.setPath("/docs/app/" + demoConfig.getRepoId()+ "/" + docId + "/" + action + "/content");
		builder.addParameter(demoConfig.getTokenName(), UserContext.getCurrentUserToken());
		if (version != null && !version.isEmpty() && !"latest".equalsIgnoreCase(version)) {
			builder.addParameter("version", version);
		}
		return ResponseEntity.ok().body(builder.toString());
	}

	@Log("文档中台请求下载文件")
    @ApiOperation(value = "/v2/context/{docId}/content：下载指定文件接口；如果有自定义实现的下载接口，放入上面的集成url中即可，无需额外实现")
    @GetMapping(path = "/{docId}/content")
    public void getDocContent(@PathVariable @ApiParam(value = "指定文件 Id。注意文件 Id 中不能含有冒号':'") String docId,
                              @RequestParam(defaultValue = "latest") @ApiParam(value = "文件版本，不填则是最新版本") String version,
                              @RequestParam(defaultValue = "false") @ApiParam(value = "是否需要作为附件下载（而非在线预览）") boolean download,
                              HttpServletResponse response) {

        // 是否允许下载的逻辑由业务系统自行实现
        if (!docService.isAllowedAccess(docId)) {
            log.error("{}无权访问文件{}", Objects.requireNonNull(UserContext.getCurrentUser()).getEmail(), docId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // 通过docId和version获取文件（支持按版本下载历史版本）
        try (InputStream inputStream = docService.getDocByIdAndVersion(docId, version);
             ServletOutputStream outputStream = response.getOutputStream()) {
            if (inputStream == null) {
                log.error("文件不存在，下载失败");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setStatus(200);
            if (download) {
                ContentDisposition attachment = ContentDisposition
                        .builder("attachment")
                        .filename(docService.findDocMetaById(docId).getName(), StandardCharsets.UTF_8)
                        .build();
                log.info("附件信息：{}", attachment);
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, attachment.toString());
            }
            IOUtils.copy(inputStream, outputStream);
            response.flushBuffer();
        } catch (IOException e) {
            log.error("下载失败", e);
        }
    }

    @Log("文档中台返回最新的文档接口")
    @ApiOperation(value = "/v2/context/{docId}/content：zoffice返回用户编辑后的文件；如果有自定义实现的上传接口，放入上面的集成url中即可，无需额外实现")
    @ResponseBody
    @PostMapping(path = "/{docId}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String publishDoc(@PathVariable String docId,
                             @RequestParam("file") @ApiParam(value = "用户编辑后的文件") MultipartFile multipartFile,
                             HttpServletResponse response) {

        // 是否允许下载的逻辑由业务系统自行实现
        if (!docService.isAllowedAccess(docId)) {
            log.error("用户{}无权访问文件{}", Objects.requireNonNull(UserContext.getCurrentUser()).getEmail(), docId);
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
                log.info("文件上传成功，保存时间：{}", newDocMeta.getModifiedAt());
                return JSON.toJSONString(newDocMeta);
            }
            log.info("文件保存失败");
        } catch (IOException e) {
            log.error("上传文件失败", e);
        }

        return JSON.toJSONString(docMeta);
    }

	@Log("获取文档信息，此api用于查询文档的meta信息")
	@ApiOperation(value = "/v2/context/{docId}/meta：获取文档信息接口；如果提前在url中提供，则不需要此方法")
	@GetMapping(path = "/{docId}/meta", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getDocMeta(@PathVariable String docId,
	                         @RequestParam(defaultValue = "latest") String version,
	                         @RequestParam(defaultValue = "edit") String action,
	                         HttpServletResponse response) {
		// 是否允许查询的逻辑由业务系统自行实现
		if (!docService.isAllowedAccess(docId)) {
			log.error("用户{}无权查询文件{}", Objects.requireNonNull(UserContext.getCurrentUser()).getEmail(), docId);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}

		DocMeta docMeta = docService.findDocMetaById(docId);

		// 请求特定历史版本时，将 modifiedAt 和 version 替换为版本对应的值，
		// 确保 luoshu-server 为不同版本创建不同的 draft key
		if (docMeta != null && version != null && !"latest".equalsIgnoreCase(version) && !version.isEmpty()) {
			try {
				long versionTs = Long.parseLong(version);
				docMeta.setModifiedAt(new Date(versionTs));
				docMeta.setVersion(version);
				log.info("[getDocMeta] override modifiedAt and version to {} for doc {}", version, docId);
			} catch (NumberFormatException e) {
				log.warn("[getDocMeta] invalid version format: {}", version);
			}
		}

		return JSON.toJSONString(docMeta, new PropFilterUtil());
	}

    @Log("获取用户信息接口，此api用于查询哪些用户有编辑权限等")
    @ApiOperation(value = "/v2/context/profiles：获取用户信息接口；如果不需要指定哪些人有编辑权限，则不需要此方法")
    @ResponseBody
    @GetMapping(path = "/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getUserProfile(
            @RequestParam(required = false) @ApiParam(value = "用户id") String userid,
            @RequestParam(required = false) @ApiParam(value = "用户id数组") String[] users,
            @RequestParam(required = false) @ApiParam(value = "关键字") String keyword,
            @RequestParam(required = false) @ApiParam(value = "第几页") String page_num,
            @RequestParam(required = false) @ApiParam(value = "每页大小") String[] page_limit) {

        // 根据用户名分页查询用户信息，此处简化查询逻辑
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
     * TODO【可选接口-1】
     * 当文档从没有人编辑到有人编辑，或者从有人编辑到所有人都退出编辑时，把状态告诉第三方服务。文档由docId来指定。
     */
    @Log("文档打开状态通知接口")
    @ApiOperation(value = "/v2/context/${docId}/notify：接收zOffice的编辑或关闭文档提醒；如果不需要则无需实现此方法")
    @ResponseBody
    @PostMapping(path = "/{docId}/notify", produces = MediaType.APPLICATION_JSON_VALUE)
    public String docsNotify(@PathVariable String docId, HttpServletRequest request) {

        String line, body = "";
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                body += line;
            }
        } catch (IOException e) {
            log.error("读取body失败", e);
        }

        // body字符串示例： '{"docId":"demo-doc","repoId":"thirdparty-rest","type":"edit.session.close"}'
        Notify notify = JSON.parseObject(body, Notify.class);
        log.info("来自zOffice notify: {}", notify);
        return notify.toString();
    }

    /**
     * TODO【可选接口-2】
     * 当有人给文档增加和修改批注时，告诉第三方服务。文档由docId来指定。
     */
    @Log("文档批注通知接口")
    @ApiOperation(value = "/v2/context/{docId}/mention：接收zOffice的批注变更通知；如果不需要则无需实现此方法")
    @PostMapping("/{docId}/mention")
    @ResponseBody
    public String mention(@PathVariable String docId, HttpServletRequest request) throws IOException {
        // 注意： body是以字符串的形式传递
        String body = IOUtils.toString(request.getReader());
        Mention mention = JSON.parseObject(body, Mention.class);
        log.info("来自zoffice的提醒：docId:{} mention from zOffice {} ", docId, mention);
        return "success";
    }

    /**
     * TODO【可选接口-3】
     * 调用jsSDK的另存为方法时，检查一下是否可以在指定的位置接受这个文件
     */
    @Log("文档另存预检接口")
    @ApiOperation(value = "/v2/context/files/content：zoffice调用jsSDK的另存为方法前检查业务系统是否支持另存；如果不需要则无需实现此方法")
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
     * 获取指定文档的历史版本列表
     * @param docId 文档ID
     * @return 版本号列表（时间戳），按时间倒序排列
     */
    @Log("获取文档版本列表")
    @ApiOperation(value = "/v2/context/{docId}/versions：获取指定文档的历史版本列表")
    @GetMapping(path = "/{docId}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getDocVersions(@PathVariable String docId) {
        List<String> versions = docService.listVersions(docId);
        return JSON.toJSONString(versions);
    }

    /**
     * 删除指定文档的某个历史版本
     * @param docId 文档ID
     * @param version 版本号（时间戳字符串），不允许删除 latest
     */
    @Log("删除文档历史版本")
    @ApiOperation(value = "/v2/context/{docId}/versions/{version}：删除指定历史版本")
    @DeleteMapping(path = "/{docId}/versions/{version}")
    @ResponseBody
    public ResponseEntity<String> deleteDocVersion(@PathVariable String docId, @PathVariable String version) {
        if ("latest".equalsIgnoreCase(version)) {
            return ResponseEntity.badRequest().body("不允许删除最新版本");
        }
        boolean success = docService.deleteVersion(docId, version);
        if (success) {
            return ResponseEntity.ok("删除成功");
        }
        return ResponseEntity.status(404).body("版本不存在或删除失败");
    }

    /**
     * 文档对比接口，支持两种比对方式：
     * 1. AI比对（需要服务端开启ai-business许可）：支持 doc/docx + PDF 以及跨格式比对
     * 2. Aspose比对（默认）：仅支持 doc/docx
     *
     * 版本比对规则：
     * - 不同文档比对（docAid != docBid）：versionA/versionB 可选，不传则取最新版
     * - 同文档不同版本比对（docAid == docBid）：versionA 和 versionB 必须都传，且不能相同
     *
     * @param docAid 文档A的ID
     * @param docBid 文档B的ID
     * @param versionA 文档A的版本号（可选，不传则取最新版）
     * @param versionB 文档B的版本号（可选，不传则取最新版）
     */
    @Log("拼接文档对比url")
    @ApiOperation(value = "/v2/context/compareDoc：拼接文档对比url，支持不同文档比对和同文档不同版本比对")
    @GetMapping(value = "/compareDoc", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> compareDoc(
            @ApiParam(value = "文档A的ID") String docAid,
            @ApiParam(value = "文档B的ID") String docBid,
            @ApiParam(value = "文档A的版本号，同文档比对时必填") @RequestParam(required = false) String versionA,
            @ApiParam(value = "文档B的版本号，同文档比对时必填") @RequestParam(required = false) String versionB,
            @ApiParam(value = "是否使用AI比对，true/false，默认true") @RequestParam(required = false) String isAiCompare) throws Exception {

        // 同文档比对时校验版本号
        if (docAid.equals(docBid)) {
            if (versionA == null || versionA.isEmpty() || versionB == null || versionB.isEmpty()) {
                return ResponseEntity.badRequest().body("同文档比对时versionA和versionB必须都传");
            }
            if (versionA.equals(versionB)) {
                return ResponseEntity.badRequest().body("同文档比对时versionA和versionB不能相同");
            }
        }

		if (!zOfficeConfig.getApp().getFeIntegration().isEnable()) {
			log.info("使用标准集成方式");
			return standardMethodCompare(docAid, docBid, versionA, versionB, isAiCompare);
		}

        URIBuilder builder = new URIBuilder().setScheme(zOfficeConfig.getSchema()).setHost(zOfficeConfig.getHost());
        if (zOfficeConfig.getPort() != 80) {
            builder.setPort(zOfficeConfig.getPort());
        }
        builder.setPath(zOfficeConfig.getContext());
        log.info("文档比对基础URL: {}", builder);

        // 注意：请保证下面传参的顺序一致，否则会报错
        builder.addParameter("repoId", demoConfig.getRepoId());
        builder.addParameter("action", "compare");
        builder.addParameter("docId", docAid);
        builder.addParameter("docIdB", docBid);

        // 1、传输用户信息
        Encoder encoder = Base64.getEncoder();
        Profile profile = Profile.convertUserToProfile(Objects.requireNonNull(UserContext.getCurrentUser()));
        String userinfo = encoder.encodeToString((JSON.toJSONString(profile).getBytes()));
        builder.addParameter("userinfo", userinfo);

        // 2、传输文档元信息（版本比对时需用版本号覆盖 modified_at，否则 server 会认为是同一份文件）
        DocMeta docMeta = docService.findDocMetaById(docAid);
        overrideMetaWithVersion(docMeta, versionA);
        String metaJson = JSON.toJSONString(docMeta, new PropFilterUtil());
        String metainfo = encoder.encodeToString(metaJson.getBytes());
        builder.addParameter("meta", metainfo);
        DocMeta docMetaB = docService.findDocMetaById(docBid);
        overrideMetaWithVersion(docMetaB, versionB);
        String metaJsonB = JSON.toJSONString(docMetaB, new PropFilterUtil());
        String metainfoB = encoder.encodeToString(metaJsonB.getBytes());
        builder.addParameter("metaB", metainfoB);

        // 3、下载地址（同文档不同版本时，下载地址附带version参数以获取对应版本文件）
        String downloadUrlA = buildDownloadUrl(docAid, versionA);
        builder.addParameter("downloadUrl", downloadUrlA);

        // 4、下载地址B
        String downloadUrlB = buildDownloadUrl(docBid, versionB);
        builder.addParameter("downloadUrlB", downloadUrlB);

        // 5、版本号参数（透传给zOffice服务端，用于版本化获取文档元数据）
        if (versionA != null && !versionA.isEmpty()) {
            builder.addParameter("versionA", versionA);
        }
        if (versionB != null && !versionB.isEmpty()) {
            builder.addParameter("versionB", versionB);
        }

        // 5.5、比对引擎选择参数
        if (isAiCompare != null && !isAiCompare.isEmpty()) {
            builder.addParameter("isAiCompare", isAiCompare);
        }

        // 6、请求体的认证信息，还可附加额外参数
        String header = String.format("%s=%s;param-1=aaa;x-param-2=bbb", demoConfig.getTokenName(), UserContext.getCurrentUserToken());
        builder.addParameter("params", header);

        // 7、当前时间戳，zoffice会校验时效性
        builder.addParameter("ts", System.currentTimeMillis() + "");

        // 8、计算hmac（必须在所有参数添加完毕后再计算）
        String hmac = HmacUtil.hmac(builder.build(), zOfficeConfig.getApp().getSecret());
        builder.addParameter("HMAC", hmac);
        log.info("文档比对完整URL: {}", builder);

        return ResponseEntity.ok().body(builder.toString());
    }

    /**
     * 用版本号覆盖 meta 中的 modified_at 和 version 字段。
     * driver-callback 方式下 luoshu-server 用 modified_at 作为 remoteLastModified 标识文档，
     * 不同版本必须有不同的 modified_at 才能被识别为不同内容。
     */
    private void overrideMetaWithVersion(DocMeta meta, String version) {
        if (meta == null || version == null || version.isEmpty() || "latest".equalsIgnoreCase(version)) {
            return;
        }
        try {
            long versionTs = Long.parseLong(version);
            meta.setModifiedAt(new java.util.Date(versionTs));
            meta.setVersion(version);
        } catch (NumberFormatException e) {
            log.warn("[overrideMetaWithVersion] invalid version format: {}", version);
        }
    }

    /**
     * 构造文档下载地址，当指定版本号时附带version参数
     * @param docId 文档ID
     * @param version 版本号，可为null
     * @return 下载URL
     */
    private String buildDownloadUrl(String docId, String version) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(demoConfig.getContext().substring(1) + "/{docId}/content")
                .buildAndExpand(docId)
                .toUriString()
                .replace("localhost", demoConfig.getHost());
        if (version != null && !version.isEmpty()) {
            baseUrl += "?version=" + version;
        }
        return baseUrl;
    }

	/**
	 * 使用标准集成方式比较文档（直接调用 /docs/app/{repoId}/compare 接口）
	 * @param docAid 文档A的ID
	 * @param docBid 文档B的ID
	 * @param versionA 文档A的版本号（可选）
	 * @param versionB 文档B的版本号（可选）
	 * @param isAiCompare 是否使用AI比对（可选，默认true）
	 */
	private ResponseEntity<String> standardMethodCompare(String docAid, String docBid, String versionA, String versionB, String isAiCompare) {
		URIBuilder builder = new URIBuilder().setScheme(zOfficeConfig.getSchema()).setHost(zOfficeConfig.getHost());
		if (zOfficeConfig.getPort() != 80) {
			builder.setPort(zOfficeConfig.getPort());
		}
		builder.setPath("/docs/app/"+ demoConfig.getRepoId()+"/compare");
		builder.addParameter("docA", docAid);
		builder.addParameter("docB", docBid);
		if (versionA != null && !versionA.isEmpty()) {
			builder.addParameter("versionA", versionA);
		}
		if (versionB != null && !versionB.isEmpty()) {
			builder.addParameter("versionB", versionB);
		}
		if (isAiCompare != null && !isAiCompare.isEmpty()) {
			builder.addParameter("isAiCompare", isAiCompare);
		}
		builder.addParameter(demoConfig.getTokenName(), UserContext.getCurrentUserToken());
		return ResponseEntity.ok().body(builder.toString());
	}
}
