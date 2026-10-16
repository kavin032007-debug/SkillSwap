package com.skillswap.model;

/**
 * Skill proficiency levels and barter compatibility rules.
 */
public enum SkillLevel {
    BEGINNER("Beginner", 1, "#10b981"),
    INTERMEDIATE("Intermediate", 2, "#3b82f6"),
    ADVANCED("Advanced", 3, "#8b5cf6");

    private final String displayName;
    private final int rank;
    private final String color;

    SkillLevel(String displayName, int rank, String color) {
        this.displayName = displayName;
        this.rank = rank;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getRank() {
        return rank;
    }

    public String getColor() {
        return color;
    }

    /**
     * Checks if a teacher at {@code teacherLevel} can teach a learner at {@code learnerLevel}.
     * PRD rule:
     * - A BEGINNER learner requires an INTERMEDIATE or ADVANCED teacher.
     * - An INTERMEDIATE learner requires an INTERMEDIATE or ADVANCED teacher.
     * - An ADVANCED learner requires an ADVANCED teacher.
     *
     * @param learnerLevel the level of skill the learner wants to acquire
     * @param teacherLevel the level of skill the teacher offers
     * @return true if teacher level is sufficient for the learner
     */
    public static boolean isCompatible(SkillLevel learnerLevel, SkillLevel teacherLevel) {
        if (learnerLevel == null || teacherLevel == null) {
            return false;
        }
        if (learnerLevel == BEGINNER) {
            return teacherLevel.rank >= INTERMEDIATE.rank;
        }
        if (learnerLevel == INTERMEDIATE) {
            return teacherLevel.rank >= INTERMEDIATE.rank;
        }
        return teacherLevel.rank >= ADVANCED.rank;
    }

    public static SkillLevel fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BEGINNER;
        }
        String clean = text.trim().toUpperCase();
        for (SkillLevel lvl : values()) {
            if (lvl.name().equalsIgnoreCase(clean) || lvl.displayName.equalsIgnoreCase(text.trim())) {
                return lvl;
            }
        }
        return BEGINNER;
    }
}
