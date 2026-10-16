package com.skillswap.web;

import com.skillswap.finder.AlmostMatchFinder;
import com.skillswap.model.*;
import com.skillswap.util.SecurityUtil;
import com.skillswap.util.SvgGenerator;

import java.util.*;

/**
 * Pure Java HTML/CSS UI renderer for SkillSwap.
 * Employs a glassmorphic dark-mode design system with responsive flex/grid layouts,
 * SVG barter cycle visualizers, skill chips, and interactive micro-animations.
 * Zero external template engines or runtime dependencies.
 */
public class HtmlRenderer {

    // =========================================================================
    // Base Page Layout with CSS Design System
    // =========================================================================

    public static String renderPage(String title, String activeNav, Member currentUser, String contentHtml, String flashSuccess, String flashError) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        sb.append("<meta charset=\"UTF-8\">\n");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("<title>").append(SecurityUtil.escapeHtml(title)).append(" | SkillSwap</title>\n");
        sb.append("<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n");
        sb.append("<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n");
        sb.append("<link href=\"https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;600&display=swap\" rel=\"stylesheet\">\n");
        sb.append("<style>\n").append(getCssStyles()).append("\n</style>\n");
        sb.append("</head>\n<body>\n");

        // Top Navigation Bar
        sb.append(renderNavbar(activeNav, currentUser));

        // Main Container
        sb.append("<main class=\"main-container\">\n");

        // Flash Alerts
        if (flashSuccess != null && !flashSuccess.trim().isEmpty()) {
            sb.append("<div class=\"alert alert-success\">")
              .append("<span class=\"alert-icon\">✓</span> ")
              .append(SecurityUtil.escapeHtml(flashSuccess))
              .append("</div>\n");
        }
        if (flashError != null && !flashError.trim().isEmpty()) {
            sb.append("<div class=\"alert alert-danger\">")
              .append("<span class=\"alert-icon\">⚠️</span> ")
              .append(SecurityUtil.escapeHtml(flashError))
              .append("</div>\n");
        }

        // Page-specific Content
        sb.append(contentHtml);

        sb.append("</main>\n");

        // Footer
        sb.append(renderFooter());

