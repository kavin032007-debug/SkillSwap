package com.skillswap.service;

import com.skillswap.util.SecurityUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages HTTP session tokens and cookie lifecycle.
 */
public class SessionManager {
    public static final String COOKIE_NAME = "SKILLSWAP_SESSION";
    private static final long SESSION_TTL_MS = 7L * 24L * 60L * 60L * 1000L; // 7 days

    private record SessionRecord(String memberId, long expiresAt) {}

    private final Map<String, SessionRecord> sessions = new ConcurrentHashMap<>();

    public String createSession(String memberId) {
        String token = SecurityUtil.generateToken();
        long expiresAt = System.currentTimeMillis() + SESSION_TTL_MS;
        sessions.put(token, new SessionRecord(memberId, expiresAt));
        return token;
    }

    public String getMemberId(String token) {
        if (token == null) return null;
        SessionRecord record = sessions.get(token);
        if (record == null) return null;
        if (System.currentTimeMillis() > record.expiresAt()) {
            sessions.remove(token);
            return null;
        }
        return record.memberId();
    }

    public void invalidate(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    public static String buildSetCookieHeader(String token, int maxAgeSeconds) {
        return COOKIE_NAME + "=" + token + "; Path=/; Max-Age=" + maxAgeSeconds + "; HttpOnly; SameSite=Lax";
    }

    public static String buildClearCookieHeader() {
        return COOKIE_NAME + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax";
    }

    public static String extractSessionToken(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.trim().isEmpty()) {
            return null;
        }
        String[] cookies = cookieHeader.split(";");
        for (String c : cookies) {
            String[] parts = c.trim().split("=", 2);
            if (parts.length == 2 && parts[0].trim().equals(COOKIE_NAME)) {
                return parts[1].trim();
            }
        }
        return null;
    }
}
