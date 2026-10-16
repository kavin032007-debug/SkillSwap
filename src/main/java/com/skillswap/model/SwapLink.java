package com.skillswap.model;

import java.util.Objects;

/**
 * Represents a single directed edge in a barter swap cycle:
 * The {@code learner} learns the {@code skill} from the {@code teacher}.
 */
public final class SwapLink {
    private final Member learner;
    private final Member teacher;
    private final Skill skill;

    public SwapLink(Member learner, Member teacher, Skill skill) {
        this.learner = Objects.requireNonNull(learner, "Learner cannot be null");
        this.teacher = Objects.requireNonNull(teacher, "Teacher cannot be null");
        this.skill = Objects.requireNonNull(skill, "Skill cannot be null");
    }

    public Member getLearner() {
        return learner;
    }

    public Member getTeacher() {
        return teacher;
    }

    public Skill getSkill() {
        return skill;
    }

    public String getDescription() {
        return learner.getName() + " learns " + skill.getName() + " from " + teacher.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SwapLink other)) return false;
        return learner.equals(other.learner) &&
               teacher.equals(other.teacher) &&
               skill.sameSkillAs(other.skill);
    }

    @Override
    public int hashCode() {
        return Objects.hash(learner.getId(), teacher.getId(), skill.getName().toLowerCase());
    }

    @Override
    public String toString() {
        return learner.getName() + " --[" + skill.getName() + "]--> " + teacher.getName();
    }
}
