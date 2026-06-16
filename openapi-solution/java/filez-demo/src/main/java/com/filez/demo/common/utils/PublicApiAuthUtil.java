package com.filez.demo.common.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * PublicAPI 鉴权工具类
 * <p>
 * Authorization 格式：{repoId}:{appId}:{token}
 * token = MD5("{secret}@@{timestamp}@@{nonce}") 或 MD5("{secret}@@{timestamp}@@{nonce}@@{bodyJson}")
 * <p>
 * 参考文档：<a href="https://api.filez.com/office/docs/docs-api/open-api/server-development/auth/">...</a>
 */
@Slf4j
public class PublicApiAuthUtil {

    public static final String AUTH_TYPE = "s2s_MD5_sig";
    public static final String HEADER_AUTH_TYPE = "zOffice-auth-type";
    public static final String HEADER_TIMESTAMP = "timeStamp";
    public static final String HEADER_NONCE = "zOffice-message-nonce";

    /**
     * 生成 MD5 签名 token
     * @param secret    应用密钥
     * @param timestamp 时间戳（毫秒）
     * @param nonce     随机字符串
     * @param body      请求体 JSON 字符串（GET 请求可为 null 或空串）
     * @return MD5 签名 32 位小写 hex 字符串
     */
    public static String createToken(String secret, String timestamp, String nonce, String body) {
        String seed = secret + "@@" + timestamp + "@@" + nonce;
        if (body != null && !body.isEmpty()) {
            seed += "@@" + body;
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(seed.getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new BigInteger(1, md5.digest()));
        } catch (Exception e) {
            log.error("[PublicApiAuthUtil] createToken failed", e);
            throw new RuntimeException("Failed to create PublicAPI auth token", e);
        }
    }

    /**
     * 生成完整的鉴权头信息
     * @param repoId 三方系统 repoId
     * @param secret 应用密钥
     * @param body   请求体 JSON（POST 请求传入，GET 请求传 null 或空串）
     * @return 鉴权头信息
     */
    public static AuthHeaders buildAuthHeaders(String repoId, String secret, String body) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = UUID.randomUUID().toString();
        String token = createToken(secret, timestamp, nonce, body);
        String authorization = repoId + ":publicApi:" + token;
        return new AuthHeaders(AUTH_TYPE, timestamp, nonce, authorization);
    }

    /**
     * 鉴权头信息封装
     */
    @Getter
    @AllArgsConstructor
    public static class AuthHeaders {
        private final String authType;
        private final String timestamp;
        private final String nonce;
        private final String authorization;
    }
}
