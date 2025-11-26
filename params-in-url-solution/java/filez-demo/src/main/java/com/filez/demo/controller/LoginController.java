package com.filez.demo.controller;

import com.filez.demo.common.aspect.Log;
import com.filez.demo.common.utils.JwtUtil;
import com.filez.demo.config.DemoConfig;
import com.filez.demo.entity.SysUserEntity;
import com.filez.demo.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

@Controller
@Slf4j
@Api(tags = "Login Controller")
public class LoginController {

    @Resource
    private SysUserService sysUserService;
    @Resource
    private DemoConfig demoConfig;
    @Value("${demo.token-name}")
    private String tokenName;

    @Log("Login redirect")
    @ApiOperation(value = "Navigate to login page")
    @GetMapping("/")
    public String index(Model model) {
        return getLoginAttr(model);
    }

    @Log("Login redirect")
    @ApiOperation(value = "Navigate to login page")
    @GetMapping("/login")
    public String login(Model model) {
        return getLoginAttr(model);
    }

    @Log("Logout interface")
    @ApiOperation(value = "Logout")
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(tokenName, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/login";
    }


    @Log("User login")
    @ApiOperation(value = "User login")
    @PostMapping("/login")
    public String login(String username, String password, HttpServletResponse response, Model model) {
        SysUserEntity user = sysUserService.getUserByNameAndPwd(username, password);
        if (user == null) {
            // Login failed
            model.addAttribute("display", "invalid username and password");
            return getLoginAttr(model);
        }

        // Set Authorization and cookie (choose one)
        String token = JwtUtil.generateToken(user);
        response.setHeader("Authorization", "bearer " + token);
        response.addCookie(new Cookie(tokenName, token));
        return "redirect:/home/";
    }

    @NotNull
    private String getLoginAttr(Model model) {
        model.addAttribute("username", demoConfig.getAdmin().getUname());
        model.addAttribute("pwd", demoConfig.getAdmin().getPwd());
        return "login";
    }

}
