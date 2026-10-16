package com.skillswap.web.handlers;

import com.skillswap.model.*;
import com.skillswap.repository.MemberRepository;
import com.skillswap.repository.ProposalRepository;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles swap proposal creation, listing, acceptance, declining, and lifecycle transitions.
 */
public class ProposalHandler implements HttpHandler {
    private static final Pattern ACTION_PATTERN = Pattern.compile("^/proposals/([^/]+)/(accept|decline|cancel|complete)$");

    private final SwapService swapService;
    private final ProposalRepository proposalRepository;
    private final MemberRepository memberRepository;
    private final SessionManager sessionManager;

    public ProposalHandler(SwapService swapService, ProposalRepository proposalRepository,
                           MemberRepository memberRepository, SessionManager sessionManager) {
        this.swapService = swapService;
        this.proposalRepository = proposalRepository;
        this.memberRepository = memberRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Member currentUser = HttpUtil.getAuthenticatedMember(exchange, sessionManager, memberRepository);
        if (currentUser == null) {
            HttpUtil.redirect(exchange, "/login?err=" + urlEncode("Please sign in to view or manage proposals"));
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // 1. Action routes: /proposals/{id}/{action}
        Matcher matcher = ACTION_PATTERN.matcher(path);
        if (matcher.matches() && "POST".equalsIgnoreCase(method)) {
            String proposalId = matcher.group(1);
            String action = matcher.group(2);
            handleProposalAction(exchange, currentUser, proposalId, action);
            return;
        }

        // 2. Listing route: GET /proposals
        if ("/proposals".equals(path) && "GET".equalsIgnoreCase(method)) {
            Map<String, String> q = HttpUtil.parseQueryParams(exchange);
            String msg = q.get("msg");
            String err = q.get("err");

            List<SwapProposal> myProposals = proposalRepository.findByMemberId(currentUser.getId());
            String content = HtmlRenderer.renderProposals(currentUser, myProposals);
            String page = HtmlRenderer.renderPage("My Proposals", "proposals", currentUser, content, msg, err);
            HttpUtil.sendHtml(exchange, 200, page);
        }
        // 3. Create route: POST /proposals
        else if ("/proposals".equals(path) && "POST".equalsIgnoreCase(method)) {
            handleCreateProposal(exchange, currentUser);
        } else {
            HttpUtil.sendHtml(exchange, 404, "Page Not Found");
        }
    }

    private void handleCreateProposal(HttpExchange exchange, Member currentUser) throws IOException {
        Map<String, String> form = HttpUtil.parseFormData(exchange);
        String signature = form.get("signature");

        if (signature == null || signature.trim().isEmpty()) {
            HttpUtil.redirect(exchange, "/swaps?err=" + urlEncode("Missing swap cycle information"));
            return;
        }

        // Discover matching cycle
        List<SwapCycle> available = swapService.discoverSwaps(currentUser, 5);
        SwapCycle target = null;
        for (SwapCycle c : available) {
            if (c.getCanonicalSignature().equals(signature.trim())) {
                target = c;
                break;
            }
        }

        if (target == null) {
            HttpUtil.redirect(exchange, "/swaps?err=" + urlEncode("Swap cycle is no longer available or member skills changed"));
            return;
        }

        try {
            SwapProposal proposal = swapService.createProposal(currentUser.getId(), target);
            HttpUtil.redirect(exchange, "/proposals?msg=" + urlEncode("Proposal #" + proposal.getId() + " created! Awaiting mutual acceptance."));
        } catch (Exception e) {
            HttpUtil.redirect(exchange, "/swaps?err=" + urlEncode(e.getMessage()));
        }
    }

    private void handleProposalAction(HttpExchange exchange, Member currentUser, String proposalId, String action) throws IOException {
        try {
            switch (action) {
                case "accept" -> {
                    swapService.acceptProposal(proposalId, currentUser.getId());
                    HttpUtil.redirect(exchange, "/proposals?msg=" + urlEncode("You accepted proposal #" + proposalId + "!"));
                }
                case "decline" -> {
                    swapService.declineProposal(proposalId, currentUser.getId());
                    HttpUtil.redirect(exchange, "/proposals?msg=" + urlEncode("Proposal #" + proposalId + " has been declined and cancelled."));
                }
                case "cancel" -> {
                    swapService.cancelProposal(proposalId, currentUser.getId());
                    HttpUtil.redirect(exchange, "/proposals?msg=" + urlEncode("Proposal #" + proposalId + " has been cancelled."));
                }
                case "complete" -> {
                    swapService.completeProposal(proposalId);
                    HttpUtil.redirect(exchange, "/proposals?msg=" + urlEncode("Swap marked as completed! You can now rate your teacher below."));
                }
                default -> HttpUtil.sendHtml(exchange, 400, "Unknown action");
            }
        } catch (Exception e) {
            HttpUtil.redirect(exchange, "/proposals?err=" + urlEncode(e.getMessage()));
        }
    }

    private String urlEncode(String text) {
        if (text == null) return "";
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
