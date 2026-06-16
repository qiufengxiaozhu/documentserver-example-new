package com.filez.demo.common.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
public class HmacUtil {

    /**
     * 使用 HMAC-SHA256 生成签名
     * @param uri    请求 URI，例如 /api/hello?name=张三
     * @param secret 加密用的密钥
     * @return 64 位小写十六进制字符串
     */
    public static String hmac(URI uri, String secret) throws Exception {
        // 构造要签名的字符串
        String path = uri.getRawPath();
        String query = uri.getRawQuery();
        String requestData = query != null ? path + "?" + query : path;
        log.debug("生成HMAC的基准request-url：requestData: {}", requestData);

        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);
        byte[] hmacBytes = mac.doFinal(requestData.getBytes(StandardCharsets.UTF_8));

        // 转为 64 位 hex 字符串
        return String.format("%064x", new BigInteger(1, hmacBytes));
    }
}
