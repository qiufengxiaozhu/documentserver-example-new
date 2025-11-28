package com.filez.demo.controller;

import com.filez.demo.common.aspect.Log;
import com.filez.demo.common.context.UserContext;
import com.filez.demo.entity.SysUserEntity;
import com.filez.demo.model.DocControl;
import com.filez.demo.model.DocMeta;
import com.filez.demo.model.Profile;
import com.filez.demo.service.DocService;
import com.filez.demo.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "Business System Controller")
@Controller
@Slf4j
@RequestMapping("/home")
public class HomeController {

    @Resource
    private DocService docService;
    @Resource
    private SysUserService sysUserService;

    @Log("Navigate to home page")
    @ApiOperation(value = "/home, Navigate to home page")
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("frameUrl", "/home/local");
        return "home";
    }

    @Log("Navigate to third-party file list page")
    @ApiOperation(value = "/home/local, Navigate to third-party file list page")
    @GetMapping("/local")
    public String thirdHome(Model model, HttpServletRequest request) {
        model.addAttribute("parentDirs", new String[]{""});
        model.addAttribute("drive", "local");

        SysUserEntity user = UserContext.getCurrentUser();
        try {
            if (docService != null && user != null) {
                String userId = user.getId();
                String shareUid = "share";
                String adminUid = "admin";
                // Sort by modification time in descending order, if no modification time, sort by creation time in descending order
                model.addAttribute("files", docService.listFiles().stream()
                        .filter(meta -> {
                            if (shareUid.equals(userId) || adminUid.equals(userId)) {
                                return true;
                            }
                            Profile createdBy = meta.getCreatedBy();
                            if (createdBy != null) {
                                return userId.equals(createdBy.getId()) || shareUid.equals(createdBy.getId());
                            }
                            Profile owner = meta.getOwner();
                            return owner != null && (userId.equals(owner.getId()) || shareUid.equals(owner.getId()));
                        }).sorted((o1, o2) -> {
                            if (o1.getModifiedAt() != null && o2.getModifiedAt() != null) {
                                return o2.getModifiedAt().compareTo(o1.getModifiedAt());
                            } else if (o1.getCreatedAt() != null && o2.getCreatedAt() != null) {
                                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                            } else {
                                return 0;
                            }})
                        .collect(Collectors.toList()));
            } else {
                model.addAttribute("files", Collections.emptyList());
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            model.addAttribute("files", Collections.emptyList());
            model.addAttribute("forceLoginWarn", true);
        }

        return "drive";
    }

    @Log("Navigate to batch operation page")
    @ApiOperation(value = "/home/local/batch, Navigate to batch operation page")
    @GetMapping("/local/batch")
    public String localBatchOp(Model model) {
        model.addAttribute("files", docService.listFiles());
        model.addAttribute("drive", "local");

        return "localBatchOp";
    }

    @Log("Navigate to user information page")
    @ApiOperation(value = "/home/user, Navigate to user information page")
    @GetMapping("/user")
    public String user(Model model) {
        SysUserEntity currentUser = UserContext.getCurrentUser();
        if  (currentUser == null) {
            log.warn("User not logged in");
            return "redirect:/login";
        }
        model.addAttribute("user", sysUserService.getUserById(currentUser.getId()));
        return "user";
    }

    @Log("Interface for editing and preview using iframe")
    @ApiOperation(value = "/home/iframe, Return a page for embedding edit preview page")
    @GetMapping("/iframe")
    public String getOfficeServiceUrl(Model model, @RequestParam(name = "url") String url) throws URISyntaxException, UnsupportedEncodingException {

        model.addAttribute("url", url);
        return "/zOffice";
    }

    private static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> queryPairs = new LinkedHashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
            String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
            queryPairs.put(key, value);
        }
        return queryPairs;
    }

    @Log("Update user information interface")
    @ApiOperation(value = "/home/user, Update user information interface")
    @PostMapping("/user")
    public String updateUser(@RequestParam SysUserEntity user) {
        sysUserService.updateUserById(user);
        return "redirect:/home/user";
    }

    @Log("Update document control function interface")
    @ApiOperation(value = "/home/control/{userId}/{docId}, Update document control function interface")
    @PostMapping("/control/{userId}/{docId}")
    @ResponseBody
    public String updateControl(@PathVariable String userId, @PathVariable String docId, DocControl controlVO) {
        docService.updateControl(userId, docId, controlVO);
	return "Update successful";
    }

    @Log("Query document metadata")
    @ApiOperation(value = "/home/meta/{docId}, Query document metadata")
    @GetMapping("/meta/{docId}")
    public String updateMeta(@PathVariable String docId,
                             Model model,
                             @RequestParam(defaultValue = "false") boolean showMsg) {
        SysUserEntity user = UserContext.getCurrentUser();
        if (user != null) {
            model.addAttribute("controlUrl", String.format("/home/control/%s/%s", user.getId(), docId));
            model.addAttribute("control", docService.getControl(user.getId(), docId));
        }
        model.addAttribute("metaUrl", String.format("/meta/%s", docId));
        model.addAttribute("meta", docService.findDocMetaById(docId));
        return "control";
    }

    @Log("Update document metadata")
    @ApiOperation(value = "/home/meta/{docId}, Update document metadata")
    @PostMapping("/meta/{docId}")
    public String updateMeta(@PathVariable String docId, DocMeta docMeta, HttpServletRequest request) {
        docService.updateDocMeta(docMeta);
        log.info("Update successful");
        return "redirect:" + request.getRequestURI();
    }

    @Log("Document comparison")
    @ApiOperation(value = "/compare/, Document comparison")
    @GetMapping("/compare")
    public String compareDoc() {
        return "compare";
    }
}
