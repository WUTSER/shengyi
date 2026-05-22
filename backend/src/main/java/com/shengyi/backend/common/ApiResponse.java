package com.shengyi.backend.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public record ApiResponse<T>(String code, String message, T data, String traceId) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("0", "success", data, newTraceId());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("0", message, data, newTraceId());
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(code, message, null, newTraceId());
    }

    private static String newTraceId() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return time + random;
    }
}
