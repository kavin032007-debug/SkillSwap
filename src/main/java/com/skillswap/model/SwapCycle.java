package com.skillswap.model;

import java.util.*;

/**
 * Represents a closed barter cycle where each member learns one skill and teaches one skill.
 * Encapsulates the ordered sequence of {@link SwapLink}s and an algorithmically computed score.
 */
public class SwapCycle {
    private final List<SwapLink> links;
    private double score;

    public SwapCycle(List<SwapLink> links) {
        if (links == null || links.size() < 2) {
            throw new IllegalArgumentException("A swap cycle must contain at least 2 swap links");
        }
        this.links = Collections.unmodifiableList(new ArrayList<>(links));
        this.score = 0.0;
    }

    public List<SwapLink> getLinks() {
        return links;
    }

    public int size() {
        return links.size();
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public List<Member> getMembers() {
        List<Member> members = new ArrayList<>();
        for (SwapLink link : links) {
            members.add(link.getLearner());
        }
        return members;
    }

    public boolean containsMember(Member member) {
        if (member == null) return false;
        for (SwapLink link : links) {
            if (link.getLearner().equals(member) || link.getTeacher().equals(member)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsMemberId(String memberId) {
        if (memberId == null) return false;
        for (SwapLink link : links) {
            if (link.getLearner().getId().equalsIgnoreCase(memberId) ||
                link.getTeacher().getId().equalsIgnoreCase(memberId)) {
                return true;
            }
        }
        return false;
    }

    public SwapLink getLinkForLearner(String memberId) {
        for (SwapLink link : links) {
            if (link.getLearner().getId().equalsIgnoreCase(memberId)) {
                return link;
            }
        }
        return null;
    }

    public SwapLink getLinkForTeacher(String memberId) {
        for (SwapLink link : links) {
            if (link.getTeacher().getId().equalsIgnoreCase(memberId)) {
                return link;
            }
        }
        return null;
    }

    /**
     * Re-validates the cycle against current members' taught and wanted skills,
     * ensuring skills haven't been removed or altered.
     */
    public boolean isValid() {
        for (SwapLink link : links) {
            Member learner = link.getLearner();
            Member teacher = link.getTeacher();
            Skill skill = link.getSkill();

            // Teacher must still teach the skill
            Skill taught = teacher.findMatchingTeaches(skill);
            if (taught == null) {
                return false;
            }
            // Learner must still want the skill
            boolean wants = false;
            for (Skill w : learner.getWants()) {
                if (w.sameSkillAs(skill) && SkillLevel.isCompatible(w.getLevel(), taught.getLevel())) {
                    wants = true;
                    break;
                }
            }
            if (!wants) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a normalized canonical representation of this cycle by rotating the cycle
     * to start at the lexicographically smallest member ID.
     * This ensures duplicate rotations (e.g. A->B->C vs B->C->A) produce identical signatures.
     */
    public String getCanonicalSignature() {
        int n = links.size();
        int minIndex = 0;
        String minId = links.get(0).getLearner().getId();

        for (int i = 1; i < n; i++) {
            String currentId = links.get(i).getLearner().getId();
            if (currentId.compareToIgnoreCase(minId) < 0) {
                minId = currentId;
                minIndex = i;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            SwapLink link = links.get((minIndex + i) % n);
            sb.append(link.getLearner().getId())
              .append("-[:")
              .append(link.getSkill().getName().toLowerCase())
              .append("]->")
              .append(link.getTeacher().getId())
              .append(";");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SwapCycle other)) return false;
        return this.getCanonicalSignature().equals(other.getCanonicalSignature());
    }

    @Override
    public int hashCode() {
        return getCanonicalSignature().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SwapCycle(").append(size()).append("-way) [");
        for (int i = 0; i < links.size(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(links.get(i).getDescription());
        }
        sb.append("] score=").append(String.format("%.2f", score));
        return sb.toString();
    }
}
