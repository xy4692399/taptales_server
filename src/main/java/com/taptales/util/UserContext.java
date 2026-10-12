package com.taptales.util;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
public class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_TYPE = new ThreadLocal<>();

    public static void set(Long userId, String type) {
        USER_ID.set(userId);
        USER_TYPE.set(type);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getType() {
        return USER_TYPE.get();
    }

    public static void clear() {
        USER_ID.remove();
        USER_TYPE.remove();
    }

    public static Long getFromRequest(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return Long.valueOf(auth.substring(7));
        }
        return null;
    }
}
