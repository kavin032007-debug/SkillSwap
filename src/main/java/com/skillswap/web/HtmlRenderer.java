package com.skillswap.web;

import com.skillswap.finder.AlmostMatchFinder;
import com.skillswap.model.*;
import com.skillswap.util.SecurityUtil;
import com.skillswap.util.SvgGenerator;

import java.util.*;

/**
 * Modern editorial & neo-brutalist frontend UI renderer for SkillSwap.
 * Features high-contrast ink/lime palette, Bricolage Grotesque & DM Sans typography,
 * interactive barter matchers, inline SVG cycle visualizations, and responsive cards.
 * Zero external template engines or runtime dependencies.
 */
public class HtmlRenderer {

    // =========================================================================
    // Base Page Layout with Design System
    // =========================================================================

    public static String renderPage(String title, String activeNav, Member currentUser, String contentHtml, String flashSuccess, String flashError) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        sb.append("<meta charset=\"UTF-8\">\n");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("<title>").append(SecurityUtil.escapeHtml(title)).append(" | SkillSwap</title>\n");
        sb.append("<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n");
        sb.append("<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n");
        sb.append("<link href=\"https://fonts.googleapis.com/css2?family=Bricolage+Grotesque:opsz,wght@12..96,600;700;800&family=DM+Sans:ital,opsz,wght@0,9..40,400;0,9..40,500;0,9..40,700;1,9..40,400&display=swap\" rel=\"stylesheet\">\n");
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
        sb.append("      <span class=\"brand-icon\">\n");
        sb.append("        <svg width=\"22\" height=\"22\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"#CDF23A\" stroke-width=\"2.6\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M4 9h13l-3-3\"></path><path d=\"M20 15H7l3 3\"></path></svg>\n");
        sb.append("      </span>\n");
        sb.append("      <span class=\"brand-name\">SkillSwap</span>\n");
        sb.append("    </a>\n");

        sb.append("    <nav class=\"nav-links\">\n");
        if (currentUser == null) {
            sb.append("      <a href=\"/\" class=\"nav-link ").append("home".equals(activeNav) ? "active" : "").append("\">Home</a>\n");
            sb.append("      <a href=\"/#how\" class=\"nav-link\">How it works</a>\n");
            sb.append("      <a href=\"/#why\" class=\"nav-link\">Why it's fair</a>\n");
            sb.append("      <a href=\"/members\" class=\"nav-link ").append("members".equals(activeNav) ? "active" : "").append("\">Community</a>\n");
        } else {
            sb.append("      <a href=\"/swaps\" class=\"nav-link ").append("swaps".equals(activeNav) ? "active" : "").append("\">Dashboard</a>\n");
            sb.append("      <a href=\"/profile\" class=\"nav-link ").append("profile".equals(activeNav) ? "active" : "").append("\">My Skills</a>\n");
            sb.append("      <a href=\"/proposals\" class=\"nav-link ").append("proposals".equals(activeNav) ? "active" : "").append("\">Proposals</a>\n");
            sb.append("      <a href=\"/members\" class=\"nav-link ").append("members".equals(activeNav) ? "active" : "").append("\">Community</a>\n");
        }
        sb.append("    </nav>\n");

        sb.append("    <div class=\"nav-actions\">\n");
        if (currentUser != null) {
            String initial = currentUser.getName().substring(0, 1).toUpperCase();
            String avClass = getAvatarClass(currentUser.getName());
            sb.append("      <a href=\"/profile\" class=\"user-pill\">\n");
            sb.append("        <span class=\"av ").append(avClass).append("\" style=\"width:32px;height:32px;font-size:13px;\">").append(SecurityUtil.escapeHtml(initial)).append("</span>\n");
            sb.append("        <span style=\"font-weight:700;\">").append(SecurityUtil.escapeHtml(currentUser.getName())).append("</span>\n");
            sb.append("      </a>\n");
            sb.append("      <form action=\"/logout\" method=\"POST\" style=\"display:inline;\">\n");
            sb.append("        <button type=\"submit\" class=\"btn btn-ghost\" style=\"min-height:38px;padding:0 16px;font-size:14px;\">Sign Out</button>\n");
            sb.append("      </form>\n");
        } else {
            sb.append("      <a href=\"/login\" class=\"btn btn-ghost\" style=\"min-height:40px;padding:0 18px;\">Log In</a>\n");
            sb.append("      <a href=\"/signup\" class=\"btn btn-lime\" style=\"min-height:40px;padding:0 20px;\">Join Free</a>\n");
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
            <div style="display:flex;align-items:center;gap:10px;">
              <span style="width:32px;height:32px;border-radius:10px;background:#0D1F1A;display:inline-flex;align-items:center;justify-content:center;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#CDF23A" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M4 9h13l-3-3"></path><path d="M20 15H7l3 3"></path></svg>
              </span>
              <span style="font-family:'Bricolage Grotesque',sans-serif;font-weight:800;font-size:18px;">SkillSwap</span>
              <span style="color:#4A5B55;">· a money-free skill barter for students</span>
            </div>
            <div style="color:#4A5B55;font-size:14px;">
              Algorithmic Skill Barter Network · Direct & Cyclic Exchange
            </div>
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
          --bg-page: #F1F4EF;
          --color-ink: #0D1F1A;
          --color-lime: #CDF23A;
          --color-lavender: #C9B8FF;
          --color-coral: #F7A58E;
          --color-blue: #3A3FD8;
          --color-sky: #8FD8F2;
          --color-yellow: #FBE08A;
          --color-muted: #33463F;
          --color-dim: #4A5B55;
          --border-ink: 2px solid #0D1F1A;
        }

        * {
          box-sizing: border-box;
          margin: 0;
          padding: 0;
        }

        body {
          margin: 0;
          background: var(--bg-page);
          color: var(--color-ink);
          font-family: 'DM Sans', system-ui, -apple-system, sans-serif;
          -webkit-font-smoothing: antialiased;
          line-height: 1.5;
          min-height: 100vh;
          display: flex;
          flex-direction: column;
        }

        h1, h2, h3, h4 {
          font-family: 'Bricolage Grotesque', 'DM Sans', sans-serif;
          margin: 0;
          letter-spacing: -0.02em;
          color: var(--color-ink);
        }

        a {
          color: inherit;
          text-decoration: none;
        }

        button {
          font-family: inherit;
          cursor: pointer;
        }

        .main-container {
          flex: 1;
          width: 100%;
          max-width: 1240px;
          margin: 0 auto;
          padding: 32px 24px 64px;
        }

        /* Navbar */
        .navbar {
          position: sticky;
          top: 0;
          z-index: 100;
          background: rgba(241, 244, 239, 0.94);
          backdrop-filter: blur(12px);
          border-bottom: var(--border-ink);
        }
        .nav-inner {
          max-width: 1240px;
          margin: 0 auto;
          padding: 16px 24px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          flex-wrap: wrap;
          gap: 16px;
        }
        .brand {
          display: flex;
          align-items: center;
          gap: 10px;
        }
        .brand-icon {
          width: 38px;
          height: 38px;
          border-radius: 12px;
          background: var(--color-ink);
          display: inline-flex;
          align-items: center;
          justify-content: center;
        }
        .brand-name {
          font-family: 'Bricolage Grotesque', sans-serif;
          font-weight: 800;
          font-size: 24px;
          letter-spacing: -0.03em;
          color: var(--color-ink);
        }
        .nav-links {
          display: flex;
          align-items: center;
          gap: 6px;
          flex-wrap: wrap;
        }
        .nav-link {
          padding: 8px 14px;
          border-radius: 999px;
          font-weight: 500;
          font-size: 15px;
          color: var(--color-ink);
          transition: background .15s;
        }
        .nav-link:hover, .nav-link.active {
          background: #E1E8DF;
        }
        .nav-actions {
          display: flex;
          align-items: center;
          gap: 12px;
        }
        .user-pill {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 4px 12px 4px 4px;
          border: 2px solid var(--color-ink);
          border-radius: 999px;
          background: #fff;
          font-size: 14px;
        }

        /* Buttons */
        .btn {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          min-height: 46px;
          padding: 0 22px;
          border-radius: 999px;
          font-weight: 700;
          font-size: 15px;
          border: 2px solid var(--color-ink);
          transition: transform .15s, background .15s, box-shadow .15s;
        }
        .btn:hover {
          transform: translateY(-2px);
        }
        .btn-sm {
          min-height: 36px;
          padding: 0 16px;
          font-size: 13px;
        }
        .btn-md {
          min-height: 44px;
          padding: 0 20px;
          font-size: 15px;
        }
        .btn-lg {
          min-height: 52px;
          padding: 0 28px;
          font-size: 17px;
        }
        .btn-lime { background: var(--color-lime); color: var(--color-ink); }
        .btn-ink { background: var(--color-ink); color: #fff; }
        .btn-ghost { background: #fff; color: var(--color-ink); }
        .btn-outline { background: transparent; color: var(--color-ink); }

        /* Pills and Tags */
        .pill {
          display: inline-flex;
          align-items: center;
          padding: 4px 12px;
          border-radius: 999px;
          font-size: 14px;
          font-weight: 700;
          background: #E1E8DF;
          color: var(--color-ink);
        }
        .pill.lm { background: var(--color-lime); color: var(--color-ink); }
        .pill.dk { background: #1d3329; color: #E8EFE9; border: 1px solid #3b5148; }

        /* Avatars */
        .av {
          width: 38px;
          height: 38px;
          border-radius: 50%;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          font-weight: 800;
          font-size: 15px;
          color: var(--color-ink);
          flex: none;
          border: 2px solid var(--color-ink);
          font-family: 'Bricolage Grotesque', sans-serif;
        }
        .av-c1 { background: var(--color-lime); }
        .av-c2 { background: var(--color-lavender); }
        .av-c3 { background: var(--color-coral); }
        .av-c4 { background: var(--color-sky); }
        .av-c5 { background: var(--color-yellow); }
        .av-c6 { background: #B8F2D0; }

        /* Statuses */
        .st {
          display: inline-flex;
          align-items: center;
          padding: 4px 12px;
          border-radius: 999px;
          font-size: 13px;
          font-weight: 700;
          border: 2px solid var(--color-ink);
        }
        .st.yes { background: var(--color-lime); color: var(--color-ink); }
        .st.wait { background: var(--color-yellow); color: var(--color-ink); }
        .st.no { background: var(--color-coral); color: var(--color-ink); }

        /* Cards */
        .card, .glass-card {
          background: #fff;
          border: 2px solid var(--color-ink);
          border-radius: 24px;
          box-sizing: border-box;
          transition: background .2s, transform .15s;
        }
        .card-lift:hover {
          transform: translateY(-3px);
          box-shadow: 6px 6px 0 var(--color-ink);
        }
        .feat:hover {
          background: var(--color-lime);
        }

        /* Grids */
        .grid-2 {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
          gap: 24px;
        }
        .grid-3 {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
          gap: 24px;
        }

        /* Tabs */
        .tab-bar {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
        }
        .tab {
          min-height: 44px;
          padding: 0 20px;
          border-radius: 999px;
          border: 2px solid #3b5148;
          background: transparent;
          color: #E8EFE9;
          font-weight: 700;
          font-size: 15px;
          transition: border-color .15s, background .15s;
        }
        .tab:hover {
          border-color: var(--color-lime);
        }
        .tab.on {
          background: var(--color-lime);
          color: var(--color-ink);
          border-color: var(--color-lime);
        }

        /* Forms */
        .form-group {
          margin-bottom: 1.25rem;
        }
        .form-label {
          display: block;
          font-size: 0.88rem;
          font-weight: 700;
          color: var(--color-dim);
          margin-bottom: 0.4rem;
        }
        .form-control {
          width: 100%;
          background: #fff;
          border: 2px solid var(--color-ink);
          border-radius: 12px;
          padding: 0.75rem 1rem;
          color: var(--color-ink);
          font-family: inherit;
          font-size: 1rem;
          transition: outline .15s;
        }
        .form-control:focus {
          outline: 3px solid var(--color-blue);
          outline-offset: 1px;
        }

        /* Star Rating Widget */
        .star {
          width: 42px;
          height: 42px;
          border: 0;
          background: transparent;
          padding: 0;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
        }
        .star svg {
          fill: #fff;
          stroke: var(--color-ink);
          stroke-width: 2;
          transition: fill .15s, transform .1s;
        }
        .star.on svg {
          fill: var(--color-lime);
        }
        .star:hover svg {
          fill: var(--color-lime);
          transform: scale(1.15);
        }

        /* Alerts */
        .alert {
          border: 2px solid var(--color-ink);
          border-radius: 18px;
          padding: 1rem 1.25rem;
          margin-bottom: 1.5rem;
          font-weight: 600;
          font-size: 0.95rem;
          display: flex;
          align-items: center;
          gap: 10px;
        }
        .alert-success { background: #DCFCE7; color: var(--color-ink); }
        .alert-danger { background: #FEE2E2; color: var(--color-ink); }

        /* Footer */
        .footer {
          border-top: 2px solid var(--color-ink);
          background: #fff;
          padding: 32px 24px;
        }
        .footer-inner {
          max-width: 1240px;
          margin: 0 auto;
          display: flex;
          justify-content: space-between;
          align-items: center;
          flex-wrap: wrap;
          gap: 16px;
        }

        a:focus-visible, button:focus-visible {
          outline: 3px solid var(--color-blue);
          outline-offset: 3px;
        }
        """;
    }

    // =========================================================================
    // Page: Home / Landing
    // =========================================================================

    public static String renderHome(Member currentUser, int memberCount, int swapOpportunitiesCount, List<Member> featuredMembers) {
        StringBuilder sb = new StringBuilder();

        // Hero Section
        sb.append("<section style=\"padding: 32px 0 64px; display: flex; flex-wrap: wrap; gap: 48px; align-items: center;\">\n");
        sb.append("  <div style=\"flex: 1 1 520px; min-width: 0; display: flex; flex-direction: column; gap: 24px;\">\n");
        sb.append("    <span class=\"pill lm\" style=\"border: 2px solid #0D1F1A; align-self: flex-start;\">No money. Just skills.</span>\n");
        sb.append("    <h1 style=\"font-size: clamp(2.8rem, 5.5vw, 4.8rem); line-height: 0.98; font-weight: 800;\">Trade what you know for what you want to learn.</h1>\n");
        sb.append("    <p style=\"font-size: 1.2rem; line-height: 1.55; color: #33463F; max-width: 560px;\">\n");
        sb.append("      Teach guitar, learn Excel. When direct one-to-one barter doesn't align, SkillSwap's graph engine discovers circular 3-way & 4-way exchanges where everyone teaches one peer and learns from another.\n");
        sb.append("    </p>\n");

        sb.append("    <div style=\"display: flex; flex-wrap: wrap; gap: 14px;\">\n");
        if (currentUser == null) {
            sb.append("      <a href=\"/signup\" class=\"btn btn-ink btn-lg\">Start Swapping Free</a>\n");
            sb.append("      <a href=\"#try\" class=\"btn btn-ghost btn-lg\">See Live Matcher</a>\n");
        } else {
            sb.append("      <a href=\"/swaps\" class=\"btn btn-ink btn-lg\">⚡ Open My Dashboard</a>\n");
            sb.append("      <a href=\"/profile\" class=\"btn btn-ghost btn-lg\">Edit My Skills</a>\n");
        }
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        // Hero Visual Card (Angled with neo-brutalist shadow)
        sb.append("  <div style=\"flex: 1 1 420px; min-width: 0; display: flex; justify-content: center;\">\n");
        sb.append("    <div class=\"card\" style=\"width: 100%; max-width: 480px; padding: 28px; display: flex; flex-direction: column; gap: 20px; box-shadow: 10px 10px 0 #0D1F1A; transform: rotate(1.5deg);\">\n");
        sb.append("      <div style=\"display: flex; justify-content: space-between; align-items: center; gap: 12px;\">\n");
        sb.append("        <h3 style=\"font-size: 22px; font-weight: 800;\">3-way swap loop</h3>\n");
        sb.append("        <span class=\"pill lm\">All learners matched</span>\n");
        sb.append("      </div>\n");
        sb.append("      <div style=\"display: flex; align-items: center; gap: 12px; font-size: 16px; line-height: 1.4;\">\n");
        sb.append("        <span class=\"av av-c1\">1</span><span><b>Peer 1</b> learns <span class=\"pill lm\">Excel</span> from <b>Peer 2</b></span>\n");
        sb.append("      </div>\n");
        sb.append("      <div style=\"display: flex; align-items: center; gap: 12px; font-size: 16px; line-height: 1.4;\">\n");
        sb.append("        <span class=\"av av-c2\">2</span><span><b>Peer 2</b> learns <span class=\"pill lm\">Photography</span> from <b>Peer 3</b></span>\n");
        sb.append("      </div>\n");
        sb.append("      <div style=\"display: flex; align-items: center; gap: 12px; font-size: 16px; line-height: 1.4;\">\n");
        sb.append("        <span class=\"av av-c3\">3</span><span><b>Peer 3</b> learns <span class=\"pill lm\">Guitar</span> from <b>Peer 1</b></span>\n");
        sb.append("      </div>\n");
        sb.append("      <div style=\"display: flex; align-items: center; justify-content: space-between; gap: 12px; padding-top: 16px; border-top: 2px dashed #0D1F1A;\">\n");
        sb.append("        <span style=\"font-size: 14px; color: #4A5B55;\">Zero tuition · Direct barter</span>\n");
        sb.append("        <a href=\"/signup\" class=\"btn btn-ink btn-sm\">Join to Swap</a>\n");
        sb.append("      </div>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");
        sb.append("</section>\n");

        // How it Works (#how)
        sb.append("<section id=\"how\" style=\"padding: 48px 0 64px;\">\n");
        sb.append("  <div style=\"margin-bottom: 32px;\">\n");
        sb.append("    <span class=\"pill lm\" style=\"margin-bottom: 10px;\">Algorithmic Barter</span>\n");
        sb.append("    <h2 style=\"font-size: clamp(2rem, 4vw, 3rem); font-weight: 800;\">How a 3-way swap solves the double coincidence of wants.</h2>\n");
        sb.append("  </div>\n");

        sb.append("  <div class=\"grid-3\">\n");
        sb.append("    <div class=\"card card-lift\" style=\"padding: 28px;\">\n");
        sb.append("      <div style=\"font-size: 2rem; margin-bottom: 12px;\">⚠️</div>\n");
        sb.append("      <h3 style=\"font-size: 20px; font-weight: 800; margin-bottom: 8px;\">1. Direct trade stalls</h3>\n");
        sb.append("      <p style=\"font-size: 15px; color: #33463F;\">When Student A teaches Guitar and wants Excel, but Student B teaches Excel and wants Photography, a direct trade fails because Student B doesn't need Guitar.</p>\n");
        sb.append("    </div>\n");

        sb.append("    <div class=\"card card-lift\" style=\"padding: 28px;\">\n");
        sb.append("      <div style=\"font-size: 2rem; margin-bottom: 12px;\">🔄</div>\n");
        sb.append("      <h3 style=\"font-size: 20px; font-weight: 800; margin-bottom: 8px;\">2. Circle discovered</h3>\n");
        sb.append("      <p style=\"font-size: 15px; color: #33463F;\">SkillSwap's graph engine discovers Student C: teaching Photography to Student B and wanting to learn Guitar from Student A. The circular loop is complete!</p>\n");
        sb.append("    </div>\n");

        sb.append("    <div class=\"card card-lift\" style=\"padding: 28px;\">\n");
        sb.append("      <div style=\"font-size: 2rem; margin-bottom: 12px;\">✨</div>\n");
        sb.append("      <h3 style=\"font-size: 20px; font-weight: 800; margin-bottom: 8px;\">3. Everyone learns</h3>\n");
        sb.append("      <p style=\"font-size: 15px; color: #33463F;\">All participants accept the trade proposal. Contact info is unlocked, barter sessions are scheduled, and peer ratings build trust.</p>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");
        sb.append("</section>\n");

        // Why It's Fair (#why)
        sb.append("<section id=\"why\" style=\"padding: 64px 0;\">\n");
        sb.append("  <h2 style=\"font-size: clamp(2rem, 4vw, 3.2rem); font-weight: 800; max-width: 800px; margin-bottom: 32px;\">Built so nobody gets left holding the bag.</h2>\n");
        sb.append("  <div class=\"grid-3\">\n");
        sb.append("    <div class=\"card feat\" style=\"padding: 28px;\">\n");
        sb.append("      <h3 style=\"font-size: 22px; font-weight: 800; margin-bottom: 8px;\">Level-aware</h3>\n");
        sb.append("      <p style=\"font-size: 15px; line-height: 1.55;\">Pedagogical integrity: a beginner is only matched with mentors at intermediate or advanced skill levels.</p>\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"card feat\" style=\"padding: 28px;\">\n");
        sb.append("      <h3 style=\"font-size: 22px; font-weight: 800; margin-bottom: 8px;\">All or nothing</h3>\n");
        sb.append("      <p style=\"font-size: 15px; line-height: 1.55;\">A circle only activates when every participant accepts. Any single decline cancels the proposal and unlocks everyone.</p>\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"card feat\" style=\"padding: 28px;\">\n");
        sb.append("      <h3 style=\"font-size: 22px; font-weight: 800; margin-bottom: 8px;\">Private until it's real</h3>\n");
        sb.append("      <p style=\"font-size: 15px; line-height: 1.55;\">Contact details and schedules stay strictly confidential until 100% of cycle members confirm.</p>\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"card feat\" style=\"padding: 28px;\">\n");
        sb.append("      <h3 style=\"font-size: 22px; font-weight: 800; margin-bottom: 8px;\">Peer rated</h3>\n");
        sb.append("      <p style=\"font-size: 15px; line-height: 1.55;\">Following each swap lesson, members review their teachers with 1-5 stars. High ratings boost match ranking.</p>\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"card feat\" style=\"padding: 28px;\">\n");
        sb.append("      <h3 style=\"font-size: 22px; font-weight: 800; margin-bottom: 8px;\">Concurrency Lock</h3>\n");
        sb.append("      <p style=\"font-size: 15px; line-height: 1.55;\">Participants in an active trade are locked to prevent overcommitting or double-booking barter slots.</p>\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"card feat\" style=\"padding: 28px;\">\n");
        sb.append("      <h3 style=\"font-size: 22px; font-weight: 800; margin-bottom: 8px;\">Almost-Matches</h3>\n");
        sb.append("      <p style=\"font-size: 15px; line-height: 1.55;\">Smart suggestions detect skills that, if added to your desired list, will instantly unlock new barter exchanges.</p>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");
        sb.append("</section>\n");

        // Bottom CTA Banner
        sb.append("<section style=\"padding: 32px 0 64px;\">\n");
        sb.append("  <div class=\"card\" style=\"background: var(--color-lime); padding: 56px 32px; display: flex; flex-direction: column; align-items: center; gap: 20px; text-align: center; box-shadow: 8px 8px 0 #0D1F1A;\">\n");
        sb.append("    <h2 style=\"font-size: clamp(2rem, 4vw, 3.5rem); font-weight: 800; max-width: 680px;\">Your next skill is one swap away.</h2>\n");
        sb.append("    <p style=\"max-width: 500px; font-size: 17px; color: #1D3329;\">Trade coding, music, design, academics, and languages with fellow students without spending a dime.</p>\n");
        sb.append("    <a href=\"/signup\" class=\"btn btn-ink btn-lg\">Create Your Profile</a>\n");
        sb.append("  </div>\n");
        sb.append("</section>\n");

        return sb.toString();
    }

    private static String renderInteractiveMatcherSection() {
        return "";
    }
/*


        <script>
        const studentData = {
          'Asha': {
            teaches: ['Guitar', 'Figma'],
            wants: ['Excel', 'Python'],
            cycles: [
              {
                rank: 1,
                label: '3-way swap',
                badgeCls: 'lm',
                members: [
                  { name: 'Asha', initial: 'A', av: 'av-Asha' },
                  { name: 'Ravi', initial: 'R', av: 'av-Ravi' },
                  { name: 'Meena', initial: 'M', av: 'av-Meena' }
                ],
                links: [
                  { learner: 'Asha', skill: 'Excel', teacher: 'Ravi' },
                  { learner: 'Ravi', skill: 'Photography', teacher: 'Meena' },
                  { learner: 'Meena', skill: 'Guitar', teacher: 'Asha' }
                ]
              },
              {
                rank: 2,
                label: 'Direct swap',
                badgeCls: 'lm',
                members: [
                  { name: 'Asha', initial: 'A', av: 'av-Asha' },
                  { name: 'Kabir', initial: 'K', av: 'av-Kabir' }
                ],
                links: [
                  { learner: 'Asha', skill: 'Python', teacher: 'Kabir' },
                  { learner: 'Kabir', skill: 'Figma', teacher: 'Asha' }
                ]
              }
            ]
          },
          'Ravi': {
            teaches: ['Excel', 'Data Analysis'],
            wants: ['Photography', 'Guitar'],
            cycles: [
              {
                rank: 1,
                label: '3-way swap',
                badgeCls: 'lm',
                members: [
                  { name: 'Ravi', initial: 'R', av: 'av-Ravi' },
                  { name: 'Meena', initial: 'M', av: 'av-Meena' },
                  { name: 'Asha', initial: 'A', av: 'av-Asha' }
                ],
                links: [
                  { learner: 'Ravi', skill: 'Photography', teacher: 'Meena' },
                  { learner: 'Meena', skill: 'Guitar', teacher: 'Asha' },
                  { learner: 'Asha', skill: 'Excel', teacher: 'Ravi' }
                ]
              }
            ]
          },
          'Meena': {
            teaches: ['Photography', 'French'],
            wants: ['Guitar', 'Chess'],
            cycles: [
              {
                rank: 1,
                label: '3-way swap',
                badgeCls: 'lm',
                members: [
                  { name: 'Meena', initial: 'M', av: 'av-Meena' },
                  { name: 'Asha', initial: 'A', av: 'av-Asha' },
                  { name: 'Ravi', initial: 'R', av: 'av-Ravi' }
                ],
                links: [
                  { learner: 'Meena', skill: 'Guitar', teacher: 'Asha' },
                  { learner: 'Asha', skill: 'Excel', teacher: 'Ravi' },
                  { learner: 'Ravi', skill: 'Photography', teacher: 'Meena' }
                ]
              }
            ]
          },
          'Kabir': {
            teaches: ['Python', 'Chess'],
            wants: ['Figma', 'French'],
            cycles: [
              {
                rank: 1,
                label: 'Direct swap',
                badgeCls: 'lm',
                members: [
                  { name: 'Kabir', initial: 'K', av: 'av-Kabir' },
                  { name: 'Asha', initial: 'A', av: 'av-Asha' }
                ],
                links: [
                  { learner: 'Kabir', skill: 'Figma', teacher: 'Asha' },
                  { learner: 'Asha', skill: 'Python', teacher: 'Kabir' }
                ]
              }
            ]
          },
          'Neha': {
            teaches: ['Calculus', 'French'],
            wants: ['Photography', 'Python'],
            cycles: [
              {
                rank: 1,
                label: '3-way swap',
                badgeCls: 'lm',
                members: [
                  { name: 'Neha', initial: 'N', av: 'av-Neha' },
                  { name: 'Meena', initial: 'M', av: 'av-Meena' },
                  { name: 'Kabir', initial: 'K', av: 'av-Kabir' }
                ],
                links: [
                  { learner: 'Neha', skill: 'Photography', teacher: 'Meena' },
                  { learner: 'Meena', skill: 'French', teacher: 'Neha' },
                  { learner: 'Kabir', skill: 'French', teacher: 'Neha' }
                ]
              }
            ]
          }
        };

        function selectStudent(name) {
          const data = studentData[name];
          if (!data) return;

          // update tabs
          document.querySelectorAll('#studentTabs .tab').forEach(b => {
            b.classList.toggle('on', b.textContent.trim() === name);
          });

          // update skill pills
          document.getElementById('teachSkills').innerHTML = data.teaches.map(s => `<span class="pill lm">${s}</span>`).join('');
          document.getElementById('wantSkills').innerHTML = data.wants.map(s => `<span class="pill" style="background:#fff;color:#0D1F1A;">${s}</span>`).join('');
          document.getElementById('loginStudentBtn').textContent = 'Log in as ' + name;

          // update cycle cards
          const container = document.getElementById('cycleContainer');
          if (data.cycles.length === 0) {
            container.innerHTML = '<p style="color:#B9C8BF;font-size:16px;">No cycle currently open. Add another desired skill to unlock fresh matches.</p>';
            return;
          }

          container.innerHTML = data.cycles.map(c => {
            const memberRing = c.members.map((m, idx) => `
              <span style="display:inline-flex;align-items:center;gap:6px;">
                <span class="av ${m.av}">${m.initial}</span>
                <b style="font-size:15px;color:#0D1F1A;">${m.name}</b>
                ${idx < c.members.length - 1 ? `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#3A3FD8" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"></path><path d="M13 6l6 6-6 6"></path></svg>` : ''}
              </span>
            `).join('') + (c.members.length > 2 ? `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#3A3FD8" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round"><path d="M20 12a8 8 0 1 1-3-6.2"></path><path d="M20 4v5h-5"></path></svg>` : '');

            const linkList = c.links.map(l => `
              <div style="font-size:14px;color:#33463F;">
                <b>${l.learner}</b> learns <span class="pill lm" style="padding:2px 8px;font-size:12px;">${l.skill}</span> from <b>${l.teacher}</b>
              </div>
            `).join('');

            return `
              <div class="card card-lift" style="padding:24px;display:flex;flex-direction:column;gap:16px;background:#fff;border:2px solid #0D1F1A;">
                <div style="display:flex;justify-content:space-between;align-items:center;">
                  <span class="pill ${c.badgeCls}">${c.label}</span>
                  <span style="font-size:13px;font-weight:700;color:#4A5B55;">Rank #${c.rank}</span>
                </div>
                <div style="display:flex;flex-wrap:wrap;align-items:center;gap:6px;">
                  ${memberRing}
                </div>
                <div style="display:flex;flex-direction:column;gap:8px;">
                  ${linkList}
                </div>
                <div style="margin-top:auto;padding-top:12px;border-top:1px dashed #0D1F1A;">
                  <a href="/login" class="btn btn-ink btn-sm" style="width:100%;">Propose this swap</a>
                </div>
              </div>
            `;
          }).join('');
        }

        */


    // =========================================================================
    // Page: Sign Up & Log In
    // =========================================================================

    public static String renderSignup() {
        return """
        <div style="max-width: 480px; margin: 3rem auto;">
          <div class="card" style="padding: 36px; box-shadow: 8px 8px 0 #0D1F1A;">
            <div style="margin-bottom: 24px;">
              <span class="pill lm" style="margin-bottom: 8px;">Zero-Money Marketplace</span>
              <h2 style="font-size: 2rem; font-weight: 800;">Create Account</h2>
              <p style="color: #33463F; font-size: 0.95rem; margin-top: 4px;">Join the student peer skill barter network</p>
            </div>

            <form action="/signup" method="POST">
              <div class="form-group">
                <label class="form-label" for="name">Your Name</label>
                <input type="text" id="name" name="name" class="form-control" placeholder="e.g. Maya" required autofocus>
              </div>

              <div class="form-group">
                <label class="form-label" for="pin">Create 4-Digit PIN</label>
                <input type="password" id="pin" name="pin" class="form-control" placeholder="••••" maxlength="8" required>
              </div>

              <div class="form-group">
                <label class="form-label" for="contact">Contact Info (Kept hidden until all accept)</label>
                <input type="text" id="contact" name="contact" class="form-control" placeholder="e.g. maya@campus.edu or @maya_social" required>
              </div>

              <div class="form-group">
                <label class="form-label" for="bio">Short Bio & Interests</label>
                <input type="text" id="bio" name="bio" class="form-control" placeholder="e.g. Sophomore passionate about Machine Learning and Ukulele">
              </div>

              <button type="submit" class="btn btn-lime btn-lg" style="width: 100%; margin-top: 0.5rem;">Join Free & Start Swapping</button>
            </form>

            <div style="text-align: center; margin-top: 1.5rem; font-size: 0.95rem; color: #4A5B55;">
              Already have an account? <a href="/login" style="color: var(--color-blue); font-weight: 700;">Log In</a>
            </div>
          </div>
        </div>
        """;
    }

    public static String renderLogin() {
        return """
        <div style="max-width: 440px; margin: 3rem auto;">
          <div class="card" style="padding: 36px; box-shadow: 8px 8px 0 #0D1F1A;">
            <div style="margin-bottom: 24px;">
              <span class="pill lm" style="margin-bottom: 8px;">Member Access</span>
              <h2 style="font-size: 2rem; font-weight: 800;">Welcome Back</h2>
              <p style="color: #33463F; font-size: 0.95rem; margin-top: 4px;">Enter your student name and PIN</p>
            </div>

            <form action="/login" method="POST">
              <div class="form-group">
                <label class="form-label" for="name">Student Name</label>
                <input type="text" id="name" name="name" class="form-control" placeholder="Enter your name" required autofocus>
              </div>

              <div class="form-group">
                <label class="form-label" for="pin">PIN</label>
                <input type="password" id="pin" name="pin" class="form-control" placeholder="••••" required>
              </div>

              <button type="submit" class="btn btn-ink btn-lg" style="width: 100%; margin-top: 0.5rem;">Log In</button>
            </form>

            <div style="text-align: center; margin-top: 1.5rem; font-size: 0.95rem; color: #4A5B55;">
              New to SkillSwap? <a href="/signup" style="color: var(--color-blue); font-weight: 700;">Join Free</a>
            </div>
          </div>
        </div>
        """;
    }

    // =========================================================================
    // Page: Profile & Skill Management
    // =========================================================================

    public static String renderProfile(Member member) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;\">\n");
        sb.append("  <div>\n");
        sb.append("    <h1 style=\"font-size: 2.4rem; font-weight: 800;\">My Profile & Skills</h1>\n");
        sb.append("    <p style=\"color: #33463F; font-size: 1.05rem;\">Manage what you teach, what you want to learn, and your weekly barter hours.</p>\n");
        sb.append("  </div>\n");

        if (member.isLocked()) {
            sb.append("  <span class=\"st wait\" style=\"font-size: 0.9rem;\">\n");
            sb.append("    🔒 Locked in Active Proposal (").append(SecurityUtil.escapeHtml(member.getLockedInProposalId())).append(")\n");
            sb.append("  </span>\n");
        } else {
            sb.append("  <a href=\"/swaps\" class=\"btn btn-lime btn-md\">⚡ View Matched Swaps</a>\n");
        }
        sb.append("</div>\n");

        // Profile Overview Card & Availability
        sb.append("<div class=\"grid-2\" style=\"margin-bottom: 2rem;\">\n");
        sb.append("  <div class=\"card\" style=\"padding: 28px;\">\n");
        String avClass = getAvatarClass(member.getName());
        String initial = member.getName().substring(0, 1).toUpperCase();
        sb.append("    <div style=\"display:flex;align-items:center;gap:14px;margin-bottom:14px;\">\n");
        sb.append("      <span class=\"av ").append(avClass).append("\" style=\"width:48px;height:48px;font-size:18px;\">").append(initial).append("</span>\n");
        sb.append("      <div>\n");
        sb.append("        <h3 style=\"font-size: 1.4rem; font-weight: 800;\">").append(SecurityUtil.escapeHtml(member.getName())).append("</h3>\n");
        sb.append("        <span class=\"pill lm\">⭐ ").append(member.getAverageRating()).append(" / 5.0 (").append(member.getRatingCount()).append(" reviews)</span>\n");
        sb.append("      </div>\n");
        sb.append("    </div>\n");

        if (member.getBio() != null && !member.getBio().isEmpty()) {
            sb.append("    <p style=\"color: #33463F; font-size: 0.95rem; margin-bottom: 12px;\">").append(SecurityUtil.escapeHtml(member.getBio())).append("</p>\n");
        }
        sb.append("    <div style=\"font-size: 0.9rem; color: #4A5B55;\">🔒 Private Contact: <b>").append(SecurityUtil.escapeHtml(member.getContact())).append("</b> (Revealed only to mutual swap partners)</div>\n");
        sb.append("  </div>\n");

        // Availability Settings
        sb.append("  <div class=\"card\" style=\"padding: 28px;\">\n");
        sb.append("    <h3 style=\"margin-bottom: 0.75rem;\">⏰ Availability & Barter Commitment</h3>\n");
        sb.append("    <form action=\"/profile/availability\" method=\"POST\" style=\"display: flex; gap: 1rem; align-items: flex-end; flex-wrap: wrap;\">\n");
        sb.append("      <div class=\"form-group\" style=\"flex: 1; min-width: 140px; margin-bottom: 0;\">\n");
        sb.append("        <label class=\"form-label\" for=\"hours\">Hours Free Per Week</label>\n");
        sb.append("        <input type=\"number\" id=\"hours\" name=\"hours\" class=\"form-control\" min=\"1\" max=\"40\" value=\"").append(member.getWeeklyAvailabilityHours()).append("\" required>\n");
        sb.append("      </div>\n");
        sb.append("      <button type=\"submit\" class=\"btn btn-ink btn-md\" style=\"min-height: 44px;\">Update Hours</button>\n");
        sb.append("    </form>\n");
        sb.append("    <p style=\"font-size: 0.85rem; color: #4A5B55; margin-top: 10px;\">Matching algorithm pairs students with compatible weekly time commitments.</p>\n");
        sb.append("  </div>\n");
        sb.append("</div>\n");

        // Skills Management Section: Teaches vs Wants
        sb.append("<div class=\"grid-2\" style=\"margin-bottom: 2rem;\">\n");

        // Teaches Card
        sb.append("  <div class=\"card\" style=\"padding: 28px;\">\n");
        sb.append("    <div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;\">\n");
        sb.append("      <h3 style=\"font-size: 1.3rem; font-weight: 800;\">🎓 Skills I Teach (").append(member.getTeaches().size()).append(")</h3>\n");
        sb.append("      <span class=\"pill lm\">Mentorship</span>\n");
        sb.append("    </div>\n");

        if (member.getTeaches().isEmpty()) {
            sb.append("    <p style=\"color: #4A5B55; font-size: 0.9rem; margin-bottom: 1.5rem;\">You haven't listed any skills you can teach. Add at least one to unlock trades!</p>\n");
        } else {
            sb.append("    <div style=\"display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 1.5rem;\">\n");
            for (Skill s : member.getTeaches()) {
                sb.append("      <div style=\"display: flex; justify-content: space-between; align-items: center; background: #F1F4EF; border: 1px solid #0D1F1A; border-radius: 12px; padding: 0.6rem 1rem;\">\n");
                sb.append("        <div>\n");
                sb.append("          <strong style=\"font-size: 1rem;\">").append(SecurityUtil.escapeHtml(s.getName())).append("</strong>\n");
                sb.append("          <span style=\"font-size: 0.8rem; color: #33463F; margin-left: 0.5rem;\">· ").append(s.getCategory().getDisplayName()).append(" · <b>").append(s.getLevel().getDisplayName()).append("</b></span>\n");
                sb.append("        </div>\n");
                sb.append("        <form action=\"/profile/skill/delete\" method=\"POST\" style=\"display: inline;\">\n");
                sb.append("          <input type=\"hidden\" name=\"name\" value=\"").append(SecurityUtil.escapeHtml(s.getName())).append("\">\n");
                sb.append("          <input type=\"hidden\" name=\"type\" value=\"teaches\">\n");
                sb.append("          <button type=\"submit\" class=\"btn btn-ghost btn-sm\" style=\"min-height:30px;padding:0 10px;font-size:12px;\" onclick=\"return confirm('Remove skill?');\">✕ Remove</button>\n");
                sb.append("        </form>\n");
                sb.append("      </div>\n");
            }
            sb.append("    </div>\n");
        }

        // Add Teach Skill Form
        sb.append("    <div style=\"border-top: 2px dashed #0D1F1A; padding-top: 1.25rem;\">\n");
        sb.append("      <h4 style=\"font-size: 1rem; font-weight: 700; margin-bottom: 0.75rem;\">+ Add a Skill You Can Teach</h4>\n");
        sb.append("      <form action=\"/profile/skill/add\" method=\"POST\">\n");
        sb.append("        <input type=\"hidden\" name=\"type\" value=\"teaches\">\n");
        sb.append("        <div class=\"form-group\">\n");
        sb.append("          <input type=\"text\" name=\"name\" class=\"form-control\" placeholder=\"e.g. Guitar, Python, UI Design\" required>\n");
        sb.append("        </div>\n");
        sb.append("        <div style=\"display: flex; gap: 0.75rem; margin-bottom: 1rem;\">\n");
        sb.append("          <select name=\"category\" class=\"form-control\" style=\"flex: 1;\">\n");
        for (SkillCategory cat : SkillCategory.values()) {
            sb.append("            <option value=\"").append(cat.name()).append("\">").append(cat.getDisplayName()).append("</option>\n");
        }
        sb.append("          </select>\n");
        sb.append("          <select name=\"level\" class=\"form-control\" style=\"flex: 1;\">\n");
        sb.append("            <option value=\"INTERMEDIATE\">Intermediate</option>\n");
        sb.append("            <option value=\"ADVANCED\" selected>Advanced</option>\n");
        sb.append("            <option value=\"BEGINNER\">Beginner</option>\n");
        sb.append("          </select>\n");
        sb.append("        </div>\n");
        sb.append("        <button type=\"submit\" class=\"btn btn-ink btn-sm\" style=\"width: 100%;\">Add Taught Skill</button>\n");
        sb.append("      </form>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        // Wants Card
        sb.append("  <div class=\"card\" style=\"padding: 28px;\">\n");
        sb.append("    <div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;\">\n");
        sb.append("      <h3 style=\"font-size: 1.3rem; font-weight: 800;\">🎯 Skills I Want to Learn (").append(member.getWants().size()).append(")</h3>\n");
        sb.append("      <span class=\"pill\">Learning Goals</span>\n");
        sb.append("    </div>\n");

        if (member.getWants().isEmpty()) {
            sb.append("    <p style=\"color: #4A5B55; font-size: 0.9rem; margin-bottom: 1.5rem;\">You haven't listed any skills you want to learn. Add skills to match with peers!</p>\n");
        } else {
            sb.append("    <div style=\"display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 1.5rem;\">\n");
            for (Skill s : member.getWants()) {
                sb.append("      <div style=\"display: flex; justify-content: space-between; align-items: center; background: #F1F4EF; border: 1px solid #0D1F1A; border-radius: 12px; padding: 0.6rem 1rem;\">\n");
                sb.append("        <div>\n");
                sb.append("          <strong style=\"font-size: 1rem;\">").append(SecurityUtil.escapeHtml(s.getName())).append("</strong>\n");
                sb.append("          <span style=\"font-size: 0.8rem; color: #33463F; margin-left: 0.5rem;\">· ").append(s.getCategory().getDisplayName()).append(" · <b>").append(s.getLevel().getDisplayName()).append("</b></span>\n");
                sb.append("        </div>\n");
                sb.append("        <form action=\"/profile/skill/delete\" method=\"POST\" style=\"display: inline;\">\n");
                sb.append("          <input type=\"hidden\" name=\"name\" value=\"").append(SecurityUtil.escapeHtml(s.getName())).append("\">\n");
                sb.append("          <input type=\"hidden\" name=\"type\" value=\"wants\">\n");
                sb.append("          <button type=\"submit\" class=\"btn btn-ghost btn-sm\" style=\"min-height:30px;padding:0 10px;font-size:12px;\" onclick=\"return confirm('Remove skill?');\">✕ Remove</button>\n");
                sb.append("        </form>\n");
                sb.append("      </div>\n");
            }
            sb.append("    </div>\n");
        }

        // Add Want Skill Form
        sb.append("    <div style=\"border-top: 2px dashed #0D1F1A; padding-top: 1.25rem;\">\n");
        sb.append("      <h4 style=\"font-size: 1rem; font-weight: 700; margin-bottom: 0.75rem;\">+ Add a Skill You Want to Learn</h4>\n");
        sb.append("      <form action=\"/profile/skill/add\" method=\"POST\">\n");
        sb.append("        <input type=\"hidden\" name=\"type\" value=\"wants\">\n");
        sb.append("        <div class=\"form-group\">\n");
        sb.append("          <input type=\"text\" name=\"name\" class=\"form-control\" placeholder=\"e.g. Excel, Photography, French\" required>\n");
        sb.append("        </div>\n");
        sb.append("        <div style=\"display: flex; gap: 0.75rem; margin-bottom: 1rem;\">\n");
        sb.append("          <select name=\"category\" class=\"form-control\" style=\"flex: 1;\">\n");
        for (SkillCategory cat : SkillCategory.values()) {
            sb.append("            <option value=\"").append(cat.name()).append("\">").append(cat.getDisplayName()).append("</option>\n");
        }
        sb.append("          </select>\n");
        sb.append("          <select name=\"level\" class=\"form-control\" style=\"flex: 1;\">\n");
        sb.append("            <option value=\"BEGINNER\" selected>Beginner (Need Intermediate/Adv Mentor)</option>\n");
        sb.append("            <option value=\"INTERMEDIATE\">Intermediate</option>\n");
        sb.append("            <option value=\"ADVANCED\">Advanced</option>\n");
        sb.append("          </select>\n");
        sb.append("        </div>\n");
        sb.append("        <button type=\"submit\" class=\"btn btn-lime btn-sm\" style=\"width: 100%;\">Add Learning Goal</button>\n");
        sb.append("      </form>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        sb.append("</div>\n");

        return sb.toString();
    }

    // =========================================================================
    // Page: Swaps Discovery & Member Dashboard
    // =========================================================================

    public static String renderSwaps(Member currentUser, List<SwapCycle> discoveredCycles, List<AlmostMatchFinder.Opportunity> almostMatches, int maxLen) {
        StringBuilder sb = new StringBuilder();

        // Dashboard Header
        sb.append("<div style=\"display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;\">\n");
        sb.append("  <div>\n");
        sb.append("    <h1 style=\"font-size: 2.6rem; font-weight: 800; line-height: 1.05;\">Hi ").append(SecurityUtil.escapeHtml(currentUser.getName())).append("</h1>\n");
        sb.append("    <p style=\"color: #33463F; font-size: 1.1rem; margin-top: 4px;\">\n");
        if (currentUser.isLocked()) {
            sb.append("You are currently committed to an active swap proposal (#").append(SecurityUtil.escapeHtml(currentUser.getLockedInProposalId())).append(").");
        } else if (discoveredCycles.isEmpty()) {
            sb.append("You have no active matches right now. Add more skills to unlock new circles.");
        } else {
            sb.append("You have <b>").append(discoveredCycles.size()).append("</b> barter exchange opportunity ready for review.");
        }
        sb.append("    </p>\n");
        sb.append("  </div>\n");

        // Depth Filter Pills
        sb.append("  <div style=\"display: flex; align-items: center; gap: 6px; background: #fff; padding: 6px; border-radius: 999px; border: 2px solid #0D1F1A;\">\n");
        sb.append("    <span style=\"font-size: 13px; font-weight: 700; color: #4A5B55; margin: 0 8px;\">Depth:</span>\n");
        for (int l = 2; l <= 4; l++) {
            boolean active = l == maxLen;
            sb.append("    <a href=\"/swaps?maxLen=").append(l).append("\" class=\"btn btn-sm ").append(active ? "btn-lime" : "btn-ghost").append("\" style=\"min-height:32px;padding:0 12px;\">").append(l).append("-Way</a>\n");
        }
        sb.append("  </div>\n");
        sb.append("</div>\n");

        if (currentUser.isLocked()) {
            sb.append("<div class=\"alert alert-danger\">\n");
            sb.append("  <span>🔒 Concurrency Lock Active: You are committed to an active swap proposal. Contacts and schedule are viewable in the Proposals tab.</span>\n");
            sb.append("</div>\n");
        }

        // Section: Discovered Swap Cycles
        if (discoveredCycles.isEmpty()) {
            sb.append("<div class=\"card\" style=\"text-align: center; padding: 48px 24px; margin-bottom: 2.5rem;\">\n");
            sb.append("  <div style=\"font-size: 3rem; margin-bottom: 1rem;\">🔍</div>\n");
            sb.append("  <h3 style=\"font-size: 1.5rem; font-weight: 800; margin-bottom: 0.5rem;\">No Swap Cycles Found Right Now</h3>\n");
            sb.append("  <p style=\"color: #4A5B55; max-width: 500px; margin: 0 auto 1.5rem auto;\">Try adding more skills to what you want to learn or teach, or see the almost-matches below!</p>\n");
            sb.append("  <a href=\"/profile\" class=\"btn btn-lime btn-md\">Add Skills to Profile</a>\n");
            sb.append("</div>\n");
        } else {
            sb.append("<div style=\"margin-bottom: 1.5rem; display: flex; justify-content: space-between; align-items: center;\">\n");
            sb.append("  <h2 style=\"font-size: 1.4rem; font-weight: 800;\">Discovered Matches (").append(discoveredCycles.size()).append(")</h2>\n");
            sb.append("  <span style=\"font-size: 0.85rem; color: #4A5B55;\">Ranked by Composite Heuristics (Cycle Length, Peer Ratings & Availability)</span>\n");
            sb.append("</div>\n");

            for (int i = 0; i < discoveredCycles.size(); i++) {
                SwapCycle cycle = discoveredCycles.get(i);
                sb.append(renderSwapCycleCard(cycle, currentUser, i + 1));
            }
        }

        // Section: Dashboard Split (My Skills & Other Opportunities)
        sb.append("<div class=\"grid-2\" style=\"margin-top: 2rem;\">\n");

        // My Skills Card
        sb.append("  <div class=\"card\" style=\"padding: 28px; display: flex; flex-direction: column; gap: 18px;\">\n");
        sb.append("    <h3 style=\"font-size: 22px; font-weight: 800;\">My Skills Overview</h3>\n");
        sb.append("    <div>\n");
        sb.append("      <span style=\"font-size: 12px; font-weight: 700; color: #4A5B55; display: block; margin-bottom: 6px;\">I CAN TEACH</span>\n");
        sb.append("      <div style=\"display: flex; flex-wrap: wrap; gap: 6px;\">\n");
        for (Skill s : currentUser.getTeaches()) {
            sb.append("        <span class=\"pill lm\">").append(SecurityUtil.escapeHtml(s.getName())).append(" · ").append(s.getLevel().getDisplayName()).append("</span>\n");
        }
        sb.append("      </div>\n");
        sb.append("    </div>\n");

        sb.append("    <div>\n");
        sb.append("      <span style=\"font-size: 12px; font-weight: 700; color: #4A5B55; display: block; margin-bottom: 6px;\">I WANT TO LEARN</span>\n");
        sb.append("      <div style=\"display: flex; flex-wrap: wrap; gap: 6px;\">\n");
        for (Skill s : currentUser.getWants()) {
            sb.append("        <span class=\"pill\">").append(SecurityUtil.escapeHtml(s.getName())).append(" · ").append(s.getLevel().getDisplayName()).append("</span>\n");
        }
        sb.append("      </div>\n");
        sb.append("    </div>\n");

        sb.append("    <div>\n");
        sb.append("      <span style=\"font-size: 12px; font-weight: 700; color: #4A5B55; display: block; margin-bottom: 4px;\">AVAILABLE HOURS</span>\n");
        sb.append("      <span style=\"font-weight: 700;\">").append(currentUser.getWeeklyAvailabilityHours()).append(" hours / week</span>\n");
        sb.append("    </div>\n");

        sb.append("    <div style=\"margin-top: auto; padding-top: 12px;\">\n");
        sb.append("      <a href=\"/profile\" class=\"btn btn-outline btn-sm\">Manage Skills in Profile</a>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        // Rate Last Teacher Card
        sb.append("  <div class=\"card\" style=\"padding: 28px; display: flex; flex-direction: column; gap: 16px;\">\n");
        sb.append("    <h3 style=\"font-size: 22px; font-weight: 800;\">Rate Your Peer Lessons</h3>\n");
        sb.append("    <p style=\"color: #33463F; font-size: 14px;\">Completed a learning exchange? Submit feedback and star ratings to enhance peer reputation across the campus.</p>\n");

        sb.append("    <form action=\"/review\" method=\"POST\" style=\"display: flex; flex-direction: column; gap: 12px;\">\n");
        sb.append("      <div class=\"form-group\" style=\"margin-bottom:0;\">\n");
        sb.append("        <label class=\"form-label\" for=\"teacherName\">Teacher Name</label>\n");
        sb.append("        <input type=\"text\" id=\"teacherName\" name=\"teacherName\" class=\"form-control\" placeholder=\"Enter peer name\" required>\n");
        sb.append("      </div>\n");

        sb.append("      <div style=\"display: flex; align-items: center; justify-content: space-between;\">\n");
        sb.append("        <span class=\"form-label\" style=\"margin-bottom:0;\">Rating (1 to 5 Stars):</span>\n");
        sb.append("        <div style=\"display:flex;\" id=\"starRating\">\n");
        for (int star = 1; star <= 5; star++) {
            sb.append("          <label style=\"cursor:pointer;\">\n");
            sb.append("            <input type=\"radio\" name=\"rating\" value=\"").append(star).append("\" style=\"display:none;\" ").append(star == 5 ? "checked" : "").append(">\n");
            sb.append("            <span class=\"star on\" style=\"width:32px;height:32px;\">\n");
            sb.append("              <svg width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"><path d=\"M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z\" stroke-linejoin=\"round\"></path></svg>\n");
            sb.append("            </span>\n");
            sb.append("          </label>\n");
        }
        sb.append("        </div>\n");
        sb.append("      </div>\n");

        sb.append("      <div class=\"form-group\" style=\"margin-bottom:0;\">\n");
        sb.append("        <input type=\"text\" name=\"comment\" class=\"form-control\" placeholder=\"Short review (e.g. Super clear explanations!)\" required>\n");
        sb.append("      </div>\n");

        sb.append("      <button type=\"submit\" class=\"btn btn-lime btn-sm\" style=\"margin-top: 4px;\">Submit Peer Review</button>\n");
        sb.append("    </form>\n");
        sb.append("  </div>\n");

        sb.append("</div>\n");

        // Section: Almost-Matches
        if (almostMatches != null && !almostMatches.isEmpty()) {
            sb.append("<div style=\"margin-top: 3.5rem;\">\n");
            sb.append("  <div style=\"display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem;\">\n");
            sb.append("    <h2 style=\"font-size: 1.4rem; font-weight: 800; color: #0D1F1A;\">✨ Almost-Matches & Unlockable Swaps</h2>\n");
            sb.append("    <span class=\"pill lm\">Smart Insights</span>\n");
            sb.append("  </div>\n");
            sb.append("  <p style=\"color: #4A5B55; font-size: 0.95rem; margin-bottom: 1.5rem;\">Adding any of these in-demand skills to your desired list will instantly unlock new barter cycles:</p>\n");

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

        sb.append("<div class=\"card card-lift\" style=\"padding: 28px; margin-bottom: 24px;\">\n");
        sb.append("  <div style=\"display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; border-bottom: 2px dashed #0D1F1A; padding-bottom: 14px; margin-bottom: 16px;\">\n");
        sb.append("    <div style=\"display: flex; align-items: center; gap: 10px;\">\n");
        sb.append("      <span style=\"font-size: 1.3rem; font-weight: 800; color: #0D1F1A;\">#").append(rank).append("</span>\n");
        sb.append("      <h3 style=\"font-size: 1.3rem; font-weight: 800;\">").append(size).append("-Way Barter Exchange</h3>\n");
        sb.append("      <span class=\"pill lm\">").append(size == 2 ? "Direct 2-Way" : size + "-Way Cyclic").append("</span>\n");
        sb.append("    </div>\n");

        sb.append("    <div style=\"display: flex; align-items: center; gap: 1rem;\">\n");
        sb.append("      <div style=\"text-align: right;\">\n");
        sb.append("        <div style=\"font-size: 0.78rem; font-weight: 700; color: #4A5B55;\">Match Score</div>\n");
        sb.append("        <strong style=\"color: #0D1F1A; font-size: 1.25rem;\">").append(String.format("%.1f%%", cycle.getScore() * 100)).append("</strong>\n");
        sb.append("      </div>\n");

        if (!currentUser.isLocked()) {
            sb.append("      <form action=\"/proposals\" method=\"POST\" style=\"margin: 0;\">\n");
            sb.append("        <input type=\"hidden\" name=\"signature\" value=\"").append(SecurityUtil.escapeHtml(cycle.getCanonicalSignature())).append("\">\n");
            sb.append("        <button type=\"submit\" class=\"btn btn-ink btn-sm\">Propose This Swap</button>\n");
            sb.append("      </form>\n");
        }
        sb.append("    </div>\n");
        sb.append("  </div>\n");

        // Inline SVG Flow Diagram
        sb.append(SvgGenerator.renderCycleSvg(cycle, currentUser.getId()));

        // Cycle Steps Summary
        sb.append("  <div style=\"display: flex; flex-direction: column; gap: 8px; margin-top: 14px; padding-top: 14px; border-top: 2px dashed #0D1F1A;\">\n");
        for (SwapLink link : cycle.getLinks()) {
            boolean isMe = link.getLearner().equals(currentUser);
            String avClass = getAvatarClass(link.getLearner().getName());
            String initial = link.getLearner().getName().substring(0, 1).toUpperCase();

            sb.append("    <div style=\"display: flex; align-items: center; gap: 10px; font-size: 15px;\">\n");
            sb.append("      <span class=\"av ").append(avClass).append("\" style=\"width:28px;height:28px;font-size:12px;\">").append(initial).append("</span>\n");
            sb.append("      <span><strong style=\"color: ").append(isMe ? "#0D1F1A" : "#33463F").append(";\">")
              .append(isMe ? "You" : SecurityUtil.escapeHtml(link.getLearner().getName())).append("</strong>");
            sb.append(" learns <span class=\"pill lm\" style=\"padding:2px 8px;font-size:13px;\">").append(SecurityUtil.escapeHtml(link.getSkill().getName())).append("</span>");
            sb.append(" from <strong>").append(SecurityUtil.escapeHtml(link.getTeacher().getName())).append("</strong></span>\n");
            sb.append("    </div>\n");
        }
        sb.append("  </div>\n");

        sb.append("</div>\n");
        return sb.toString();
    }

    private static String renderAlmostMatchCard(AlmostMatchFinder.Opportunity opp) {
        String teacher = opp.getSampleCycles().isEmpty() ? "Community" : opp.getSampleCycles().get(0).getLinks().get(0).getTeacher().getName();
        return """
        <div class="card card-lift" style="padding: 24px; border: 2px dashed #0D1F1A; background: #fff;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
            <strong style="font-size:18px;">%s</strong>
            <span class="pill lm">Unlocks %d Swaps</span>
          </div>
          <p style="font-size:14px; color:#33463F; margin-bottom:14px;">Offered by <b>%s</b>. Adding this to your learning list immediately completes a trade cycle!</p>
          <form action="/profile/skill/add" method="POST">
            <input type="hidden" name="type" value="wants">
            <input type="hidden" name="name" value="%s">
            <input type="hidden" name="category" value="TECH">
            <input type="hidden" name="level" value="BEGINNER">
            <button type="submit" class="btn btn-ghost btn-sm" style="width:100%%;">+ Add to Wanted Skills</button>
          </form>
        </div>
        """.formatted(
            SecurityUtil.escapeHtml(opp.getSuggestedSkill().getName()),
            opp.getUnlockedCyclesCount(),
            SecurityUtil.escapeHtml(teacher),
            SecurityUtil.escapeHtml(opp.getSuggestedSkill().getName())
        );
    }

    // =========================================================================
    // Page: Proposals
    // =========================================================================

    public static String renderProposals(Member currentUser, List<SwapProposal> proposals) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;\">\n");
        sb.append("  <div>\n");
        sb.append("    <h1 style=\"font-size: 2.4rem; font-weight: 800;\">🤝 Swap Proposals</h1>\n");
        sb.append("    <p style=\"color: #33463F; font-size: 1.05rem;\">Track active barter proposals, mutual consensus, and confidential contact reveals.</p>\n");
        sb.append("  </div>\n");
        sb.append("  <a href=\"/swaps\" class=\"btn btn-lime btn-md\">⚡ Find New Swaps</a>\n");
        sb.append("</div>\n");

        if (proposals.isEmpty()) {
            sb.append("<div class=\"card\" style=\"text-align: center; padding: 48px 24px;\">\n");
            sb.append("  <div style=\"font-size: 3rem; margin-bottom: 1rem;\">📭</div>\n");
            sb.append("  <h3 style=\"font-size: 1.4rem; font-weight: 800; margin-bottom: 0.5rem;\">No Proposals Yet</h3>\n");
            sb.append("  <p style=\"color: #4A5B55; max-width: 480px; margin: 0 auto 1.5rem auto;\">You haven't initiated or received any swap proposals. Head to Find Swaps to propose your first circular trade!</p>\n");
            sb.append("  <a href=\"/swaps\" class=\"btn btn-ink btn-md\">Browse Available Matches</a>\n");
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

        String stCls = status == ProposalStatus.ACTIVE ? "yes" : (status == ProposalStatus.CANCELLED ? "no" : "wait");

        sb.append("<div class=\"card\" style=\"margin-bottom: 24px; padding: 32px;\">\n");

        // Header
        sb.append("  <div style=\"display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; border-bottom: 2px dashed #0D1F1A; padding-bottom: 16px; margin-bottom: 20px;\">\n");
        sb.append("    <div style=\"display: flex; align-items: center; gap: 10px;\">\n");
        sb.append("      <h2 style=\"font-size: 24px; font-weight: 800;\">").append(p.getCycle().size()).append("-way swap proposal</h2>\n");
        sb.append("      <span class=\"st ").append(stCls).append("\">").append(status.getLabel()).append("</span>\n");
        if (isInitiator) {
            sb.append("      <span class=\"pill lm\">Created by You</span>\n");
        }
        sb.append("    </div>\n");

        // Countdown or status text
        long remainingMs = Math.max(0, p.getExpiresAt() - System.currentTimeMillis());
        long hoursLeft = remainingMs / (1000 * 60 * 60);
        long minsLeft = (remainingMs / (1000 * 60)) % 60;

        if (status == ProposalStatus.PENDING) {
            sb.append("    <div style=\"font-size: 14px; font-weight: 700; color: #4A5B55;\">⏳ Expires in <b>").append(hoursLeft).append("h ").append(minsLeft).append("m</b> if not all accept</div>\n");
        } else if (status == ProposalStatus.ACTIVE) {
            sb.append("    <div style=\"font-size: 14px; font-weight: 700; color: #0D1F1A; background:#CDF23A; padding:4px 12px; border-radius:999px;\">🚀 100% Consensus Reached!</div>\n");
        }
        sb.append("  </div>\n");

        // Participants Cards (grid of 3)
        sb.append("  <div style=\"display: flex; flex-wrap: wrap; gap: 18px; margin-bottom: 20px;\">\n");
        for (SwapLink link : p.getCycle().getLinks()) {
            Member m = link.getLearner();
            ResponseStatus resp = p.getResponseFor(m.getId());
            boolean isMe = m.equals(currentUser);
            String avClass = getAvatarClass(m.getName());
            String initial = m.getName().substring(0, 1).toUpperCase();
            String respCls = resp == ResponseStatus.ACCEPTED ? "yes" : (resp == ResponseStatus.DECLINED ? "no" : "wait");
            String respText = resp == ResponseStatus.ACCEPTED ? "Accepted" : (resp == ResponseStatus.DECLINED ? "Declined" : (isMe ? "Waiting for you" : "Pending"));

            // Contact details reveal check
            String contactInfo;
            if (isMe) {
                contactInfo = status == ProposalStatus.ACTIVE ? "Shared with your trade circle" : "Your own details";
            } else if (status == ProposalStatus.ACTIVE) {
                contactInfo = SecurityUtil.escapeHtml(m.getContact());
            } else {
                contactInfo = "Hidden until all accept";
            }

            sb.append("    <div style=\"flex: 1 1 260px; min-width: 0; border: 2px solid #0D1F1A; border-radius: 20px; padding: 20px; display: flex; flex-direction: column; gap: 12px; background: #F1F4EF;\">\n");
            sb.append("      <div style=\"display: flex; align-items: center; justify-content: space-between;\">\n");
            sb.append("        <span style=\"display: flex; align-items: center; gap: 8px;\">\n");
            sb.append("          <span class=\"av ").append(avClass).append("\">").append(initial).append("</span>\n");
            sb.append("          <b style=\"font-size: 17px;\">").append(isMe ? "You (" + m.getName() + ")" : SecurityUtil.escapeHtml(m.getName())).append("</b>\n");
            sb.append("        </span>\n");
            sb.append("        <span class=\"st ").append(respCls).append("\">").append(respText).append("</span>\n");
            sb.append("      </div>\n");
            sb.append("      <div style=\"font-size: 14px; color: #33463F;\">learns <span class=\"pill lm\" style=\"padding:2px 8px;font-size:12px;\">").append(SecurityUtil.escapeHtml(link.getSkill().getName())).append("</span> from <b>").append(SecurityUtil.escapeHtml(link.getTeacher().getName())).append("</b></div>\n");
            sb.append("      <div style=\"font-size: 13px; color: #4A5B55; margin-top: auto;\">Contact: <b style=\"color: #0D1F1A;\">").append(contactInfo).append("</b></div>\n");
            sb.append("    </div>\n");
        }
        sb.append("  </div>\n");

        // Action Controls
        if (status == ProposalStatus.PENDING) {
            sb.append("  <div style=\"display: flex; flex-wrap: wrap; gap: 12px; align-items: center; padding-top: 16px; border-top: 2px dashed #0D1F1A;\">\n");
            if (myResponse == ResponseStatus.PENDING) {
                sb.append("    <form action=\"/proposals/respond\" method=\"POST\" style=\"display: inline;\">\n");
                sb.append("      <input type=\"hidden\" name=\"proposalId\" value=\"").append(SecurityUtil.escapeHtml(p.getId())).append("\">\n");
                sb.append("      <input type=\"hidden\" name=\"action\" value=\"accept\">\n");
                sb.append("      <button type=\"submit\" class=\"btn btn-ink\">Accept swap</button>\n");
                sb.append("    </form>\n");

                sb.append("    <form action=\"/proposals/respond\" method=\"POST\" style=\"display: inline;\">\n");
                sb.append("      <input type=\"hidden\" name=\"proposalId\" value=\"").append(SecurityUtil.escapeHtml(p.getId())).append("\">\n");
                sb.append("      <input type=\"hidden\" name=\"action\" value=\"decline\">\n");
                sb.append("      <button type=\"submit\" class=\"btn btn-ghost\" onclick=\"return confirm('Decline this proposal? It will cancel the trade for all students.');\">Decline</button>\n");
                sb.append("    </form>\n");
            } else if (myResponse == ResponseStatus.ACCEPTED) {
                sb.append("    <span class=\"st yes\">✓ You Accepted</span>\n");
                sb.append("    <span style=\"font-size: 14px; color: #4A5B55;\">Waiting for fellow students to respond...</span>\n");
            }
            sb.append("  </div>\n");
        } else if (status == ProposalStatus.ACTIVE) {
            sb.append("  <div style=\"padding: 16px 20px; border-radius: 16px; background: #CDF23A; border: 2px solid #0D1F1A; font-weight: 700; color: #0D1F1A;\">\n");
            sb.append("    🎉 All students have accepted! Contact info is revealed above. You can now coordinate lessons directly.\n");
            sb.append("  </div>\n");
        } else if (status == ProposalStatus.CANCELLED) {
            sb.append("  <div style=\"padding: 16px 20px; border-radius: 16px; background: #F7A58E; border: 2px solid #0D1F1A; font-weight: 700; color: #0D1F1A;\">\n");
            sb.append("    ✕ Proposal was declined or cancelled. All students are freed to match in new cycles.\n");
            sb.append("  </div>\n");
        }

        sb.append("</div>\n");
        return sb.toString();
    }

    // =========================================================================
    // Page: Community Member Directory
    // =========================================================================

    public static String renderMembers(List<Member> members, Member currentUser) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;\">\n");
        sb.append("  <div>\n");
        sb.append("    <h1 style=\"font-size: 2.4rem; font-weight: 800;\">🌍 SkillSwap Community</h1>\n");
        sb.append("    <p style=\"color: #33463F; font-size: 1.05rem;\">Discover fellow students, explore skills offered and needed, and see who is learning what.</p>\n");
        sb.append("  </div>\n");
        if (currentUser != null) {
            sb.append("  <a href=\"/swaps\" class=\"btn btn-lime btn-md\">⚡ View My Matched Swaps</a>\n");
        } else {
            sb.append("  <a href=\"/signup\" class=\"btn btn-lime btn-md\">Join Community</a>\n");
        }
        sb.append("</div>\n");

        if (members.isEmpty()) {
            sb.append("<div class=\"card\" style=\"text-align: center; padding: 64px 32px; max-width: 640px; margin: 40px auto; display: flex; flex-direction: column; align-items: center; gap: 16px; box-shadow: 8px 8px 0 #0D1F1A;\">\n");
            sb.append("  <div style=\"font-size: 3.5rem;\">🌱</div>\n");
            sb.append("  <h2 style=\"font-size: 26px; font-weight: 800;\">No community members yet</h2>\n");
            sb.append("  <p style=\"color: #4A5B55; font-size: 16px; max-width: 460px; line-height: 1.5;\">\n");
            sb.append("    The member directory is completely fresh. Create an account, list what you can teach and what you want to learn, and be the first to start swapping!\n");
            sb.append("  </p>\n");
            if (currentUser == null) {
                sb.append("  <a href=\"/signup\" class=\"btn btn-lime btn-lg\">Join Community</a>\n");
            } else {
                sb.append("  <a href=\"/profile\" class=\"btn btn-lime btn-lg\">Add Skills in Profile</a>\n");
            }
            sb.append("</div>\n");
            return sb.toString();
        }

        sb.append("<div class=\"grid-3\">\n");
        for (Member m : members) {
            boolean isMe = currentUser != null && m.equals(currentUser);
            String avClass = getAvatarClass(m.getName());
            String initial = m.getName().substring(0, 1).toUpperCase();

            sb.append("<div class=\"card card-lift\" style=\"padding: 24px; display: flex; flex-direction: column; gap: 14px;\">\n");
            sb.append("  <div style=\"display: flex; justify-content: space-between; align-items: flex-start;\">\n");
            sb.append("    <div style=\"display: flex; align-items: center; gap: 12px;\">\n");
            sb.append("      <span class=\"av ").append(avClass).append("\">").append(initial).append("</span>\n");
            sb.append("      <div>\n");
            sb.append("        <h3 style=\"font-size: 1.25rem; font-weight: 800;\">").append(SecurityUtil.escapeHtml(m.getName())).append(isMe ? " <span style=\"font-size:0.8rem;color:#3A3FD8;\">(You)</span>" : "").append("</h3>\n");
            sb.append("        <div style=\"font-size: 0.85rem; color: #4A5B55;\">⭐ ").append(m.getAverageRating()).append(" / 5.0 (").append(m.getRatingCount()).append(" reviews)</div>\n");
            sb.append("      </div>\n");
            sb.append("    </div>\n");

            if (m.isLocked()) {
                sb.append("    <span class=\"st wait\">Locked</span>\n");
            } else {
                sb.append("    <span class=\"st yes\">Available</span>\n");
            }
            sb.append("  </div>\n");

            if (m.getBio() != null && !m.getBio().isEmpty()) {
                sb.append("  <p style=\"font-size: 0.9rem; color: #33463F; margin: 0;\">").append(SecurityUtil.escapeHtml(m.getBio())).append("</p>\n");
            }

            // Teaches chips
            sb.append("  <div>\n");
            sb.append("    <span style=\"font-size: 11px; font-weight: 700; color: #4A5B55; display: block; margin-bottom: 4px;\">TEACHES</span>\n");
            sb.append("    <div style=\"display: flex; flex-wrap: wrap; gap: 4px;\">\n");
            if (m.getTeaches().isEmpty()) {
                sb.append("      <span style=\"font-size:12px;color:#94a3b8;\">None listed</span>\n");
            } else {
                for (Skill s : m.getTeaches()) {
                    sb.append("      <span class=\"pill lm\" style=\"font-size:12px;padding:2px 8px;\">").append(SecurityUtil.escapeHtml(s.getName())).append("</span>\n");
                }
            }
            sb.append("    </div>\n");
            sb.append("  </div>\n");

            // Wants chips
            sb.append("  <div>\n");
            sb.append("    <span style=\"font-size: 11px; font-weight: 700; color: #4A5B55; display: block; margin-bottom: 4px;\">WANTS</span>\n");
            sb.append("    <div style=\"display: flex; flex-wrap: wrap; gap: 4px;\">\n");
            if (m.getWants().isEmpty()) {
                sb.append("      <span style=\"font-size:12px;color:#94a3b8;\">None listed</span>\n");
            } else {
                for (Skill s : m.getWants()) {
                    sb.append("      <span class=\"pill\" style=\"font-size:12px;padding:2px 8px;\">").append(SecurityUtil.escapeHtml(s.getName())).append("</span>\n");
                }
            }
            sb.append("    </div>\n");
            sb.append("  </div>\n");

            sb.append("  <div style=\"margin-top: auto; padding-top: 10px; border-top: 1px dashed #0D1F1A; display: flex; justify-content: space-between; align-items: center;\">\n");
            sb.append("    <span style=\"font-size: 12px; color: #4A5B55;\">⏱️ ").append(m.getWeeklyAvailabilityHours()).append(" hrs/wk</span>\n");
            if (!isMe && currentUser != null && !currentUser.isLocked()) {
                sb.append("    <a href=\"/swaps\" class=\"btn btn-ink btn-sm\" style=\"min-height:30px;padding:0 12px;font-size:12px;\">Find Match</a>\n");
            }
            sb.append("  </div>\n");

            sb.append("</div>\n");
        }
        sb.append("</div>\n");

        return sb.toString();
    }

    private static String getAvatarClass(String name) {
        if (name == null || name.trim().isEmpty()) return "av-c1";
        int hash = Math.abs(name.trim().hashCode());
        String[] classes = {"av-c1", "av-c2", "av-c3", "av-c4", "av-c5", "av-c6"};
        return classes[hash % classes.length];
    }
}
