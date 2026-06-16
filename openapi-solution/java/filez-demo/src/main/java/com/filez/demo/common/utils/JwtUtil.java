package com.filez.demo.common.utils;

import com.alibaba.fastjson.JSON;
import com.filez.demo.entity.SysUserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class JwtUtil {

	/**
	 * JWT密钥，用于加密和解密JWT令牌
	 */
	private static String secretKey;

	/**
	 * JWT过期时间，单位为小时，默认24小时
	 */
	private final static Duration expire = Duration.ofHours(24);

	// 静态代码块，从环境变量中获取secretKey，变量名是jwt.secretKey
	static {
		String secretKey = System.getenv("jwt.secretKey");
		if (StringUtils.isNotEmpty(secretKey)) {
			JwtUtil.secretKey = secretKey;
		} else {
			log.warn("未配置jwt.secretKey，使用默认值");
			JwtUtil.secretKey = UUID.randomUUID().toString();
		}
	}

	/**
	 * 生成JWT令牌
	 * 该方法使用用户信息作为payload，设置当前时间作为颁发时间，设置未来的过期时间，并使用HS512算法签名
	 * @param user 用户对象，包含用户信息 该信息将被序列化至令牌主体中
	 * @return 生成的JWT令牌字符串
	 */
	public static String generateToken(SysUserEntity user) {
		// 计算令牌过期时间
		Date expireDate = new Date(System.currentTimeMillis() + expire.toMillis());

		// 构建JWT令牌
		return Jwts.builder()
				// 设置令牌主体，将用户对象序列化为JSON字符串
				.setSubject(JSON.toJSONString(user))
				// 设置令牌颁发时间
				.setIssuedAt(new Date())
				// 设置令牌过期时间
				.setExpiration(expireDate)
				// 使用HS512算法签名，secretKey作为密钥
				.signWith(SignatureAlgorithm.HS512, secretKey)
				// 将令牌压缩为JWS字符串
				.compact();
	}

	/**
	 * 解析JWT令牌
	 * 本方法旨在从给定的JWT令牌中解析出其中的声明（Claims）
	 * 如果令牌为空、格式不正确或已过期，将返回null
	 * @param token 待解析的JWT令牌
	 * @return 解析成功的Claims对象，否则返回null
	 */
	public static Claims parseToken(String token) {

		// 检查令牌是否为空或仅由空白字符构成
		if (StringUtils.isEmpty(token)) {
			return null;
		}

		try {
			// 使用预定义的密钥解析JWT令牌，并返回其主体部分（Body）
			return Jwts.parser()
					.setSigningKey(secretKey)
					.parseClaimsJws(token)
					.getBody();
		} catch (ExpiredJwtException e) {
			log.warn("token已过期");
		} catch (Exception e) {
			log.warn("无效token");
		}

		return null;
	}
}

