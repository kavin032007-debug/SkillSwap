package com.skillswap.model;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a student/member participating in the SkillSwap barter community.
 * Encapsulates member credentials, contact info, availability, taught skills,
 * desired skills, and reputation ratings.
 */
public class Member {
    private final String id;
    private String name;
    private String pinHash;
    private String pinSalt;
    private String contact;
    private String bio;
    private int weeklyAvailabilityHours;
    private String availabilityNote;
    private final Set<Skill> teaches;
    private final Set<Skill> wants;
    private final List<Rating> ratings;
    private volatile String lockedInProposalId; // Non-null when locked in an ACTIVE proposal

    public Member(String id, String name, String pinHash, String pinSalt, String contact) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Member ID cannot be empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Member name cannot be empty");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.pinHash = pinHash;
        this.pinSalt = pinSalt;
        this.contact = contact != null ? contact.trim() : "";
        this.bio = "";
        this.weeklyAvailabilityHours = 4;
        this.availabilityNote = "Flexible";
        this.teaches = Collections.synchronizedSet(new LinkedHashSet<>());
        this.wants = Collections.synchronizedSet(new LinkedHashSet<>());
        this.ratings = new CopyOnWriteArrayList<>();
        this.lockedInProposalId = null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public String getPinSalt() {
        return pinSalt;
    }

    public void setPinSalt(String pinSalt) {
        this.pinSalt = pinSalt;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact != null ? contact.trim() : "";
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio != null ? bio.trim() : "";
    }

    public int getWeeklyAvailabilityHours() {
        return weeklyAvailabilityHours;
    }

    public void setWeeklyAvailabilityHours(int weeklyAvailabilityHours) {
        this.weeklyAvailabilityHours = Math.max(1, weeklyAvailabilityHours);
    }

    public String getAvailabilityNote() {
        return availabilityNote;
    }

    public void setAvailabilityNote(String availabilityNote) {
        this.availabilityNote = availabilityNote != null ? availabilityNote.trim() : "Flexible";
    }

    public Set<Skill> getTeaches() {
        synchronized (teaches) {
            return new LinkedHashSet<>(teaches);
        }
    }

    public Set<Skill> getWants() {
        synchronized (wants) {
            return new LinkedHashSet<>(wants);
        }
    }

    public boolean addTeaches(Skill skill) {
        if (skill == null) return false;
        synchronized (teaches) {
            // Remove existing skill with same name if any
            teaches.removeIf(s -> s.sameSkillAs(skill));
            return teaches.add(skill);
        }
    }

    public boolean removeTeaches(String skillName) {
        if (skillName == null) return false;
        synchronized (teaches) {
            return teaches.removeIf(s -> s.getName().equalsIgnoreCase(skillName.trim()));
        }
    }

    public boolean addWants(Skill skill) {
        if (skill == null) return false;
        synchronized (wants) {
            wants.removeIf(s -> s.sameSkillAs(skill));
            return wants.add(skill);
        }
    }

    public boolean removeWants(String skillName) {
        if (skillName == null) return false;
        synchronized (wants) {
            return wants.removeIf(s -> s.getName().equalsIgnoreCase(skillName.trim()));
        }
    }

    public List<Rating> getRatings() {
        return Collections.unmodifiableList(ratings);
    }

    public void addRating(Rating rating) {
        if (rating != null) {
            ratings.add(rating);
        }
    }

    public double getAverageRating() {
        if (ratings.isEmpty()) {
            return 5.0; // Default welcoming baseline for new members
        }
        double sum = 0;
        for (Rating r : ratings) {
            sum += r.getScore();
        }
        return Math.round((sum / ratings.size()) * 10.0) / 10.0;
    }

    public int getRatingCount() {
        return ratings.size();
    }

    public String getLockedInProposalId() {
        return lockedInProposalId;
    }

    public boolean isLocked() {
        return lockedInProposalId != null && !lockedInProposalId.isEmpty();
    }

    public void lockInProposal(String proposalId) {
        this.lockedInProposalId = proposalId;
    }

    public void unlockFromProposal(String proposalId) {
        if (Objects.equals(this.lockedInProposalId, proposalId)) {
            this.lockedInProposalId = null;
        }
    }

    public void forceUnlock() {
        this.lockedInProposalId = null;
    }

    /**
     * Checks if this member can teach a specific wanted skill.
     */
    public Skill findMatchingTeaches(Skill wantedSkill) {
        if (wantedSkill == null) return null;
        synchronized (teaches) {
            for (Skill s : teaches) {
                if (s.canTeach(wantedSkill)) {
                    return s;
                }
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member member)) return false;
        return id.equalsIgnoreCase(member.id);
    }

    @Override
    public int hashCode() {
        return id.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return "Member{" + id + ", name='" + name + "'}";
    }
}
