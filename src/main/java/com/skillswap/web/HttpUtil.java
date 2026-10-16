package com.skillswap.web;

import com.skillswap.model.Member;
import com.skillswap.repository.MemberRepository;
import com.skillswap.service.SessionManager;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utility functions for handling HTTP exchanges with {@link com.sun.net.httpserver.HttpServer}.
 */
public final class HttpUtil {

    private HttpUtil() {}

    /**
     * Sends an HTML response with UTF-8 encoding.
     */
    public static void sendHtml(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("X-Frame-Options", "DENY");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Issues an HTTP 303 See Other redirect.
     */
    public static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
    }

    /**
     * Parses standard application/x-www-form-urlencoded request bodies.
     */
    public static Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        Map<String, String> params = new HashMap<>();
        try (InputStream is = exchange.getRequestBody()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            if (body.isEmpty()) {
                return params;
            }
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String val = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                params.put(key, val);
            }
        }
        return params;
    }

    /**
     * Parses URI query parameters from the request.
     */
    public static Map<String, String> parseQueryParams(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>();
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || query.isEmpty()) {
            return params;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String val = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            params.put(key, val);
        }
        return params;
    }

    /**
     * Retrieves the currently authenticated member from the session cookie, if present.
     */
    public static Member getAuthenticatedMember(HttpExchange exchange, SessionManager sessionManager, MemberRepository memberRepository) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        String token = SessionManager.extractSessionToken(cookieHeader);
        if (token == null) {
            return null;
        }
        String memberId = sessionManager.getMemberId(token);
        if (memberId == null) {
            return null;
        }
        return memberRepository.findById(memberId).orElse(null);
    }
}
