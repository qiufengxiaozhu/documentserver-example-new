package com.filez.demo.common.utils;

import org.springframework.http.ResponseEntity;

/**
 * 响应工具类，用于统一处理HTTP响应
 */
public class ResponseUtil {

    /**
     * 创建错误响应，确保正确的Content-Type
     * @param message 错误消息
     * @return ResponseEntity
     */
    public static ResponseEntity<String> badRequest(String message) {
        return ResponseEntity.badRequest()
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body(message);
    }

    /**
     * 创建成功响应
     * @param body 响应体
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }
}
