package com.skillswap.web.handlers;

import com.skillswap.model.Member;
import com.skillswap.repository.MemberRepository;
import com.skillswap.service.SessionManager;
import com.skillswap.service.SwapService;
import com.skillswap.web.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Handles POST /ratings for leaving peer reviews for teachers following completed swaps.
 */
public class RatingHandler implements HttpHandler {
    private final SwapService swapService;
    private final MemberRepository memberRepository;
    private final SessionManager sessionManager;

    public RatingHandler(SwapService swapService, MemberRepository memberRepository, SessionManager sessionManager) {
        this.swapService = swapService;
        this.memberRepository = memberRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendHtml(exchange, 405, "Method Not Allowed");
            return;
        }

        Member currentUser = HttpUtil.getAuthenticatedMember(exchange, sessionManager, memberRepository);
        if (currentUser == null) {
            HttpUtil.redirect(exchange, "/login?err=" + urlEncode("Please sign in to submit a rating"));
            return;
        }

        Map<String, String> form = HttpUtil.parseFormData(exchange);
        String proposalId = form.get("proposalId");
        String teacherId = form.get("teacherId");
        String scoreStr = form.get("score");
        String feedback = form.get("feedback");

        try {
            int score = Integer.parseInt(scoreStr);
            swapService.rateTeacher(proposalId, currentUser.getId(), teacherId, score, feedback);
            HttpUtil.redirect(exchange, "/proposals?msg=" + urlEncode("Teacher rating submitted! Thank you for strengthening the barter community."));
        } catch (Exception e) {
            HttpUtil.redirect(exchange, "/proposals?err=" + urlEncode(e.getMessage()));
        }
    }

    private String urlEncode(String text) {
        if (text == null) return "";
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
