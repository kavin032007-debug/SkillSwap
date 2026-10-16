package com.skillswap.util;

import com.skillswap.model.Member;
import com.skillswap.model.SwapCycle;
import com.skillswap.model.SwapLink;

import java.util.List;

/**
 * Generates inline SVG diagrams illustrating barter swap cycles (2-way, 3-way, 4-way).
 * Highlights the current user, shows direction of skill flow, and visualizes member nodes.
 */
public final class SvgGenerator {

    private SvgGenerator() {}

    /**
     * Renders a responsive SVG diagram for a swap cycle.
     *
     * @param cycle the swap cycle to illustrate
     * @param currentUserId the ID of the logged in user (highlighted in gold/cyan)
     * @return SVG XML string ready for inline embedding in HTML
     */
    public static String renderCycleSvg(SwapCycle cycle, String currentUserId) {
        if (cycle == null || cycle.size() < 2) {
            return "<div class=\"text-muted\">Diagram unavailable</div>";
        }

        int count = cycle.size();
        int width = 540;
        int height = count == 2 ? 260 : (count == 3 ? 360 : 400);
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = count == 2 ? 160 : (count == 3 ? 125 : 135);

        List<SwapLink> links = cycle.getLinks();

        // Calculate positions for each member in the cycle
        double[][] positions = new double[count][2];
        for (int i = 0; i < count; i++) {
            double angle;
            if (count == 2) {
                positions[0] = new double[]{centerX - radius, centerY};
                positions[1] = new double[]{centerX + radius, centerY};
                break;
            } else if (count == 3) {
                // Triangle: top, bottom-right, bottom-left
                angle = -Math.PI / 2 + (i * 2 * Math.PI / 3);
            } else {
                // 4-way: diamond/square
                angle = -Math.PI / 2 + (i * 2 * Math.PI / count);
            }
            positions[i][0] = centerX + radius * Math.cos(angle);
            positions[i][1] = centerY + radius * Math.sin(angle);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<svg viewBox=\"0 0 ").append(width).append(" ").append(height)
          .append("\" class=\"swap-cycle-svg\" xmlns=\"http://www.w3.org/2000/svg\">\n");

        // Definitions: Gradients, Markers, Filters
        sb.append("""
          <defs>
            <linearGradient id="linkGrad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stop-color="#6366f1" />
              <stop offset="100%" stop-color="#10b981" />
            </linearGradient>
            <marker id="arrowHead" viewBox="0 0 10 10" refX="24" refY="5"
                    markerWidth="6" markerHeight="6" orient="auto-start-reverse">
              <path d="M 0 1 L 10 5 L 0 9 z" fill="#10b981" />
            </marker>
            <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
              <feDropShadow dx="0" dy="4" stdDeviation="6" flood-color="#6366f1" flood-opacity="0.35"/>
            </filter>
          </defs>
        """);

        // Draw curved connector paths and skill badges
        for (int i = 0; i < count; i++) {
            SwapLink link = links.get(i);
            int nextIdx = (i + 1) % count;

            double x1 = positions[i][0];
            double y1 = positions[i][1];
            double x2 = positions[nextIdx][0];
            double y2 = positions[nextIdx][1];

            // Curvature control point
            double cx, cy;
            if (count == 2) {
                cx = (x1 + x2) / 2;
                cy = i == 0 ? centerY - 65 : centerY + 65;
            } else {
                double midX = (x1 + x2) / 2;
                double midY = (y1 + y2) / 2;
                // Curve outwards slightly from center
                cx = midX + (midX - centerX) * 0.35;
                cy = midY + (midY - centerY) * 0.35;
            }

            // Path with directional arrowhead
            sb.append("<path d=\"M ").append(x1).append(" ").append(y1)
              .append(" Q ").append(cx).append(" ").append(cy)
              .append(" ").append(x2).append(" ").append(y2)
              .append("\" fill=\"none\" stroke=\"url(#linkGrad)\" stroke-width=\"3\" ")
              .append("stroke-dasharray=\"6 3\" marker-end=\"url(#arrowHead)\" />\n");

            // Skill pill badge at the curve peak
            String skillName = SecurityUtil.escapeHtml(link.getSkill().getName());
            int pillWidth = Math.max(90, skillName.length() * 8 + 24);
            int pillHeight = 26;

            sb.append("<g transform=\"translate(").append(cx - pillWidth / 2.0).append(",")
              .append(cy - pillHeight / 2.0).append(")\">\n")
              .append("<rect width=\"").append(pillWidth).append("\" height=\"").append(pillHeight)
              .append("\" rx=\"13\" fill=\"#1e293b\" stroke=\"#38bdf8\" stroke-width=\"1.5\" />\n")
              .append("<text x=\"").append(pillWidth / 2.0).append("\" y=\"17\" fill=\"#f8fafc\" ")
              .append("font-size=\"11\" font-weight=\"600\" text-anchor=\"middle\">")
              .append(skillName).append("</text>\n")
              .append("</g>\n");
        }

        // Draw Member Nodes on top
        for (int i = 0; i < count; i++) {
            Member member = links.get(i).getLearner();
            double x = positions[i][0];
            double y = positions[i][1];
            boolean isCurrentUser = currentUserId != null && currentUserId.equalsIgnoreCase(member.getId());

            String nodeColor = isCurrentUser ? "#fbbf24" : "#6366f1";
            String strokeColor = isCurrentUser ? "#f59e0b" : "#818cf8";
            String initials = getInitials(member.getName());

            sb.append("<g class=\"svg-node\" transform=\"translate(").append(x).append(",").append(y).append(")\">\n");

            // Outer pulse circle for current user
            if (isCurrentUser) {
                sb.append("<circle r=\"32\" fill=\"none\" stroke=\"#fbbf24\" stroke-width=\"2\" opacity=\"0.5\" ")
                  .append("stroke-dasharray=\"4 4\" />\n");
            }

            // Node Circle
            sb.append("<circle r=\"24\" fill=\"#0f172a\" stroke=\"").append(strokeColor)
              .append("\" stroke-width=\"3\" filter=\"url(#glow)\" />\n");

            // Initials text
            sb.append("<text y=\"5\" fill=\"#ffffff\" font-size=\"12\" font-weight=\"700\" text-anchor=\"middle\">")
              .append(initials).append("</text>\n");

            // Name Label below/above node
            double labelY = (y > centerY) ? 42 : -32;
            String displayName = isCurrentUser ? "You (" + member.getName() + ")" : member.getName();
            sb.append("<text y=\"").append(labelY).append("\" fill=\"")
              .append(isCurrentUser ? "#fbbf24" : "#e2e8f0")
              .append("\" font-size=\"12\" font-weight=\"600\" text-anchor=\"middle\">")
              .append(SecurityUtil.escapeHtml(displayName)).append("</text>\n");

            sb.append("</g>\n");
        }

        sb.append("</svg>\n");
        return sb.toString();
    }

    private static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }
}