        sb.append("</body>\n</html>");
        return sb.toString();
    }

    private static String renderNavbar(String activeNav, Member currentUser) {
        StringBuilder sb = new StringBuilder();
        sb.append("<header class=\"navbar\">\n");
        sb.append("  <div class=\"nav-inner\">\n");
        sb.append("    <a href=\"/\" class=\"brand\">\n");
        sb.append("      <span class=\"brand-icon\">🔄</span>\n");
        sb.append("      <span class=\"brand-name\">Skill<span class=\"text-gradient\">Swap</span></span>\n");
        sb.append("    </a>\n");

        sb.append("    <nav class=\"nav-links\">\n");
        sb.append("      <a href=\"/\" class=\"nav-link ").append("home".equals(activeNav) ? "active" : "").append("\">Home</a>\n");
        sb.append("      <a href=\"/members\" class=\"nav-link ").append("members".equals(activeNav) ? "active" : "").append("\">Community</a>\n");

        if (currentUser != null) {
            sb.append("      <a href=\"/swaps\" class=\"nav-link ").append("swaps".equals(activeNav) ? "active" : "").append("\">⚡ Find Swaps</a>\n");
            sb.append("      <a href=\"/proposals\" class=\"nav-link ").append("proposals".equals(activeNav) ? "active" : "").append("\">🤝 Proposals</a>\n");
            sb.append("      <a href=\"/profile\" class=\"nav-link ").append("profile".equals(activeNav) ? "active" : "").append("\">👤 My Profile</a>\n");
        }
        sb.append("    </nav>\n");

        sb.append("    <div class=\"nav-actions\">\n");
        if (currentUser != null) {
            sb.append("      <div class=\"user-pill\">\n");
            sb.append("        <span class=\"user-avatar\">").append(SecurityUtil.escapeHtml(currentUser.getName().substring(0, 1).toUpperCase())).append("</span>\n");
            sb.append("        <span class=\"user-name\">").append(SecurityUtil.escapeHtml(currentUser.getName())).append("</span>\n");
            sb.append("      </div>\n");
            sb.append("      <form action=\"/logout\" method=\"POST\" style=\"display:inline;\">\n");
            sb.append("        <button type=\"submit\" class=\"btn btn-outline btn-sm\">Sign Out</button>\n");
            sb.append("      </form>\n");
        } else {
            sb.append("      <a href=\"/login\" class=\"btn btn-ghost btn-sm\">Log In</a>\n");
            sb.append("      <a href=\"/signup\" class=\"btn btn-primary btn-sm\">Sign Up</a>\n");
        }
        sb.append("    </div>\n");
        sb.append("  </div>\n");
        sb.append("</header>\n");
        return sb.toString();
    }

    private static String renderFooter() {
        return """
        <footer class="footer">
          <div class="footer-inner">
            <p><strong>SkillSwap</strong> &mdash; A Money-Free Skill Barter Marketplace.</p>
            <p class="text-muted">Exchange knowledge directly or through algorithmic 3-way & 4-way barter cycles.</p>
          </div>
        </footer>
        """;
    }

    // =========================================================================
    // Core CSS Stylesheet
    // =========================================================================

    private static String getCssStyles() {
        return """
        :root {
          --bg-base: #090d16;
          --bg-surface: #0f172a;
          --bg-card: rgba(30, 41, 59, 0.72);
          --bg-card-hover: rgba(39, 53, 77, 0.82);
          --border-color: rgba(255, 255, 255, 0.08);
          --border-highlight: rgba(99, 102, 241, 0.35);
          --primary: #6366f1;
          --primary-hover: #4f46e5;
          --accent: #8b5cf6;
          --success: #10b981;
          --warning: #f59e0b;
          --danger: #ef4444;
          --text-main: #f8fafc;
          --text-muted: #94a3b8;
          --text-dim: #64748b;
          --radius-sm: 8px;
          --radius-md: 14px;
          --radius-lg: 20px;
          --shadow-subtle: 0 4px 20px -2px rgba(0, 0, 0, 0.35);
          --shadow-glow: 0 0 25px -5px rgba(99, 102, 241, 0.3);
        }

        * {
          box-sizing: border-box;
          margin: 0;
          padding: 0;
        }

        body {
          font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;
          background-color: var(--bg-base);
          background-image: 
            radial-gradient(at 0% 0%, rgba(99, 102, 241, 0.15) 0px, transparent 50%),
            radial-gradient(at 100% 100%, rgba(16, 185, 129, 0.12) 0px, transparent 50%),
            radial-gradient(at 50% 50%, rgba(139, 92, 246, 0.08) 0px, transparent 60%);
          background-attachment: fixed;
          color: var(--text-main);
          min-height: 100vh;
          display: flex;
          flex-direction: column;
          line-height: 1.6;
          -webkit-font-smoothing: antialiased;
        }

        a {
          color: inherit;
          text-decoration: none;
        }

        .main-container {
          flex: 1;
          width: 100%;
          max-width: 1200px;
          margin: 0 auto;
          padding: 2.5rem 1.5rem;
        }

        /* Navbar */
        .navbar {
          position: sticky;
          top: 0;
          z-index: 100;
          backdrop-filter: blur(16px);
          background: rgba(15, 23, 42, 0.85);
          border-bottom: 1px solid var(--border-color);
        }
        .nav-inner {
          max-width: 1200px;
          margin: 0 auto;
          padding: 0.9rem 1.5rem;
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 1.5rem;
        }
        .brand {
          display: flex;
          align-items: center;
          gap: 0.6rem;
          font-size: 1.35rem;
          font-weight: 800;
          letter-spacing: -0.02em;
        }
        .brand-icon {
          font-size: 1.5rem;
        }
        .text-gradient {
          background: linear-gradient(135deg, #818cf8, #34d399);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }
        .badge-tag {
          font-size: 0.68rem;
          font-weight: 700;
          text-transform: uppercase;
          letter-spacing: 0.05em;
          background: rgba(99, 102, 241, 0.18);
          color: #a5b4fc;
          padding: 0.2rem 0.55rem;
          border-radius: 9999px;
          border: 1px solid rgba(99, 102, 241, 0.3);
        }
        .nav-links {
          display: flex;
          align-items: center;
          gap: 1rem;
        }
        .nav-link {
          font-size: 0.92rem;
          font-weight: 600;
          color: var(--text-muted);
          padding: 0.5rem 0.85rem;
          border-radius: var(--radius-sm);
          transition: all 0.2s ease;
        }
        .nav-link:hover {
          color: var(--text-main);
          background: rgba(255, 255, 255, 0.05);
        }
        .nav-link.active {
          color: #ffffff;
          background: rgba(99, 102, 241, 0.22);
          border: 1px solid rgba(99, 102, 241, 0.4);
        }
        .nav-actions {
          display: flex;
          align-items: center;
          gap: 0.75rem;
        }
        .user-pill {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          background: rgba(255, 255, 255, 0.06);
          padding: 0.35rem 0.75rem 0.35rem 0.4rem;
          border-radius: 9999px;
          border: 1px solid var(--border-color);
        }
        .user-avatar {
          width: 26px;
          height: 26px;
          border-radius: 50%;
          background: linear-gradient(135deg, var(--primary), var(--accent));
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 0.8rem;
          font-weight: 800;
          color: #fff;
        }
        .user-name {
          font-size: 0.85rem;
          font-weight: 600;
        }

        /* Buttons */
        .btn {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          gap: 0.5rem;
          font-family: inherit;
          font-weight: 600;
          border-radius: var(--radius-sm);
          cursor: pointer;
          transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
          border: none;
          text-decoration: none;
          white-space: nowrap;
        }
        .btn:hover {
          transform: translateY(-1px);
        }
        .btn-sm {
          padding: 0.45rem 0.9rem;
          font-size: 0.85rem;
        }
        .btn-md {
          padding: 0.65rem 1.3rem;
          font-size: 0.95rem;
        }
        .btn-lg {
          padding: 0.85rem 1.75rem;
          font-size: 1.05rem;
          border-radius: var(--radius-md);
        }
        .btn-primary {
          background: linear-gradient(135deg, var(--primary), var(--accent));
          color: #ffffff;
          box-shadow: 0 4px 14px 0 rgba(99, 102, 241, 0.38);
        }
        .btn-primary:hover {
          box-shadow: 0 6px 20px 0 rgba(99, 102, 241, 0.5);
        }
        .btn-success {
          background: linear-gradient(135deg, #059669, #10b981);
          color: #ffffff;
          box-shadow: 0 4px 14px 0 rgba(16, 185, 129, 0.38);
        }
        .btn-danger {
          background: linear-gradient(135deg, #dc2626, #ef4444);
          color: #ffffff;
        }
        .btn-outline {
          background: transparent;
          color: var(--text-main);
          border: 1px solid var(--border-color);
        }
        .btn-outline:hover {
          background: rgba(255, 255, 255, 0.08);
          border-color: rgba(255, 255, 255, 0.2);
        }
        .btn-ghost {
          background: transparent;
          color: var(--text-muted);
        }
        .btn-ghost:hover {
          color: var(--text-main);
          background: rgba(255, 255, 255, 0.06);
        }

        /* Glass Cards */
        .glass-card {
          background: var(--bg-card);
          backdrop-filter: blur(12px);
          border: 1px solid var(--border-color);
          border-radius: var(--radius-md);
          padding: 1.75rem;
          box-shadow: var(--shadow-subtle);
          transition: all 0.25s ease;
        }
        .glass-card:hover {
          border-color: rgba(255, 255, 255, 0.14);
        }

        /* Alerts */
        .alert {
          padding: 1rem 1.35rem;
          border-radius: var(--radius-sm);
          margin-bottom: 1.5rem;
          font-weight: 500;
          display: flex;
          align-items: center;
          gap: 0.75rem;
          animation: slideDown 0.3s ease-out;
        }
        .alert-success {
          background: rgba(16, 185, 129, 0.15);
          color: #6ee7b7;
          border: 1px solid rgba(16, 185, 129, 0.3);
        }
        .alert-danger {
          background: rgba(239, 68, 68, 0.15);
          color: #fca5a5;
          border: 1px solid rgba(239, 68, 68, 0.3);
        }

        /* Badges & Chips */
        .badge {
          display: inline-flex;
          align-items: center;
          gap: 0.35rem;
          font-size: 0.78rem;
          font-weight: 600;
          padding: 0.25rem 0.65rem;
          border-radius: 9999px;
          white-space: nowrap;
        }
        .skill-chip {
          display: inline-flex;
          align-items: center;
          gap: 0.5rem;
          background: rgba(255, 255, 255, 0.05);
          border: 1px solid var(--border-color);
          border-radius: var(--radius-sm);
          padding: 0.4rem 0.75rem;
          font-size: 0.88rem;
          font-weight: 500;
          transition: all 0.2s ease;
        }
        .skill-chip:hover {
          border-color: rgba(255, 255, 255, 0.2);
          background: rgba(255, 255, 255, 0.08);
        }

        /* Grid System */
        .grid-2 {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 1.5rem;
        }
        .grid-3 {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 1.5rem;
        }
        .grid-4 {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 1.25rem;
        }
        @media (max-width: 900px) {
          .grid-2, .grid-3, .grid-4 {
            grid-template-columns: 1fr;
          }
          .nav-links {
            display: none;
          }
        }

        /* Forms */
        .form-group {
          margin-bottom: 1.25rem;
        }
        .form-label {
          display: block;
          font-size: 0.85rem;
          font-weight: 600;
          color: var(--text-muted);
          margin-bottom: 0.4rem;
        }
        .form-control {
          width: 100%;
          background: rgba(15, 23, 42, 0.75);
          border: 1px solid var(--border-color);
          border-radius: var(--radius-sm);
          padding: 0.65rem 0.9rem;
          color: #ffffff;
          font-family: inherit;
          font-size: 0.95rem;
          transition: border-color 0.2s ease;
        }
        .form-control:focus {
          outline: none;
          border-color: var(--primary);
          box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.25);
        }

        /* SVG swap diagram container */
        .swap-cycle-card {
          background: var(--bg-card);
          border: 1px solid var(--border-color);
          border-radius: var(--radius-lg);
          padding: 1.75rem;
          margin-bottom: 2rem;
          transition: all 0.25s ease;
        }
        .swap-cycle-card:hover {
          border-color: var(--border-highlight);
          box-shadow: var(--shadow-glow);
        }
        .swap-cycle-svg {
          width: 100%;
          max-height: 400px;
          display: block;
          margin: 1rem auto;
        }

        /* Hero section */
        .hero {
          text-align: center;
          padding: 4rem 1rem 3rem 1rem;
        }
        .hero h1 {
          font-size: 3.2rem;
          font-weight: 800;
          letter-spacing: -0.03em;
          margin-bottom: 1.25rem;
          line-height: 1.15;
        }
        .hero p {
          font-size: 1.2rem;
          color: var(--text-muted);
          max-width: 680px;
          margin: 0 auto 2.5rem auto;
        }

        /* Footer */
        .footer {
          margin-top: auto;
          border-top: 1px solid var(--border-color);
          background: rgba(15, 23, 42, 0.6);
          padding: 2rem 1.5rem;
          text-align: center;
          font-size: 0.85rem;
          color: var(--text-dim);
        }

        @keyframes slideDown {
          from { opacity: 0; transform: translateY(-8px); }
          to { opacity: 1; transform: translateY(0); }
        }
        """;
    }

    // =========================================================================
    // Page: Home / Landing
    // =========================================================================

    public static String renderHome(Member currentUser, int memberCount, int swapOpportunitiesCount, List<Member> featuredMembers) {
        StringBuilder sb = new StringBuilder();

        // Hero
        sb.append("<section class=\"hero\">\n");
        sb.append("  <span class=\"badge-tag\" style=\"margin-bottom:1rem;display:inline-block;\">💸 Zero-Money Barter Marketplace</span>\n");
        sb.append("  <h1>Trade Skills, Build Mastery.<br><span class=\"text-gradient\">Direct & Cyclic Peer Barter.</span></h1>\n");
        sb.append("  <p>Exchange knowledge directly or through algorithmic 3-way & 4-way cycles. Asha learns Excel from Ravi, Ravi learns Photography from Meena, Meena learns Guitar from Asha.</p>\n");

        sb.append("  <div style=\"display:flex;gap:1rem;justify-content:center;flex-wrap:wrap;\">\n");
        if (currentUser == null) {
            sb.append("    <a href=\"/signup\" class=\"btn btn-primary btn-lg\">Get Started Free</a>\n");
            sb.append("    <a href=\"/login\" class=\"btn btn-outline btn-lg\">Member Log In</a>\n");
        } else {
            sb.append("    <a href=\"/swaps\" class=\"btn btn-primary btn-lg\">⚡ Discover My Swaps</a>\n");
            sb.append("    <a href=\"/profile\" class=\"btn btn-outline btn-lg\">Edit My Skills</a>\n");
        }
        sb.append("  </div>\n");
        sb.append("</section>\n");

        // Feature Highlights
        sb.append("<div class=\"grid-3\" style=\"margin-bottom: 3rem;\">\n");
        sb.append("""
          <div class="glass-card">
            <div style="font-size: 2rem; margin-bottom: 0.75rem;">🔄</div>
            <h3 style="margin-bottom: 0.5rem; font-size: 1.2rem;">Multi-Way Cyclic Swaps</h3>
            <p class="text-muted" style="font-size: 0.9rem;">
              Direct barter often stalls because A has what B wants, but B doesn't have what A wants. Our DFS cycle engine connects 3-way and 4-way circular exchanges.
            </p>
          </div>
          <div class="glass-card">
            <div style="font-size: 2rem; margin-bottom: 0.75rem;">🎯</div>
            <h3 style="margin-bottom: 0.5rem; font-size: 1.2rem;">Level Compatibility</h3>
            <p class="text-muted" style="font-size: 0.9rem;">
              Enforces pedagogical rules: Beginners require Intermediate or Advanced mentors, while Advanced learners match with senior mentors.
            </p>
          </div>
          <div class="glass-card">
            <div style="font-size: 2rem; margin-bottom: 0.75rem;">🔒</div>
            <h3 style="margin-bottom: 0.5rem; font-size: 1.2rem;">Fairness & Privacy Lock</h3>
            <p class="text-muted" style="font-size: 0.9rem;">
              Members in an active proposal are locked to prevent overcommitting. Contacts are revealed only after 100% mutual consensus.
            </p>
          </div>
        """);
        sb.append("</div>\n");

        return sb.toString();
    }

    // =========================================================================
    // Page: Sign Up & Log In
    // =========================================================================

    public static String renderSignup() {
        return """
        <div style="max-width: 480px; margin: 2rem auto;">
          <div class="glass-card">
            <h2 style="font-size: 1.75rem; margin-bottom: 0.5rem; font-weight: 800;">Create Account</h2>
            <p class="text-muted" style="margin-bottom: 1.75rem; font-size: 0.92rem;">Join the skill-sharing barter community</p>

            <form action="/signup" method="POST">
              <div class="form-group">
                <label class="form-label" for="name">Your Full Name or Handle</label>
                <input type="text" id="name" name="name" class="form-control" placeholder="e.g. Elena Rostova" required>
              </div>

              <div class="form-group">
                <label class="form-label" for="pin">Secret 4-Digit Security PIN</label>
                <input type="password" id="pin" name="pin" class="form-control" placeholder="••••" pattern="[0-9]{4,}" title="At least 4 digits" required>
                <small class="text-dim" style="font-size:0.78rem;">Your PIN is cryptographically salted and hashed with SHA-256.</small>
              </div>

              <div class="form-group">
                <label class="form-label" for="contact">Contact Information</label>
                <input type="text" id="contact" name="contact" class="form-control" placeholder="email@college.edu or @telegram" required>
                <small class="text-dim" style="font-size:0.78rem;">🔒 Hidden from public. Only revealed to swap partners after all accept.</small>
              </div>

              <div class="form-group">
                <label class="form-label" for="bio">Short Bio & Academic Focus</label>
                <input type="text" id="bio" name="bio" class="form-control" placeholder="e.g. CS Sophomore passionate about WebDev and Acoustic Guitar">
              </div>

              <button type="submit" class="btn btn-primary btn-md" style="width: 100%; margin-top: 1rem;">Complete Registration</button>
            </form>

            <div style="text-align: center; margin-top: 1.5rem; font-size: 0.9rem;" class="text-muted">
              Already have an account? <a href="/login" style="color: var(--primary); font-weight: 600;">Sign In</a>
            </div>
          </div>
        </div>
        """;
    }

    public static String renderLogin() {
        return """
        <div style="max-width: 440px; margin: 2rem auto;">
          <div class="glass-card">
            <h2 style="font-size: 1.75rem; margin-bottom: 0.5rem; font-weight: 800;">Welcome Back</h2>
            <p class="text-muted" style="margin-bottom: 1.75rem; font-size: 0.92rem;">Enter your student name and PIN</p>

            <form action="/login" method="POST">
              <div class="form-group">
                <label class="form-label" for="name">Name</label>
                <input type="text" id="name" name="name" class="form-control" placeholder="e.g. Asha" required autofocus>
              </div>

              <div class="form-group">
                <label class="form-label" for="pin">PIN</label>
                <input type="password" id="pin" name="pin" class="form-control" placeholder="••••" required>
              </div>

              <button type="submit" class="btn btn-primary btn-md" style="width: 100%; margin-top: 0.5rem;">Log In</button>
            </form>

            <div style="text-align: center; margin-top: 1.5rem; font-size: 0.9rem;" class="text-muted">
              Need an account? <a href="/signup" style="color: var(--primary); font-weight: 600;">Sign Up</a>
            </div>
          </div>
        </div>
        """;
    }

    // =========================================================================
    // Page: Profile & Skill Management (F2)
    // =========================================================================

    public static String renderProfile(Member member) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;\">\n");
        sb.append("  <div>\n");
        sb.append("    <h1 style=\"font-size: 2.2rem; font-weight: 800;\">My Profile & Skills</h1>\n");
        sb.append("    <p class=\"text-muted\">Manage what you teach, what you want to learn, and your weekly barter hours.</p>\n");
        sb.append("  </div>\n");

        if (member.isLocked()) {
            sb.append("  <span class=\"badge\" style=\"background: rgba(239, 68, 68, 0.2); color: #fca5a5; border: 1px solid #ef4444; font-size: 0.88rem;\">\n");
            sb.append("    🔒 Locked in Active Proposal (").append(SecurityUtil.escapeHtml(member.getLockedInProposalId())).append(")\n");
            sb.append("  </span>\n");
        } else {
            sb.append("  <a href=\"/swaps\" class=\"btn btn-primary btn-md\">⚡ Find Available Swaps</a>\n");
        }
        sb.append("</div>\n");

        // Profile Overview & Availability Form
        sb.append("<div class=\"grid-3\" style=\"margin-bottom: 2rem;\">\n");
        sb.append("  <div class=\"glass-card\">\n");
        sb.append("    <h3 style=\"margin-bottom: 0.5rem;\">👤 ").append(SecurityUtil.escapeHtml(member.getName())).append("</h3>\n");
        sb.append("    <p class=\"text-muted\" style=\"font-size:0.9rem; margin-bottom: 1rem;\">").append(SecurityUtil.escapeHtml(member.getBio())).append("</p>\n");
        sb.append("    <div style=\"font-size: 0.88rem; margin-bottom: 0.5rem;\">🔒 Contact: <code>").append(SecurityUtil.escapeHtml(member.getContact())).append("</code></div>\n");
        sb.append("    <div style=\"font-size: 0.88rem; color: #fbbf24;\">⭐ Reputation: <strong>").append(member.getAverageRating()).append(" / 5.0</strong> (").append(member.getRatingCount()).append(" reviews)</div>\n");
        sb.append("  </div>\n");

        // Availability Settings
        sb.append("  <div class=\"glass-card\" style=\"grid-column: span 2;\">\n");
        sb.append("    <h3 style=\"margin-bottom: 0.75rem;\">⏰ Availability & Barter Commitment</h3>\n");
        sb.append("    <form action=\"/profile/availability\" method=\"POST\" style=\"display: flex; gap: 1rem; align-items: flex-end; flex-wrap: wrap;\">\n");
        sb.append("      <div style=\"flex: 1; min-width: 140px;\">\n");
        sb.append("        <label class=\"form-label\">Weekly Hours Available</label>\n");
        sb.append("        <input type=\"number\" name=\"hours\" min=\"1\" max=\"40\" class=\"form-control\" value=\"").append(member.getWeeklyAvailabilityHours()).append("\" required>\n");
        sb.append("      </div>\n");
        sb.append("      <div style=\"flex: 2; min-width: 200px;\">\n");
        sb.append("        <label class=\"form-label\">Schedule Notes</label>\n");
        sb.append("        <input type=\"text\" name=\"note\" class=\"form-control\" value=\"").append(SecurityUtil.escapeHtml(member.getAvailabilityNote())).append("\" placeholder=\"e.g. Weekends, Evenings after 5pm\">\n");
        sb.append("      </div>\n");
        sb.append("      <button type=\"submit\" class=\"btn btn-outline btn-md\">Save Availability</button>\n");
        sb.append("    </form>\n");
        sb.append("  </div>\n");
        sb.append("</div>\n");

        // Skills Grid: Teaches (Offer) vs Wants (Desire)
        sb.append("<div class=\"grid-2\">\n");

        // 1. Skills I Can Teach
        sb.append("  <div class=\"glass-card\">\n");
        sb.append("    <div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;\">\n");
        sb.append("      <div>\n");
        sb.append("        <h2 style=\"font-size: 1.25rem; font-weight: 700; color: #6ee7b7;\">🎓 Skills I Can Teach</h2>\n");
        sb.append("        <p class=\"text-dim\" style=\"font-size: 0.82rem;\">What you offer in trade</p>\n");
        sb.append("      </div>\n");
        sb.append("      <span class=\"badge\" style=\"background: rgba(16, 185, 129, 0.2); color: #6ee7b7;\">").append(member.getTeaches().size()).append(" skills</span>\n");
        sb.append("    </div>\n");

        sb.append("    <div style=\"display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 1.5rem;\">\n");
        if (member.getTeaches().isEmpty()) {
            sb.append("      <div class=\"text-muted\" style=\"padding: 1rem; border: 1px dashed var(--border-color); border-radius: var(--radius-sm); text-align: center;\">No skills listed yet. Add one below!</div>\n");
        } else {
            for (Skill s : member.getTeaches()) {
                sb.append(renderSkillRow(s, "teaches"));
            }
        }
        sb.append("    </div>\n");

        sb.append(renderAddSkillForm("teaches"));
        sb.append("  </div>\n");

        // 2. Skills I Want to Learn
        sb.append("  <div class=\"glass-card\">\n");
        sb.append("    <div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;\">\n");
        sb.append("      <div>\n");
        sb.append("        <h2 style=\"font-size: 1.25rem; font-weight: 700; color: #93c5fd;\">💡 Skills I Want to Learn</h2>\n");
        sb.append("        <p class=\"text-dim\" style=\"font-size: 0.82rem;\">What you desire in trade</p>\n");
        sb.append("      </div>\n");
        sb.append("      <span class=\"badge\" style=\"background: rgba(59, 130, 246, 0.2); color: #93c5fd;\">").append(member.getWants().size()).append(" skills</span>\n");
        sb.append("    </div>\n");

        sb.append("    <div style=\"display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 1.5rem;\">\n");
        if (member.getWants().isEmpty()) {
            sb.append("      <div class=\"text-muted\" style=\"padding: 1rem; border: 1px dashed var(--border-color); border-radius: var(--radius-sm); text-align: center;\">No desired skills listed yet. Add one below!</div>\n");
        } else {
            for (Skill s : member.getWants()) {
                sb.append(renderSkillRow(s, "wants"));
            }
        }
        sb.append("    </div>\n");

        sb.append(renderAddSkillForm("wants"));
        sb.append("  </div>\n");

        sb.append("</div>\n");
        return sb.toString();
    }

    private static String renderSkillRow(Skill skill, String skillType) {
        return """
        <div style="display: flex; justify-content: space-between; align-items: center; background: rgba(15, 23, 42, 0.6); padding: 0.65rem 0.9rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">
          <div style="display: flex; align-items: center; gap: 0.6rem;">
            <span style="font-size: 1.1rem;">%s</span>
            <div>
              <strong style="font-size: 0.95rem;">%s</strong>
              <div style="display: flex; gap: 0.4rem; margin-top: 0.2rem;">
                <span class="badge" style="background: rgba(255,255,255,0.06); font-size: 0.72rem; color: #94a3b8;">%s</span>
                <span class="badge" style="background: %s; color: #ffffff; font-size: 0.72rem;">%s</span>
              </div>
            </div>
          </div>
          <form action="/profile/skills/remove" method="POST" style="margin: 0;">
            <input type="hidden" name="type" value="%s">
            <input type="hidden" name="name" value="%s">
            <button type="submit" class="btn btn-ghost btn-sm" title="Remove skill" style="color: #ef4444; padding: 0.3rem 0.6rem;">✕</button>
          </form>
        </div>
        """.formatted(
            skill.getCategory().getEmoji(),
            SecurityUtil.escapeHtml(skill.getName()),
            SecurityUtil.escapeHtml(skill.getCategory().getDisplayName()),
            skill.getLevel().getColor(),
            skill.getLevel().getDisplayName(),
            skillType,
            SecurityUtil.escapeHtml(skill.getName())
        );
    }

    private static String renderAddSkillForm(String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("<form action=\"/profile/skills/add\" method=\"POST\" style=\"background: rgba(15, 23, 42, 0.4); padding: 1rem; border-radius: var(--radius-sm); border: 1px dashed var(--border-color);\">\n");
        sb.append("  <input type=\"hidden\" name=\"type\" value=\"").append(type).append("\">\n");
        sb.append("  <div style=\"font-weight: 600; font-size: 0.88rem; margin-bottom: 0.75rem;\">+ Add New Skill</div>\n");

        sb.append("  <div style=\"display: flex; flex-direction: column; gap: 0.65rem;\">\n");
        sb.append("    <input type=\"text\" name=\"name\" class=\"form-control\" placeholder=\"Skill name (e.g. Python, Guitar, UI Design)\" required>\n");

        sb.append("    <div style=\"display: grid; grid-template-columns: 1fr 1fr; gap: 0.65rem;\">\n");
        sb.append("      <select name=\"category\" class=\"form-control\">\n");
        for (SkillCategory cat : SkillCategory.values()) {
            sb.append("        <option value=\"").append(cat.name()).append("\">").append(cat.getEmoji()).append(" ").append(cat.getDisplayName()).append("</option>\n");
        }
        sb.append("      </select>\n");

        sb.append("      <select name=\"level\" class=\"form-control\">\n");
        for (SkillLevel lvl : SkillLevel.values()) {
            sb.append("        <option value=\"").append(lvl.name()).append("\">").append(lvl.getDisplayName()).append("</option>\n");
        }
        sb.append("      </select>\n");
        sb.append("    </div>\n");

        sb.append("    <button type=\"submit\" class=\"btn btn-primary btn-sm\" style=\"margin-top: 0.25rem;\">Add to ").append("teaches".equals(type) ? "Teaches" : "Wants").append("</button>\n");
        sb.append("  </div>\n");
        sb.append("</form>\n");
        return sb.toString();
    }

    // =========================================================================
    // Page: Swap Discovery (F3 & F7)
    // =========================================================================

    public static String renderSwaps(Member currentUser, List<SwapCycle> discoveredCycles, List<AlmostMatchFinder.Opportunity> almostMatches, int maxLen) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div style=\"display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;\">\n");
        sb.append("  <div>\n");
        sb.append("    <h1 style=\"font-size: 2.2rem; font-weight: 800;\">⚡ Swap Discovery</h1>\n");
        sb.append("    <p class=\"text-muted\">Ranked multi-way barter cycles involving <strong>").append(SecurityUtil.escapeHtml(currentUser.getName())).append("</strong></p>\n");
        sb.append("  </div>\n");

        // Length Filter Tabs
        sb.append("  <div style=\"display: flex; align-items: center; gap: 0.5rem; background: rgba(15, 23, 42, 0.7); padding: 0.3rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);\">\n");
        sb.append("    <span style=\"font-size: 0.8rem; color: var(--text-muted); margin-left: 0.4rem;\">Max Depth:</span>\n");
        for (int l = 2; l <= 4; l++) {
            boolean active = l == maxLen;
            sb.append("    <a href=\"/swaps?maxLen=").append(l).append("\" class=\"btn btn-sm ").append(active ? "btn-primary" : "btn-ghost").append("\">").append(l).append("-Way</a>\n");
        }
        sb.append("  </div>\n");
        sb.append("</div>\n");

        if (currentUser.isLocked()) {
            sb.append("<div class=\"alert alert-danger\">\n");
            sb.append("  <span>🔒 You are currently committed to an active swap proposal. You cannot propose new swaps until it finishes or cancels.</span>\n");
            sb.append("</div>\n");
        }

        // Section 1: Discovered Cycles
        if (discoveredCycles.isEmpty()) {
            sb.append("<div class=\"glass-card\" style=\"text-align: center; padding: 3.5rem 1.5rem; margin-bottom: 2.5rem;\">\n");
            sb.append("  <div style=\"font-size: 3rem; margin-bottom: 1rem;\">🔍</div>\n");
            sb.append("  <h3 style=\"font-size: 1.4rem; margin-bottom: 0.5rem;\">No Direct or Cyclic Swaps Found Right Now</h3>\n");
            sb.append("  <p class=\"text-muted\" style=\"max-width: 500px; margin: 0 auto 1.5rem auto;\">Try adding more skills to your 'Wants' or 'Teaches' list on your profile, or explore the almost-matches below!</p>\n");
            sb.append("  <a href=\"/profile\" class=\"btn btn-primary btn-md\">Manage My Skills</a>\n");
            sb.append("</div>\n");
        } else {
            sb.append("<div style=\"margin-bottom: 1.5rem; display: flex; justify-content: space-between; align-items: center;\">\n");
            sb.append("  <h2 style=\"font-size: 1.3rem; font-weight: 700;\">Discovered Matches (").append(discoveredCycles.size()).append(")</h2>\n");
            sb.append("  <span class=\"text-muted\" style=\"font-size: 0.85rem;\">Ranked by Composite Score (Length, Ratings & Availability)</span>\n");
            sb.append("</div>\n");

            for (int i = 0; i < discoveredCycles.size(); i++) {
                SwapCycle cycle = discoveredCycles.get(i);
                sb.append(renderSwapCycleCard(cycle, currentUser, i + 1));
            }
        }

        // Section 2: Almost-Matches (Feature F7)
        if (almostMatches != null && !almostMatches.isEmpty()) {
            sb.append("<div style=\"margin-top: 3.5rem;\">\n");
            sb.append("  <div style=\"display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem;\">\n");
            sb.append("    <h2 style=\"font-size: 1.4rem; font-weight: 800; color: #fbbf24;\">✨ Almost-Matches & Opportunities (Feature F7)</h2>\n");
            sb.append("    <span class=\"badge\" style=\"background: rgba(251, 191, 36, 0.2); color: #fbbf24;\">Stretch Feature</span>\n");
            sb.append("  </div>\n");
            sb.append("  <p class=\"text-muted\" style=\"font-size: 0.92rem; margin-bottom: 1.5rem;\">Adding any of these in-demand skills to your <em>Wanted Skills</em> will instantly unlock new barter cycles:</p>\n");

            sb.append("  <div class=\"grid-3\">\n");
            for (AlmostMatchFinder.Opportunity opp : almostMatches) {
                sb.append(renderAlmostMatchCard(opp));
            }
            sb.append("  </div>\n");
            sb.append("</div>\n");
        }

        return sb.toString();
    }

    private static String renderSwapCycleCard(SwapCycle cycle, Member currentUser, int rank) {
        StringBuilder sb = new StringBuilder();
        int size = cycle.size();

        sb.append("<div class=\"swap-cycle-card\">\n");
        sb.append("  <div style=\"display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; border-bottom: 1px solid var(--border-color); padding-bottom: 1rem;\">\n");
        sb.append("    <div style=\"display: flex; align-items: center; gap: 0.75rem;\">\n");
        sb.append("      <span style=\"font-size: 1.1rem; font-weight: 800; color: #a5b4fc;\">#").append(rank).append("</span>\n");
        sb.append("      <h3 style=\"font-size: 1.25rem; font-weight: 700;\">").append(size).append("-Way Barter Exchange</h3>\n");
        sb.append("      <span class=\"badge\" style=\"background: rgba(99, 102, 241, 0.2); color: #a5b4fc;\">").append(size == 2 ? "Direct 2-Way" : size + "-Way Cyclic").append("</span>\n");
        sb.append("    </div>\n");

        sb.append("    <div style=\"display: flex; align-items: center; gap: 1rem;\">\n");
        sb.append("      <div style=\"text-align: right;\">\n");
        sb.append("        <div style=\"font-size: 0.78rem; color: var(--text-muted);\">Match Score</div>\n");
        sb.append("        <strong style=\"color: #10b981; font-size: 1.15rem;\">").append(String.format("%.1f%%", cycle.getScore() * 100)).append("</strong>\n");
        sb.append("      </div>\n");

        // Action: Propose Swap
        if (!currentUser.isLocked()) {
            sb.append("      <form action=\"/proposals\" method=\"POST\" style=\"margin:0;\">\n");
            sb.append("        <input type=\"hidden\" name=\"signature\" value=\"").append(SecurityUtil.escapeHtml(cycle.getCanonicalSignature())).append("\">\n");
            sb.append("        <button type=\"submit\" class=\"btn btn-success btn-md\">🤝 Propose Swap</button>\n");
            sb.append("      </form>\n");
        } else {
            sb.append("      <button class=\"btn btn-outline btn-md\" disabled style=\"opacity:0.5;\">Locked</button>\n");
        }
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        // Inline SVG Diagram (Bonus Feature in PRD F3)
        sb.append("  <div style=\"margin: 1rem 0; background: rgba(15, 23, 42, 0.5); border-radius: var(--radius-md); padding: 0.5rem;\">\n");
        sb.append(SvgGenerator.renderCycleSvg(cycle, currentUser.getId()));
        sb.append("  </div>\n");

        // Cycle Breakdown Table
        sb.append("  <div style=\"display: flex; flex-direction: column; gap: 0.5rem;\">\n");
        for (SwapLink link : cycle.getLinks()) {
            boolean isUserLearner = link.getLearner().equals(currentUser);
            boolean isUserTeacher = link.getTeacher().equals(currentUser);

            sb.append("    <div style=\"display: flex; justify-content: space-between; align-items: center; background: rgba(255,255,255,0.03); padding: 0.6rem 1rem; border-radius: var(--radius-sm);\">\n");
            sb.append("      <div style=\"display: flex; align-items: center; gap: 0.6rem;\">\n");
            sb.append("        <strong style=\"color:").append(isUserLearner ? "#fbbf24" : "#e2e8f0").append(";\">").append(isUserLearner ? "You" : SecurityUtil.escapeHtml(link.getLearner().getName())).append("</strong>\n");
            sb.append("        <span class=\"text-muted\">learns</span>\n");
            sb.append("        <span class=\"badge\" style=\"background:rgba(99, 102, 241, 0.25); color:#c7d2fe;\">").append(link.getSkill().getCategory().getEmoji()).append(" ").append(SecurityUtil.escapeHtml(link.getSkill().getName())).append(" (").append(link.getSkill().getLevel().getDisplayName()).append(")</span>\n");
            sb.append("        <span class=\"text-muted\">from</span>\n");
            sb.append("        <strong style=\"color:").append(isUserTeacher ? "#fbbf24" : "#e2e8f0").append(";\">").append(isUserTeacher ? "You" : SecurityUtil.escapeHtml(link.getTeacher().getName())).append("</strong>\n");
            sb.append("      </div>\n");
            sb.append("      <span style=\"font-size: 0.8rem; color: #94a3b8;\">⭐ ").append(link.getTeacher().getAverageRating()).append("</span>\n");
            sb.append("    </div>\n");
        }
        sb.append("  </div>\n");

        sb.append("</div>\n");
        return sb.toString();
    }

    private static String renderAlmostMatchCard(AlmostMatchFinder.Opportunity opp) {
        Skill skill = opp.getSuggestedSkill();
        return """
        <div class="glass-card" style="border-color: rgba(251, 191, 36, 0.3);">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.75rem;">
            <div>
              <span style="font-size: 1.5rem;">%s</span>
              <h3 style="font-size: 1.15rem; font-weight: 700; margin-top: 0.25rem;">%s</h3>
              <span class="badge" style="background: rgba(255,255,255,0.06); font-size: 0.75rem; color: #94a3b8;">%s</span>
            </div>
            <span class="badge" style="background: #fbbf24; color: #000; font-weight: 700;">+%d Swaps</span>
          </div>
          <p class="text-muted" style="font-size: 0.85rem; margin-bottom: 1.25rem;">
            If you also want to learn <strong>%s</strong>, <strong>%d more multi-way swaps</strong> open up for you immediately!
          </p>
          <form action="/profile/skills/add" method="POST">
            <input type="hidden" name="type" value="wants">
            <input type="hidden" name="name" value="%s">
            <input type="hidden" name="category" value="%s">
            <input type="hidden" name="level" value="BEGINNER">
            <button type="submit" class="btn btn-outline btn-sm" style="width: 100%%; border-color: #fbbf24; color: #fbbf24;">+ Add to My Wants</button>
          </form>
        </div>
        """.formatted(
            skill.getCategory().getEmoji(),
            SecurityUtil.escapeHtml(skill.getName()),
            skill.getCategory().getDisplayName(),
            opp.getUnlockedCyclesCount(),
            SecurityUtil.escapeHtml(skill.getName()),
            opp.getUnlockedCyclesCount(),
            SecurityUtil.escapeHtml(skill.getName()),
            skill.getCategory().name()
        );
    }

    // =========================================================================
    // Page: Proposals (F4, F5, F6)
    // =========================================================================

    public static String renderProposals(Member currentUser, List<SwapProposal> proposals) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;\">\n");
        sb.append("  <div>\n");
        sb.append("    <h1 style=\"font-size: 2.2rem; font-weight: 800;\">🤝 Swap Proposals</h1>\n");
        sb.append("    <p class=\"text-muted\">Track active proposals, mutual acceptances, and confidential contact reveals.</p>\n");
        sb.append("  </div>\n");
        sb.append("  <a href=\"/swaps\" class=\"btn btn-primary btn-md\">⚡ Find New Swaps</a>\n");
        sb.append("</div>\n");

        if (proposals.isEmpty()) {
            sb.append("<div class=\"glass-card\" style=\"text-align: center; padding: 3.5rem 1.5rem;\">\n");
            sb.append("  <div style=\"font-size: 3rem; margin-bottom: 1rem;\">📭</div>\n");
            sb.append("  <h3 style=\"font-size: 1.35rem; margin-bottom: 0.5rem;\">No Proposals Yet</h3>\n");
            sb.append("  <p class=\"text-muted\" style=\"max-width: 480px; margin: 0 auto 1.5rem auto;\">You haven't initiated or received any swap proposals. Head to Find Swaps to propose your first exchange!</p>\n");
            sb.append("  <a href=\"/swaps\" class=\"btn btn-primary btn-md\">Browse Available Matches</a>\n");
            sb.append("</div>\n");
            return sb.toString();
        }

        for (SwapProposal p : proposals) {
            sb.append(renderProposalCard(p, currentUser));
        }

        return sb.toString();
    }

    private static String renderProposalCard(SwapProposal p, Member currentUser) {
        StringBuilder sb = new StringBuilder();
        ProposalStatus status = p.getStatus();
        ResponseStatus myResponse = p.getResponseFor(currentUser.getId());
        boolean isInitiator = p.getInitiatorMemberId().equalsIgnoreCase(currentUser.getId());

        sb.append("<div class=\"glass-card\" style=\"margin-bottom: 1.75rem; border-color: ").append(status == ProposalStatus.ACTIVE ? "rgba(16, 185, 129, 0.4)" : "var(--border-color)").append(";\">\n");

        // Header
        sb.append("  <div style=\"display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; border-bottom: 1px solid var(--border-color); padding-bottom: 1rem; margin-bottom: 1.25rem;\">\n");
        sb.append("    <div style=\"display: flex; align-items: center; gap: 0.75rem;\">\n");
        sb.append("      <span class=\"badge\" style=\"background: ").append(status.getColor()).append("; color: #fff;\">").append(status.getIcon()).append(" ").append(status.getLabel()).append("</span>\n");
        sb.append("      <span style=\"font-size: 0.85rem; color: var(--text-dim);\">Proposal #").append(SecurityUtil.escapeHtml(p.getId())).append("</span>\n");
        if (isInitiator) {
            sb.append("      <span class=\"badge\" style=\"background: rgba(99, 102, 241, 0.2); color: #a5b4fc;\">Created by You</span>\n");
        }
        sb.append("    </div>\n");

        // 48h Expiration Countdown
        long remainingMs = Math.max(0, p.getExpiresAt() - System.currentTimeMillis());
        long hoursLeft = remainingMs / (1000 * 60 * 60);
        long minsLeft = (remainingMs / (1000 * 60)) % 60;

        if (status == ProposalStatus.PENDING) {
            sb.append("    <div style=\"font-size: 0.85rem; color: #f59e0b;\">⏳ Expires in <strong>").append(hoursLeft).append("h ").append(minsLeft).append("m</strong></div>\n");
        } else if (status == ProposalStatus.ACTIVE) {
            sb.append("    <div style=\"font-size: 0.85rem; color: #10b981;\">🚀 Barter Sessions in Progress!</div>\n");
        }
        sb.append("  </div>\n");

        // Participants Acceptance Status Bar
        sb.append("  <div style=\"margin-bottom: 1.25rem;\">\n");
        sb.append("    <div style=\"font-size: 0.85rem; font-weight: 600; color: var(--text-muted); margin-bottom: 0.5rem;\">Mutual Consensus Checklist (All must accept):</div>\n");
        sb.append("    <div style=\"display: flex; gap: 0.75rem; flex-wrap: wrap;\">\n");

        for (Member m : p.getCycle().getMembers()) {
            ResponseStatus resp = p.getResponseFor(m.getId());
            boolean isMe = m.equals(currentUser);

            sb.append("      <div style=\"display: flex; align-items: center; gap: 0.4rem; background: rgba(15, 23, 42, 0.6); padding: 0.4rem 0.75rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color);\">\n");
            sb.append("        <span style=\"font-size: 0.9rem;\">").append(resp != null ? resp.getIcon() : "⏳").append("</span>\n");
            sb.append("        <strong style=\"font-size: 0.85rem; color: ").append(isMe ? "#fbbf24" : "#e2e8f0").append(";\">").append(isMe ? "You" : SecurityUtil.escapeHtml(m.getName())).append("</strong>\n");
            sb.append("        <span style=\"font-size: 0.75rem; color: ").append(resp != null ? resp.getColor() : "#94a3b8").append(";\">(").append(resp != null ? resp.getLabel() : "Pending").append(")</span>\n");
            sb.append("      </div>\n");
        }
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        // Action Buttons for Current User
        if (status == ProposalStatus.PENDING) {
            sb.append("  <div style=\"display: flex; gap: 0.75rem; margin-bottom: 1.25rem; flex-wrap: wrap;\">\n");
            if (myResponse == ResponseStatus.PENDING) {
                sb.append("    <form action=\"/proposals/").append(p.getId()).append("/accept\" method=\"POST\">\n");
                sb.append("      <button type=\"submit\" class=\"btn btn-success btn-md\">✅ Accept Swap Proposal</button>\n");
                sb.append("    </form>\n");
                sb.append("    <form action=\"/proposals/").append(p.getId()).append("/decline\" method=\"POST\">\n");
                sb.append("      <button type=\"submit\" class=\"btn btn-danger btn-md\">❌ Decline & Cancel</button>\n");
                sb.append("    </form>\n");
            } else if (isInitiator) {
                sb.append("    <form action=\"/proposals/").append(p.getId()).append("/cancel\" method=\"POST\">\n");
                sb.append("      <button type=\"submit\" class=\"btn btn-outline btn-md\" style=\"color: #ef4444;\">Cancel My Proposal</button>\n");
                sb.append("    </form>\n");
            }
            sb.append("  </div>\n");
        } else if (status == ProposalStatus.ACTIVE) {
            sb.append("  <div style=\"display: flex; gap: 0.75rem; margin-bottom: 1.25rem; flex-wrap: wrap;\">\n");
            sb.append("    <form action=\"/proposals/").append(p.getId()).append("/complete\" method=\"POST\">\n");
            sb.append("      <button type=\"submit\" class=\"btn btn-primary btn-md\">🎉 Mark Swap Completed & Enable Ratings</button>\n");
            sb.append("    </form>\n");
            sb.append("  </div>\n");
        }

        // Contact Reveal Box (Feature F5)
        sb.append("  <div style=\"background: rgba(15, 23, 42, 0.75); border-radius: var(--radius-sm); padding: 1.25rem; border: 1px solid var(--border-color);\">\n");
        sb.append("    <div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;\">\n");
        sb.append("      <div style=\"font-size: 0.95rem; font-weight: 700;\">📬 Swap Contacts & Session Logistics</div>\n");

        if (p.areContactsRevealed()) {
            sb.append("      <span class=\"badge\" style=\"background: #10b981; color: #fff;\">🔓 Contacts Revealed!</span>\n");
        } else {
            sb.append("      <span class=\"badge\" style=\"background: rgba(255, 255, 255, 0.08); color: #94a3b8;\">🔒 Hidden until all accept</span>\n");
        }
        sb.append("    </div>\n");

        if (p.areContactsRevealed()) {
            sb.append("    <div class=\"grid-3\" style=\"gap: 0.75rem;\">\n");
            for (SwapLink link : p.getCycle().getLinks()) {
                Member teacher = link.getTeacher();
                boolean isTeacherMe = teacher.equals(currentUser);

                sb.append("      <div style=\"background: rgba(255,255,255,0.04); padding: 0.75rem; border-radius: var(--radius-sm); border: 1px solid rgba(16, 185, 129, 0.2);\">\n");
                sb.append("        <strong style=\"color: ").append(isTeacherMe ? "#fbbf24" : "#6ee7b7").append(";\">").append(isTeacherMe ? "Your Contact Info" : SecurityUtil.escapeHtml(teacher.getName())).append("</strong>\n");
                sb.append("        <div style=\"font-size: 0.85rem; margin-top: 0.25rem; color: #f8fafc;\"><code>").append(SecurityUtil.escapeHtml(teacher.getContact())).append("</code></div>\n");
                sb.append("        <div style=\"font-size: 0.78rem; color: #94a3b8; margin-top: 0.25rem;\">Teaches: ").append(SecurityUtil.escapeHtml(link.getSkill().getName())).append("</div>\n");
                sb.append("        <div style=\"font-size: 0.75rem; color: #a5b4fc;\">Availability: ").append(SecurityUtil.escapeHtml(teacher.getAvailabilityNote())).append("</div>\n");
                sb.append("      </div>\n");
            }
            sb.append("    </div>\n");
        } else {
            sb.append("    <p class=\"text-dim\" style=\"font-size: 0.85rem;\">For student safety and anti-spam protection, phone numbers and email contacts remain confidential until every member in this cycle confirms their acceptance.</p>\n");
        }
        sb.append("  </div>\n");

        // Rating Section (Feature F6) if completed
        if (status == ProposalStatus.COMPLETED) {
            SwapLink myLink = p.getCycle().getLinkForLearner(currentUser.getId());
            if (myLink != null) {
                Member myTeacher = myLink.getTeacher();
                sb.append(renderRatingForm(p.getId(), currentUser.getId(), myTeacher));
            }
        }

        sb.append("</div>\n");
        return sb.toString();
    }

    private static String renderRatingForm(String proposalId, String learnerId, Member teacher) {
        return """
        <div style="margin-top: 1.25rem; background: rgba(139, 92, 246, 0.1); border: 1px solid rgba(139, 92, 246, 0.3); border-radius: var(--radius-sm); padding: 1.25rem;">
          <h4 style="font-size: 1.05rem; font-weight: 700; color: #c4b5fd; margin-bottom: 0.4rem;">⭐ Rate Your Teacher: %s</h4>
          <p class="text-muted" style="font-size: 0.85rem; margin-bottom: 1rem;">Leave feedback for your barter session. Your peer review directly influences future cycle matching scores!</p>

          <form action="/ratings" method="POST" style="display: flex; gap: 1rem; align-items: flex-end; flex-wrap: wrap;">
            <input type="hidden" name="proposalId" value="%s">
            <input type="hidden" name="teacherId" value="%s">
            
            <div>
              <label class="form-label">Score (1-5 Stars)</label>
              <select name="score" class="form-control" style="width: 140px;">
                <option value="5">⭐⭐⭐⭐⭐ (5.0)</option>
                <option value="4">⭐⭐⭐⭐ (4.0)</option>
                <option value="3">⭐⭐⭐ (3.0)</option>
                <option value="2">⭐⭐ (2.0)</option>
                <option value="1">⭐ (1.0)</option>
              </select>
            </div>

            <div style="flex: 1; min-width: 220px;">
              <label class="form-label">Review / Feedback</label>
              <input type="text" name="feedback" class="form-control" placeholder="Great explanations and very patient mentor!">
            </div>

            <button type="submit" class="btn btn-primary btn-md">Submit Teacher Rating</button>
          </form>
        </div>
        """.formatted(
            SecurityUtil.escapeHtml(teacher.getName()),
            proposalId,
            teacher.getId()
        );
    }

    // =========================================================================
    // Page: Community Member Directory
    // =========================================================================

    public static String renderMembers(List<Member> members, Member currentUser) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;\">\n");
        sb.append("  <div>\n");
        sb.append("    <h1 style=\"font-size: 2.2rem; font-weight: 800;\">🌍 SkillSwap Community</h1>\n");
        sb.append("    <p class=\"text-muted\">Discover fellow students, explore skills offered and needed, and see what the community is learning.</p>\n");
        sb.append("  </div>\n");

        sb.append("</div>\n");

        sb.append("<div class=\"grid-3\">\n");
        for (Member m : members) {
            boolean isMe = currentUser != null && m.equals(currentUser);

            sb.append("<div class=\"glass-card\" style=\"border-color: ").append(isMe ? "#6366f1" : "var(--border-color)").append(";\">\n");
            sb.append("  <div style=\"display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.75rem;\">\n");
            sb.append("    <div>\n");
            sb.append("      <h3 style=\"font-size: 1.2rem; font-weight: 700;\">").append(SecurityUtil.escapeHtml(m.getName())).append(isMe ? " <span style=\"color:#fbbf24;font-size:0.85rem;\">(You)</span>" : "").append("</h3>\n");
            sb.append("      <div style=\"font-size: 0.8rem; color: #fbbf24; margin-top: 0.2rem;\">⭐ ").append(m.getAverageRating()).append(" / 5.0 (").append(m.getRatingCount()).append(" reviews)</div>\n");
            sb.append("    </div>\n");
            if (m.isLocked()) {
                sb.append("    <span class=\"badge\" style=\"background:rgba(239,68,68,0.2);color:#fca5a5;\">Locked</span>\n");
            } else {
                sb.append("    <span class=\"badge\" style=\"background:rgba(16,185,129,0.2);color:#6ee7b7;\">Available</span>\n");
            }
            sb.append("  </div>\n");

            if (m.getBio() != null && !m.getBio().isEmpty()) {
                sb.append("  <p class=\"text-muted\" style=\"font-size: 0.88rem; margin-bottom: 1rem;\">").append(SecurityUtil.escapeHtml(m.getBio())).append("</p>\n");
            }

            // Teaches chips
            sb.append("  <div style=\"margin-bottom: 0.75rem;\">\n");
            sb.append("    <div style=\"font-size: 0.78rem; font-weight: 700; color: #6ee7b7; text-transform: uppercase; margin-bottom: 0.35rem;\">Teaches</div>\n");
            sb.append("    <div style=\"display: flex; flex-wrap: wrap; gap: 0.35rem;\">\n");
            for (Skill s : m.getTeaches()) {
                sb.append("      <span class=\"badge\" style=\"background: rgba(16, 185, 129, 0.15); color: #6ee7b7;\">").append(s.getCategory().getEmoji()).append(" ").append(SecurityUtil.escapeHtml(s.getName())).append("</span>\n");
            }
            sb.append("    </div>\n");
            sb.append("  </div>\n");

            // Wants chips
            sb.append("  <div style=\"margin-bottom: 1rem;\">\n");
            sb.append("    <div style=\"font-size: 0.78rem; font-weight: 700; color: #93c5fd; text-transform: uppercase; margin-bottom: 0.35rem;\">Wants to Learn</div>\n");
            sb.append("    <div style=\"display: flex; flex-wrap: wrap; gap: 0.35rem;\">\n");
            for (Skill s : m.getWants()) {
                sb.append("      <span class=\"badge\" style=\"background: rgba(59, 130, 246, 0.15); color: #93c5fd;\">").append(s.getCategory().getEmoji()).append(" ").append(SecurityUtil.escapeHtml(s.getName())).append("</span>\n");
            }
            sb.append("    </div>\n");
            sb.append("  </div>\n");

            sb.append("  <div style=\"font-size: 0.8rem; color: var(--text-dim); border-top: 1px solid var(--border-color); padding-top: 0.6rem;\">\n");
            sb.append("    ⏰ ").append(m.getWeeklyAvailabilityHours()).append(" hrs/wk &bull; ").append(SecurityUtil.escapeHtml(m.getAvailabilityNote())).append("\n");
            sb.append("  </div>\n");

            sb.append("</div>\n");
        }
        sb.append("</div>\n");

        return sb.toString();
    }
}
