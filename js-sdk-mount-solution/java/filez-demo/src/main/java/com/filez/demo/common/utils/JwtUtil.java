package com.filez.demo.common.utils;

import com.alibaba.fastjson.JSON;
import com.filez.demo.entity.SysUserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class JwtUtil {

	/**
	 * JWT secret key for encrypting and decrypting JWT tokens
	 */
	private static String secretKey;

	/**
	 * JWT expiration time in hours, default 24 hours
	 */
	private final static Duration expire = Duration.ofHours(24);

	// Static code block to get secretKey from environment variable named jwt.secretKey
	static {
		String secretKey = System.getenv("jwt.secretKey");
		if (StringUtils.isNotEmpty(secretKey)) {
			JwtUtil.secretKey = secretKey;
		} else {
			log.warn("jwt.secretKey not configured, using default value");
			JwtUtil.secretKey = UUID.randomUUID().toString();
		}
	}

	/**
	 * Generate JWT token
	 * This method uses user information as payload, sets current time as issued time, sets future expiration time, and signs with HS512 algorithm
	 * @param user User object containing user information, this information will be serialized into the token body
	 * @return Generated JWT token string
	 */
	public static String generateToken(SysUserEntity user) {
		// Calculate token expiration time
		Date expireDate = new Date(System.currentTimeMillis() + expire.toMillis());

		// Build JWT token
		return Jwts.builder()
				// Set token subject, serialize user object to JSON string
				.setSubject(JSON.toJSONString(user))
				// Set token issued time
				.setIssuedAt(new Date())
				// Set token expiration time
				.setExpiration(expireDate)
				// Sign with HS512 algorithm, using secretKey as the key
				.signWith(SignatureAlgorithm.HS512, secretKey.getBytes())
				// Compress token to JWS string
				.compact();
	}

	/**
	 * Parse JWT token use demo secret
	 * This method aims to parse the claims from the given JWT token
	 * If the token is empty, malformed, or expired, it will return null
	 * @param token JWT token to be parsed
	 * @return Successfully parsed Claims object, otherwise returns null
	 */
	public static Claims parseToken(String token) {

		return JwtUtil.parseToken(token, secretKey);
	}

	/**
	 * Parse JWT token
	 * This method aims to parse the claims from the given JWT token
	 * If the token is empty, malformed, or expired, it will return null
	 * @param token JWT token to be parsed
	 * @param secret secret
	 * @return Successfully parsed Claims object, otherwise returns null
	 */
	public static Claims parseToken(String token, String secret) {

		// Check if token is empty or consists only of whitespace characters
		if (StringUtils.isEmpty(token)) {
			return null;
		}

		try {
			return Jwts.parser()
					.setSigningKey(secret.getBytes())
					.parseClaimsJws(token)
					.getBody();
		} catch (ExpiredJwtException e) {
			log.warn("Token has expired");
		} catch (Exception e) {
			log.warn("Invalid token");
		}

		return null;
	}
}

