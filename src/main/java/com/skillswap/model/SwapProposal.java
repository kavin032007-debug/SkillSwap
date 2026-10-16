package com.skillswap.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a formal proposal for members in a {@link SwapCycle} to barter skills.
 * Encapsulates the 48-hour expiration lifecycle, member acceptance states,
 * and concurrency-safe transitions.
 */
public class SwapProposal {
    public static final long EXPIRATION_DURATION_MS = 48L * 60L * 60L * 1000L; // 48 hours

    private final String id;
    private final SwapCycle cycle;
    private final String initiatorMemberId;
    private final Map<String, ResponseStatus> memberResponses;
    private volatile ProposalStatus status;
    private final long createdAt;
    private final long expiresAt;
    private Long completedAt;

    public SwapProposal(String id, SwapCycle cycle, String initiatorMemberId) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Proposal ID cannot be empty");
        }
        if (cycle == null) {
            throw new IllegalArgumentException("SwapCycle cannot be null");
        }
        this.id = id.trim();
        this.cycle = cycle;
        this.initiatorMemberId = initiatorMemberId;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = this.createdAt + EXPIRATION_DURATION_MS;
        this.status = ProposalStatus.PENDING;
        this.completedAt = null;

        this.memberResponses = new ConcurrentHashMap<>();
        for (Member m : cycle.getMembers()) {
            if (m.getId().equalsIgnoreCase(initiatorMemberId)) {
                memberResponses.put(m.getId(), ResponseStatus.ACCEPTED);
            } else {
                memberResponses.put(m.getId(), ResponseStatus.PENDING);
            }
        }

        // If it's a 1-person cycle (should not occur, min 2), check immediately
        checkAllAccepted();
    }

    /**
     * Constructor for reconstruction / deserialization.
     */
    public SwapProposal(String id, SwapCycle cycle, String initiatorMemberId,
                        Map<String, ResponseStatus> responses, ProposalStatus status,
                        long createdAt, long expiresAt, Long completedAt) {
        this.id = id;
        this.cycle = cycle;
        this.initiatorMemberId = initiatorMemberId;
        this.memberResponses = new ConcurrentHashMap<>(responses);
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.completedAt = completedAt;
    }

    public String getId() {
        return id;
    }

    public SwapCycle getCycle() {
        return cycle;
    }

    public String getInitiatorMemberId() {
        return initiatorMemberId;
    }

    public Map<String, ResponseStatus> getMemberResponses() {
        return Collections.unmodifiableMap(memberResponses);
    }

    public ProposalStatus getStatus() {
        checkExpiration();
        return status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    /**
     * Checks if proposal has expired past its 48-hour window.
     * Transitions PENDING to EXPIRED if so.
     */
    public synchronized boolean checkExpiration() {
        if (status == ProposalStatus.PENDING && System.currentTimeMillis() > expiresAt) {
            status = ProposalStatus.EXPIRED;
            return true;
        }
        return false;
    }

    public boolean isExpired() {
        return checkExpiration() || status == ProposalStatus.EXPIRED;
    }

    public boolean isActive() {
        return getStatus() == ProposalStatus.ACTIVE;
    }

    public boolean isCompleted() {
        return getStatus() == ProposalStatus.COMPLETED;
    }

    public boolean areContactsRevealed() {
        ProposalStatus s = getStatus();
        return s == ProposalStatus.ACTIVE || s == ProposalStatus.COMPLETED;
    }

    public ResponseStatus getResponseFor(String memberId) {
        return memberResponses.get(memberId);
    }

    /**
     * Accepts the proposal on behalf of a participant.
     * If all members have accepted, transitions proposal status to ACTIVE!
     *
     * @return true if status was updated
     */
    public synchronized boolean accept(String memberId) {
        if (isExpired() || status != ProposalStatus.PENDING) {
            return false;
        }
        if (!memberResponses.containsKey(memberId)) {
            return false;
        }

        memberResponses.put(memberId, ResponseStatus.ACCEPTED);
        checkAllAccepted();
        return true;
    }

    /**
     * Declines the proposal. Any decline immediately cancels the entire proposal
     * and frees all participants.
     */
    public synchronized boolean decline(String memberId) {
        if (status != ProposalStatus.PENDING && status != ProposalStatus.ACTIVE) {
            return false;
        }
        if (!memberResponses.containsKey(memberId)) {
            return false;
        }

        memberResponses.put(memberId, ResponseStatus.DECLINED);
        status = ProposalStatus.CANCELLED;
        return true;
    }

    /**
     * Cancels the proposal by the initiator or administrator.
     */
    public synchronized boolean cancel(String memberId) {
        if (status != ProposalStatus.PENDING && status != ProposalStatus.ACTIVE) {
            return false;
        }
        status = ProposalStatus.CANCELLED;
        return true;
    }

    /**
     * Marks an active swap as completed, allowing participants to rate their teachers.
     */
    public synchronized boolean complete() {
        if (status == ProposalStatus.ACTIVE) {
            status = ProposalStatus.COMPLETED;
            completedAt = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private void checkAllAccepted() {
        boolean allAccepted = true;
        for (ResponseStatus r : memberResponses.values()) {
            if (r != ResponseStatus.ACCEPTED) {
                allAccepted = false;
                break;
            }
        }
        if (allAccepted && status == ProposalStatus.PENDING) {
            status = ProposalStatus.ACTIVE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SwapProposal that)) return false;
        return id.equalsIgnoreCase(that.id);
    }

    @Override
    public int hashCode() {
        return id.toLowerCase().hashCode();
    }
}
