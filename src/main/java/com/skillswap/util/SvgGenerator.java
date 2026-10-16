package com.skillswap.util;

import com.skillswap.model.Member;
import com.skillswap.model.SwapCycle;
import com.skillswap.model.SwapLink;

import java.util.List;

/**
 * Generates inline SVG diagrams illustrating barter swap cycles (2-way, 3-way, 4-way).
 * Matches the SkillSwap high-contrast neo-brutalist theme (ink, lime, pill tokens).
 */
public final class SvgGenerator {

    private SvgGenerator() {}

    /**
     * Renders a responsive SVG diagram for a swap cycle.
     *
     * @param cycle the swap cycle to illustrate
     * @param currentUserId the ID of the logged in user (highlighted in electric lime)
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
          .append("\" class=\"swap-cycle-svg\" xmlns=\"http://www.w3.org/2000/svg\" style=\"display:block;margin:1rem auto;max-width:100%;\">\n");

        // Definitions: Arrowhead Marker
        sb.append("""
          <defs>
            <marker id="arrowHead" viewBox="0 0 10 10" refX="24" refY="5"
                    markerWidth="6" markerHeight="6" orient="auto-start-reverse">
              <path d="M 0 1 L 10 5 L 0 9 z" fill="#0D1F1A" />
            </marker>
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
                cx = midX + (midX - centerX) * 0.35;
                cy = midY + (midY - centerY) * 0.35;
            }

            // Path with directional arrowhead
            sb.append("<path d=\"M ").append(x1).append(" ").append(y1)
              .append(" Q ").append(cx).append(" ").append(cy)
              .append(" ").append(x2).append(" ").append(y2)
              .append("\" fill=\"none\" stroke=\"#0D1F1A\" stroke-width=\"2.5\" ")
              .append("stroke-dasharray=\"6 3\" marker-end=\"url(#arrowHead)\" />\n");

            // Skill pill badge at the curve peak
            String skillName = SecurityUtil.escapeHtml(link.getSkill().getName());
            int pillWidth = Math.max(90, skillName.length() * 8 + 24);
            int pillHeight = 26;

            sb.append("<g transform=\"translate(").append(cx - pillWidth / 2.0).append(",")
              .append(cy - pillHeight / 2.0).append(")\">\n")
              .append("<rect width=\"").append(pillWidth).append("\" height=\"").append(pillHeight)
              .append("\" rx=\"13\" fill=\"#CDF23A\" stroke=\"#0D1F1A\" stroke-width=\"2\" />\n")
              .append("<text x=\"").append(pillWidth / 2.0).append("\" y=\"17\" fill=\"#0D1F1A\" ")
              .append("font-family=\"'DM Sans', sans-serif\" font-size=\"11\" font-weight=\"700\" text-anchor=\"middle\">")
              .append(skillName).append("</text>\n")
              .append("</g>\n");
        }

        // Draw Member Nodes on top
        for (int i = 0; i < count; i++) {
            Member member = links.get(i).getLearner();
            double x = positions[i][0];
            double y = positions[i][1];
            boolean isCurrentUser = currentUserId != null && currentUserId.equalsIgnoreCase(member.getId());

            String nodeColor = getAvatarColor(member.getName());
            String initials = getInitials(member.getName());

            sb.append("<g class=\"svg-node\" transform=\"translate(").append(x).append(",").append(y).append(")\">\n");

            // Outer highlight circle for current user
            if (isCurrentUser) {
                sb.append("<circle r=\"32\" fill=\"none\" stroke=\"#0D1F1A\" stroke-width=\"2\" stroke-dasharray=\"4 4\" />\n");
            }

            // Node Circle
            sb.append("<circle r=\"24\" fill=\"").append(nodeColor).append("\" stroke=\"#0D1F1A\" stroke-width=\"2.5\" />\n");

            // Initials text
            sb.append("<text y=\"5\" fill=\"#0D1F1A\" font-family=\"'Bricolage Grotesque', sans-serif\" font-size=\"14\" font-weight=\"800\" text-anchor=\"middle\">")
              .append(initials).append("</text>\n");

            // Name Label below/above node
            double labelY = (y > centerY) ? 42 : -32;
            String displayName = isCurrentUser ? "You (" + member.getName() + ")" : member.getName();
            sb.append("<text y=\"").append(labelY).append("\" fill=\"#0D1F1A\" ")
              .append("font-family=\"'Bricolage Grotesque', sans-serif\" font-size=\"13\" font-weight=\"700\" text-anchor=\"middle\">")
              .append(SecurityUtil.escapeHtml(displayName)).append("</text>\n");

            sb.append("</g>\n");
        }

        sb.append("</svg>\n");
        return sb.toString();
    }

    private static String getAvatarColor(String name) {
        if (name == null) return "#CDF23A";
        String n = name.trim().toLowerCase();
        if (n.startsWith("asha")) return "#CDF23A";
        if (n.startsWith("ravi")) return "#C9B8FF";
        if (n.startsWith("meena")) return "#F7A58E";
        if (n.startsWith("kabir")) return "#8FD8F2";
        if (n.startsWith("neha")) return "#FBE08A";
        return "#CDF23A";
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
