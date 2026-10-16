package com.skillswap.web.handlers;

import com.skillswap.model.Member;
import com.skillswap.repository.MemberRepository;
import com.skillswap.service.SessionManager;
import com.skillswap.service.SwapService;
import com.skillswap.web.HtmlRenderer;
import com.skillswap.web.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Handles GET / route rendering the landing page and community overview.
 */
public class HomeHandler implements HttpHandler {
    private final MemberRepository memberRepository;
    private final SessionManager sessionManager;
    private final SwapService swapService;

    public HomeHandler(MemberRepository memberRepository, SessionManager sessionManager, SwapService swapService) {
        this.memberRepository = memberRepository;
        this.sessionManager = sessionManager;
        this.swapService = swapService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendHtml(exchange, 405, "Method Not Allowed");
            return;
        }

        Member currentUser = HttpUtil.getAuthenticatedMember(exchange, sessionManager, memberRepository);
        Map<String, String> q = HttpUtil.parseQueryParams(exchange);
        String msg = q.get("msg");
        String err = q.get("err");

        List<Member> allMembers = memberRepository.findAll();
        int swapCount = 0;
        if (currentUser != null) {
            swapCount = swapService.discoverSwaps(currentUser, 4).size();
        }

        String content = HtmlRenderer.renderHome(currentUser, allMembers.size(), swapCount, allMembers);
        String page = HtmlRenderer.renderPage("Home", "home", currentUser, content, msg, err);
        HttpUtil.sendHtml(exchange, 200, page);
    }
}
