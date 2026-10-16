package com.skillswap.web.handlers;

import com.skillswap.repository.MemberRepository;
import com.skillswap.repository.ProposalRepository;
import com.skillswap.service.DemoDataSeeder;
import com.skillswap.web.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles POST /demo/reset to reset data back to clean seed state.
 */
public class DemoDataHandler implements HttpHandler {
    private final MemberRepository memberRepository;
    private final ProposalRepository proposalRepository;

    public DemoDataHandler(MemberRepository memberRepository, ProposalRepository proposalRepository) {
        this.memberRepository = memberRepository;
        this.proposalRepository = proposalRepository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendHtml(exchange, 405, "Method Not Allowed");
            return;
        }

        DemoDataSeeder.reseed(memberRepository, proposalRepository);
        HttpUtil.redirect(exchange, "/members?msg=" + URLEncoder.encode("Demo data reseeded successfully! 12 members and overlapping cycles ready.", StandardCharsets.UTF_8));
    }
}
