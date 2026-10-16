package com.skillswap.web;

import com.skillswap.repository.MemberRepository;
import com.skillswap.repository.ProposalRepository;
import com.skillswap.service.*;
import com.skillswap.web.handlers.*;
import com.sun.net.httpserver.HttpServer;

/**
 * Maps URL paths to their respective handler instances.
 */
public class Router {

    public static void registerRoutes(HttpServer server,
                                      MemberRepository memberRepo,
                                      ProposalRepository proposalRepo,
                                      AuthService authService,
                                      SessionManager sessionManager,
                                      SwapService swapService) {

        // Home
        server.createContext("/", new HomeHandler(memberRepo, sessionManager, swapService));

        // Auth
        AuthHandler authHandler = new AuthHandler(authService, memberRepo, sessionManager);
        server.createContext("/signup", authHandler);
        server.createContext("/login", authHandler);
        server.createContext("/logout", authHandler);

        // Profile
        ProfileHandler profileHandler = new ProfileHandler(memberRepo, sessionManager);
        server.createContext("/profile", profileHandler);

        // Swaps
        server.createContext("/swaps", new SwapDiscoveryHandler(swapService, memberRepo, sessionManager));

        // Proposals (including /proposals/{id}/accept, etc.)
        server.createContext("/proposals", new ProposalHandler(swapService, proposalRepo, memberRepo, sessionManager));

        // Ratings
        server.createContext("/ratings", new RatingHandler(swapService, memberRepo, sessionManager));

        // Community Members
        server.createContext("/members", new MemberDirectoryHandler(memberRepo, sessionManager));

        // Demo Data Reset
        server.createContext("/demo/reset", new DemoDataHandler(memberRepo, proposalRepo));
    }
}
