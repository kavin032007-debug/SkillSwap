package com.skillswap.web.handlers;

import com.skillswap.model.Member;
import com.skillswap.repository.MemberRepository;
import com.skillswap.service.SessionManager;
import com.skillswap.web.HtmlRenderer;
import com.skillswap.web.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Handles GET /members for exploring all community members and available skills.
 */
public class MemberDirectoryHandler implements HttpHandler {
    private final MemberRepository memberRepository;
    private final SessionManager sessionManager;

    public MemberDirectoryHandler(MemberRepository memberRepository, SessionManager sessionManager) {
        this.memberRepository = memberRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Member currentUser = HttpUtil.getAuthenticatedMember(exchange, sessionManager, memberRepository);
        Map<String, String> q = HttpUtil.parseQueryParams(exchange);
        String msg = q.get("msg");
        String err = q.get("err");

        List<Member> members = memberRepository.findAll();
        members.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        String content = HtmlRenderer.renderMembers(members, currentUser);
        String page = HtmlRenderer.renderPage("Community", "members", currentUser, content, msg, err);
        HttpUtil.sendHtml(exchange, 200, page);
    }
}
