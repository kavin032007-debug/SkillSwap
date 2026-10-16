package com.skillswap;

import com.skillswap.repository.FilePersistedMemberRepository;
import com.skillswap.repository.FilePersistedProposalRepository;
import com.skillswap.repository.MemberRepository;
import com.skillswap.repository.ProposalRepository;
import com.skillswap.service.*;
import com.skillswap.web.Router;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

/**
 * Main application bootstrap and entry point for SkillSwap.
 * Configures file persistence, seeds demo community members, initializes HTTP server,
 * and binds routes.
 */
public class Main {

    public static void main(String[] args) {
        // Resolve PORT from environment (standard for Render / Railway / Cloud Run)
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }

        // Initialize persistent file repositories
        Path dataDir = Paths.get("data");
        Path membersFile = dataDir.resolve("members.json");
        Path proposalsFile = dataDir.resolve("proposals.json");

        MemberRepository memberRepository = new FilePersistedMemberRepository(membersFile);
        ProposalRepository proposalRepository = new FilePersistedProposalRepository(proposalsFile, memberRepository);

        // Core business services
        AuthService authService = new AuthService(memberRepository);
        SessionManager sessionManager = new SessionManager();
        SwapService swapService = new SwapService(memberRepository, proposalRepository);

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newFixedThreadPool(16));

            Router.registerRoutes(server, memberRepository, proposalRepository, authService, sessionManager, swapService);

            server.start();

            System.out.println("==================================================================");
            System.out.println("   🔄 SkillSwap - Money-Free Skill Barter Marketplace (Pure Java) ");
            System.out.println("==================================================================");
            System.out.println("   Server successfully running at: http://localhost:" + port);
            System.out.println("   Join free / explore community: http://localhost:" + port + "/signup");
            System.out.println("   Registered members: " + memberRepository.count());
            System.out.println("   Press Ctrl+C to stop server.");
            System.out.println("==================================================================");

            // Graceful shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down SkillSwap server...");
                server.stop(1);
                System.out.println("Server stopped gracefully.");
            }));

        } catch (IOException e) {
            System.err.println("Fatal: Failed to start HttpServer on port " + port + ": " + e.getMessage());
            System.exit(1);
        }
    }
}
