package com.skillswap.web.handlers;

import com.skillswap.finder.AlmostMatchFinder;
import com.skillswap.model.Member;
import com.skillswap.model.SwapCycle;
import com.skillswap.repository.MemberRepository;
import com.skillswap.service.SessionManager;
import com.skillswap.service.SwapService;
import com.skillswap.web.HtmlRenderer;
import com.skillswap.web.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Handles GET /swaps for discovering direct and multi-way barter cycles and almost-matches.
 */
public class SwapDiscoveryHandler implements HttpHandler {
    private final SwapService swapService;
    private final MemberRepository memberRepository;
    private final SessionManager sessionManager;

    public SwapDiscoveryHandler(SwapService swapService, MemberRepository memberRepository, SessionManager sessionManager) {
        this.swapService = swapService;
        this.memberRepository = memberRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Member currentUser = HttpUtil.getAuthenticatedMember(exchange, sessionManager, memberRepository);
        if (currentUser == null) {
            HttpUtil.redirect(exchange, "/login?err=" + urlEncode("Please sign in to find barter swaps"));
            return;
        }

        Map<String, String> q = HttpUtil.parseQueryParams(exchange);
        int maxLen = 4;
        if (q.containsKey("maxLen")) {
            try {
                maxLen = Integer.parseInt(q.get("maxLen"));
            } catch (NumberFormatException ignored) {}
        }

        String msg = q.get("msg");
        String err = q.get("err");

        // Discovered cycles
        List<SwapCycle> cycles = swapService.discoverSwaps(currentUser, maxLen);

        // Almost-Matches (Feature F7)
        List<AlmostMatchFinder.Opportunity> almostMatches = swapService.discoverAlmostMatches(currentUser, 3);

        String content = HtmlRenderer.renderSwaps(currentUser, cycles, almostMatches, maxLen);
        String page = HtmlRenderer.renderPage("Find Swaps", "swaps", currentUser, content, msg, err);
        HttpUtil.sendHtml(exchange, 200, page);
    }

    private String urlEncode(String text) {
        if (text == null) return "";
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
