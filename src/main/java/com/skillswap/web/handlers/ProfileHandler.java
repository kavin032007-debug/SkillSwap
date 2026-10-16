package com.skillswap.web.handlers;

import com.skillswap.model.*;
import com.skillswap.repository.MemberRepository;
import com.skillswap.service.SessionManager;
import com.skillswap.web.HtmlRenderer;
import com.skillswap.web.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Handles member profile management, adding/removing skills, and availability settings.
 */
public class ProfileHandler implements HttpHandler {
    private final MemberRepository memberRepository;
    private final SessionManager sessionManager;

    public ProfileHandler(MemberRepository memberRepository, SessionManager sessionManager) {
        this.memberRepository = memberRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Member currentUser = HttpUtil.getAuthenticatedMember(exchange, sessionManager, memberRepository);
        if (currentUser == null) {
            HttpUtil.redirect(exchange, "/login?err=" + urlEncode("Please sign in to view your profile"));
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("/profile".equals(path) && "GET".equalsIgnoreCase(method)) {
            Map<String, String> q = HttpUtil.parseQueryParams(exchange);
            String msg = q.get("msg");
            String err = q.get("err");

            String content = HtmlRenderer.renderProfile(currentUser);
            String page = HtmlRenderer.renderPage("My Profile", "profile", currentUser, content, msg, err);
            HttpUtil.sendHtml(exchange, 200, page);
        } else if ("/profile/skills/add".equals(path) && "POST".equalsIgnoreCase(method)) {
            handleAddSkill(exchange, currentUser);
        } else if ("/profile/skills/remove".equals(path) && "POST".equalsIgnoreCase(method)) {
            handleRemoveSkill(exchange, currentUser);
        } else if ("/profile/availability".equals(path) && "POST".equalsIgnoreCase(method)) {
            handleUpdateAvailability(exchange, currentUser);
        } else {
            HttpUtil.sendHtml(exchange, 404, "Page Not Found");
        }
    }

    private void handleAddSkill(HttpExchange exchange, Member currentUser) throws IOException {
        Map<String, String> form = HttpUtil.parseFormData(exchange);
        String type = form.get("type"); // "teaches" or "wants"
        String name = form.get("name");
        String categoryStr = form.get("category");
        String levelStr = form.get("level");

        if (name == null || name.trim().isEmpty()) {
            HttpUtil.redirect(exchange, "/profile?err=" + urlEncode("Skill name cannot be empty"));
            return;
        }

        SkillCategory category = SkillCategory.fromString(categoryStr);
        SkillLevel level = SkillLevel.fromString(levelStr);
        Skill skill = new Skill(name.trim(), category, level);

        if ("teaches".equalsIgnoreCase(type)) {
            currentUser.addTeaches(skill);
        } else {
            currentUser.addWants(skill);
        }

        memberRepository.save(currentUser);
        HttpUtil.redirect(exchange, "/profile?msg=" + urlEncode("Skill '" + skill.getName() + "' added successfully!"));
    }

    private void handleRemoveSkill(HttpExchange exchange, Member currentUser) throws IOException {
        Map<String, String> form = HttpUtil.parseFormData(exchange);
        String type = form.get("type");
        String name = form.get("name");

        if (name != null) {
            if ("teaches".equalsIgnoreCase(type)) {
                currentUser.removeTeaches(name);
            } else {
                currentUser.removeWants(name);
            }
            memberRepository.save(currentUser);
        }

        HttpUtil.redirect(exchange, "/profile?msg=" + urlEncode("Skill removed."));
    }

    private void handleUpdateAvailability(HttpExchange exchange, Member currentUser) throws IOException {
        Map<String, String> form = HttpUtil.parseFormData(exchange);
        String hoursStr = form.get("hours");
        String note = form.get("note");

        try {
            int hours = Integer.parseInt(hoursStr);
            currentUser.setWeeklyAvailabilityHours(hours);
            currentUser.setAvailabilityNote(note);
            memberRepository.save(currentUser);
            HttpUtil.redirect(exchange, "/profile?msg=" + urlEncode("Availability updated to " + hours + " hrs/week!"));
        } catch (Exception e) {
            HttpUtil.redirect(exchange, "/profile?err=" + urlEncode("Invalid availability hours"));
        }
    }

    private String urlEncode(String text) {
        if (text == null) return "";
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
