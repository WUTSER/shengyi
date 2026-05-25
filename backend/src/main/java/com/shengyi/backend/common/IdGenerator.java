package com.shengyi.backend.common;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class IdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String CHARS = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    private IdGenerator() {
    }

    public static String next(String prefix) {
        StringBuilder builder = new StringBuilder(prefix);
        builder.append(LocalDateTime.now().format(FORMATTER));
        for (int i = 0; i < 6; i++) {
            builder.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return builder.toString();
    }
}
