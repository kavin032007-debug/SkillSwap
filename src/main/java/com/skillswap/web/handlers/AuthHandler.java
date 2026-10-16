package com.skillswap.web.handlers;

import com.skillswap.model.Member;
import com.skillswap.repository.MemberRepository;
import com.skillswap.service.AuthService;
import com.skillswap.service.SessionManager;
import com.skillswap.web.HtmlRenderer;
import com.skillswap.web.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * Handles member authentication, registration, login, and session termination.
 */
public class AuthHandler implements HttpHandler {
    private final AuthService authService;
    private final MemberRepository memberRepository;
    private final SessionManager sessionManager;

    public AuthHandler(AuthService authService, MemberRepository memberRepository, SessionManager sessionManager) {
        this.authService = authService;
        this.memberRepository = memberRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        Member currentUser = HttpUtil.getAuthenticatedMember(exchange, sessionManager, memberRepository);
        Map<String, String> q = HttpUtil.parseQueryParams(exchange);
        String msg = q.get("msg");
        String err = q.get("err");

        if ("/signup".equals(path)) {
            if ("GET".equalsIgnoreCase(method)) {
                if (currentUser != null) {
                    HttpUtil.redirect(exchange, "/profile");
                    return;
                }
                String content = HtmlRenderer.renderSignup();
                String page = HtmlRenderer.renderPage("Sign Up", "signup", null, content, msg, err);
                HttpUtil.sendHtml(exchange, 200, page);
            } else if ("POST".equalsIgnoreCase(method)) {
                handleSignupPost(exchange);
            }
        } else if ("/login".equals(path)) {
            if ("GET".equalsIgnoreCase(method)) {
                if (currentUser != null) {
                    HttpUtil.redirect(exchange, "/profile");
                    return;
                }
                String content = HtmlRenderer.renderLogin();
                String page = HtmlRenderer.renderPage("Log In", "login", null, content, msg, err);
                HttpUtil.sendHtml(exchange, 200, page);
            } else if ("POST".equalsIgnoreCase(method)) {
                handleLoginPost(exchange);
            }
        } else if ("/logout".equals(path)) {
            handleLogout(exchange);
        } else {
            HttpUtil.sendHtml(exchange, 404, "Page Not Found");
        }
    }

    private void handleSignupPost(HttpExchange exchange) throws IOException {
        Map<String, String> form = HttpUtil.parseFormData(exchange);
        String name = form.get("name");
        String pin = form.get("pin");
        String contact = form.get("contact");
        String bio = form.get("bio");

        try {
            Member member = authService.register(name, pin, contact, bio);
            String token = sessionManager.createSession(member.getId());
            exchange.getResponseHeaders().add("Set-Cookie", SessionManager.buildSetCookieHeader(token, 7 * 86400));
            HttpUtil.redirect(exchange, "/profile?msg=" + urlEncode("Welcome to SkillSwap, " + member.getName() + "! Add your skills below."));
        } catch (Exception e) {
            HttpUtil.redirect(exchange, "/signup?err=" + urlEncode(e.getMessage()));
        }
    }

    private void handleLoginPost(HttpExchange exchange) throws IOException {
        Map<String, String> form = HttpUtil.parseFormData(exchange);
        String name = form.get("name");
        String pin = form.get("pin");

        Optional<Member> authenticated = authService.authenticate(name, pin);
        if (authenticated.isPresent()) {
            Member m = authenticated.get();
            String token = sessionManager.createSession(m.getId());
            exchange.getResponseHeaders().add("Set-Cookie", SessionManager.buildSetCookieHeader(token, 7 * 86400));
            HttpUtil.redirect(exchange, "/swaps?msg=" + urlEncode("Welcome back, " + m.getName() + "!"));
        } else {
            HttpUtil.redirect(exchange, "/login?err=" + urlEncode("Invalid name or PIN. Please check and try again."));
        }
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String cookie = exchange.getRequestHeaders().getFirst("Cookie");
        String token = SessionManager.extractSessionToken(cookie);
        if (token != null) {
            sessionManager.invalidate(token);
        }
        exchange.getResponseHeaders().add("Set-Cookie", SessionManager.buildClearCookieHeader());
        HttpUtil.redirect(exchange, "/?msg=" + urlEncode("You have signed out successfully."));
    }

    private String urlEncode(String text) {
        if (text == null) return "";
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
