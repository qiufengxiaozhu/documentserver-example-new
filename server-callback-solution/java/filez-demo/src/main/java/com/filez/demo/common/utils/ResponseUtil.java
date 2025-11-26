package com.filez.demo.common.utils;

import org.springframework.http.ResponseEntity;

/**
 * Response utility class for unified HTTP response handling
 */
public class ResponseUtil {

    /**
     * Create error response with correct Content-Type
     * @param message Error message
     * @return ResponseEntity
     */
    public static ResponseEntity<String> badRequest(String message) {
        return ResponseEntity.badRequest()
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body(message);
    }

    /**
     * Create success response
     * @param body Response body
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }
}
