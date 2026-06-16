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
@Api(tags = "登录控制器")
public class LoginController {

    @Resource
    private SysUserService sysUserService;
    @Resource
    private DemoConfig demoConfig;
    @Value("${demo.token-name}")
    private String tokenName;

    @Log("登录跳转")
    @ApiOperation(value = "跳转至登录页面")
    @GetMapping("/")
    public String index(Model model) {
        return getLoginAttr(model);
    }

    @Log("登录跳转")
    @ApiOperation(value = "跳转至登录页面")
    @GetMapping("/login")
    public String login(Model model) {
        return getLoginAttr(model);
    }

    @Log("登出接口")
    @ApiOperation(value = "退出登录")
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(tokenName, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/login";
    }


    @Log("用户登录")
    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    public String login(String username, String password, HttpServletResponse response, Model model) {
        SysUserEntity user = sysUserService.getUserByNameAndPwd(username, password);
        if (user == null) {
            // 登录失败
            model.addAttribute("display", "invalid username and password");
            return getLoginAttr(model);
        }

        // 设置Authorization和cookie二选一
        String token = JwtUtil.generateToken(user);
        response.setHeader("Authorization", "bearer " + token);
        response.addCookie(new Cookie(tokenName, token));
        return "redirect:/home/";
    }

    @NotNull
    private String getLoginAttr(Model model) {
        model.addAttribute("username", demoConfig.getAdmin().getUname());
        model.addAttribute("pwd", demoConfig.getAdmin().getPwd());
        return "login/login";
    }

}
