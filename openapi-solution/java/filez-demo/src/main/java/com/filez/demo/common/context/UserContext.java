package com.filez.demo.common.context;

import com.alibaba.fastjson.JSON;
import com.filez.demo.common.utils.JwtUtil;
import com.filez.demo.entity.SysUserEntity;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;

public final class UserContext {

	public static final ThreadLocal<String> local = new ThreadLocal<>();

	public static void add(String token) {
		local.set(token);
	}

	public static void remove() {
		local.remove();
	}

	public static String getCurrentUserToken() {
		return local.get();
	}

	/**
	 * 获取当前用户
	 */
	public static SysUserEntity getCurrentUser() {
		Claims claims = JwtUtil.parseToken(getCurrentUserToken());
		if (claims == null) {
			return null;
		}
		String userStr = claims.getSubject();
		if (StringUtils.isEmpty(userStr)) {
			return null;
		}
		return JSON.parseObject(userStr, SysUserEntity.class);
	}
}
