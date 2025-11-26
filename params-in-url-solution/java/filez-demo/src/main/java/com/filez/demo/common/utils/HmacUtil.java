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
     * Generate signature using HMAC-SHA256
     * @param uri    Request URI, e.g. /api/hello?name=zhangsan
     * @param secret Secret key for encryption
     * @return 64-bit lowercase hexadecimal string
     */
    public static String hmac(URI uri, String secret) throws Exception {
        // Construct the string to be signed
        String path = uri.getRawPath();
        String query = uri.getRawQuery();
        String requestData = query != null ? path + "?" + query : path;
        log.debug("Base request-url for generating HMAC: requestData: {}", requestData);

        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);
        byte[] hmacBytes = mac.doFinal(requestData.getBytes(StandardCharsets.UTF_8));

        // Convert to 64-bit hex string
        return String.format("%064x", new BigInteger(1, hmacBytes));
    }
}
