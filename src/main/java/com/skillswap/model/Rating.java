package com.skillswap.model;

import java.util.Objects;

/**
 * Represents a star rating and qualitative feedback left by a learner for a teacher
 * following a completed swap.
 */
public final class Rating {
    private final String fromMemberId;
    private final String fromMemberName;
    private final int score; // 1 to 5
    private final String feedback;
    private final long timestamp;

    public Rating(String fromMemberId, String fromMemberName, int score, String feedback, long timestamp) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Score must be between 1 and 5 stars");
        }
        this.fromMemberId = fromMemberId;
        this.fromMemberName = fromMemberName != null ? fromMemberName : "Anonymous";
        this.score = score;
        this.feedback = feedback != null ? feedback.trim() : "";
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
    }

    public String getFromMemberId() {
        return fromMemberId;
    }

    public String getFromMemberName() {
        return fromMemberName;
    }

    public int getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rating other)) return false;
        return score == other.score &&
               timestamp == other.timestamp &&
               Objects.equals(fromMemberId, other.fromMemberId) &&
               Objects.equals(feedback, other.feedback);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromMemberId, score, feedback, timestamp);
    }
}
