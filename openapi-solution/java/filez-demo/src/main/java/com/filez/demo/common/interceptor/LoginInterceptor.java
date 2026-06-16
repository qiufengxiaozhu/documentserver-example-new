package com.filez.demo.common.interceptor;

import com.filez.demo.common.context.UserContext;
import com.filez.demo.common.utils.JwtUtil;
import com.filez.demo.config.DemoConfig;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
		String reqUri = request.getRequestURI();

		// 放行登录页
		if ("/".equals(reqUri) || reqUri.startsWith("/login") || reqUri.startsWith("/ai")) {
			return true;
		}

		// 放行 server 回调接口（luoshu-server 使用自己的鉴权方式）
		if (reqUri.contains("/tasks/callback")) {
			return true;
		}

		// 静态资源不拦截（使用扩展名判断，避免 contains("js") 误匹配 "json" 等路径）
		if (reqUri.endsWith(".js") || reqUri.endsWith(".css") || reqUri.endsWith(".svg")
				|| reqUri.contains("/img/") || reqUri.contains("/fonts/") || reqUri.contains("favicon.ico")) {
			return true;
		}

		// 放行 Knife4j 所需的资源路径
		if (reqUri.startsWith("/webjars/")
				|| reqUri.startsWith("/swagger-resources")
				|| reqUri.startsWith("/v2/api-docs")
				|| reqUri.startsWith("/v3/api-docs")
				|| reqUri.startsWith("/doc.html")
				|| reqUri.startsWith("/swagger-ui.html")
				|| reqUri.startsWith("/swagger-ui/")
				|| reqUri.startsWith("/favicon.ico")
				|| reqUri.startsWith("/js/")
				|| reqUri.startsWith("/css/")
				|| reqUri.startsWith("/img/")
				|| reqUri.startsWith("/fonts/")
				|| reqUri.startsWith("/svg/")
				|| reqUri.startsWith("/knife4j/")
				|| reqUri.startsWith("/static/")) {
			return true;
		}

		WebApplicationContext webAppCtx = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
		String tokenName = "zdocs_access_token";
		if (webAppCtx != null) {
			DemoConfig demoConfig = (DemoConfig) webAppCtx.getBean("demoConfig");
			tokenName = demoConfig.getTokenName();
		}

		// 获取token
		if (extractToken(request, response, tokenName)) {
			return true;
		}

		Cookie cookie = new Cookie(tokenName, null);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
		response.setContentType("text/html;charset=utf-8");
		response.sendRedirect("/login");

		return false;
	}

	/**
	 * 提取token
	 */
	private static boolean extractToken(HttpServletRequest request, @NotNull HttpServletResponse response, String tokenName) {

		// Authorization 和 cookie 二选一
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
			String token = authHeader.substring(authHeader.lastIndexOf(" ") + 1);
			Claims claims = JwtUtil.parseToken(token);
			if (claims == null) {
				return false;
			}
			UserContext.add(authHeader);
			return true;
		}

		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return false;
		}

		for (Cookie cookie : cookies) {
			if (!tokenName.equals(cookie.getName())) {
				continue;
			}
			Claims claims = JwtUtil.parseToken(cookie.getValue());
			if (claims == null) {
				continue;
			}
			UserContext.add(cookie.getValue());
			response.addHeader("docs-token-expire", claims.getExpiration().getTime() + "");
			return true;
		}

		return false;
	}

	@Override
	public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) {
		UserContext.remove();
	}
}
