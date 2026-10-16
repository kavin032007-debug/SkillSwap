package com.skillswap.model;

import java.util.Objects;

/**
 * Represents a skill taught or desired by a member.
 * Encapsulates the skill name, category domain, and proficiency level.
 */
public final class Skill {
    private final String name;
    private final SkillCategory category;
    private final SkillLevel level;

    public Skill(String name, SkillCategory category, SkillLevel level) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill name cannot be empty");
        }
        this.name = name.trim();
        this.category = category != null ? category : SkillCategory.OTHER;
        this.level = level != null ? level : SkillLevel.BEGINNER;
    }

    public String getName() {
        return name;
    }

    public SkillCategory getCategory() {
        return category;
    }

    public SkillLevel getLevel() {
        return level;
    }

    /**
     * Checks if this skill shares the same identity (name and category) as another skill,
     * ignoring case differences in the name.
     */
    public boolean sameSkillAs(Skill other) {
        if (other == null) return false;
        return this.name.equalsIgnoreCase(other.name);
    }

    /**
     * Checks if this skill (as offered by a teacher) satisfies the requirement of a learner's wanted skill,
     * including name matching and level compatibility.
     *
     * @param wantedSkill the skill desired by the learner
     * @return true if this skill can teach the wanted skill
     */
    public boolean canTeach(Skill wantedSkill) {
        if (wantedSkill == null) return false;
        if (!sameSkillAs(wantedSkill)) return false;
        return SkillLevel.isCompatible(wantedSkill.getLevel(), this.level);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skill other)) return false;
        return name.equalsIgnoreCase(other.name) &&
               category == other.category &&
               level == other.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(), category, level);
    }

    @Override
    public String toString() {
        return name + " (" + level.getDisplayName() + ", " + category.getDisplayName() + ")";
    }
}
