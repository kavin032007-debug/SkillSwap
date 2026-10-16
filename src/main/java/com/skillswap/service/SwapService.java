package com.skillswap.service;

import com.skillswap.finder.AlmostMatchFinder;
import com.skillswap.finder.CycleSwapFinder;
import com.skillswap.model.*;
import com.skillswap.repository.MemberRepository;
import com.skillswap.repository.ProposalRepository;
import com.skillswap.scoring.CompositeScoring;
import com.skillswap.scoring.ScoringStrategy;

import java.util.*;

/**
 * Orchestrator service for swap cycle discovery, ranking, proposal lifecycle,
 * concurrency locking, and peer ratings.
 */
public class SwapService {
    private final MemberRepository memberRepository;
    private final ProposalRepository proposalRepository;
    private final ScoringStrategy defaultScoring;
    private final AlmostMatchFinder almostMatchFinder;

    public SwapService(MemberRepository memberRepository, ProposalRepository proposalRepository) {
        this.memberRepository = memberRepository;
        this.proposalRepository = proposalRepository;
        this.defaultScoring = CompositeScoring.defaultStrategy();
        this.almostMatchFinder = new AlmostMatchFinder();
    }

    /**
     * Finds and ranks swap cycles for a member up to a maximum cycle length.
     */
    public List<SwapCycle> discoverSwaps(Member member, int maxCycleLength) {
        if (member == null) {
            return Collections.emptyList();
        }

        // Clean up any expired proposals first to free locked members
        cleanupExpiredProposals();

        List<Member> allMembers = memberRepository.findAll();
        CycleSwapFinder finder = new CycleSwapFinder(Math.max(2, Math.min(5, maxCycleLength)));
        List<SwapCycle> cycles = finder.find(member, allMembers);

        // Score and rank cycles
        for (SwapCycle cycle : cycles) {
            double score = defaultScoring.score(cycle);
            cycle.setScore(score);
        }

        cycles.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return cycles;
    }

    /**
     * Discovers high-potential almost-match skill opportunities (Feature F7).
     */
    public List<AlmostMatchFinder.Opportunity> discoverAlmostMatches(Member member, int maxSuggestions) {
        if (member == null) return Collections.emptyList();
        List<Member> allMembers = memberRepository.findAll();
        return almostMatchFinder.findOpportunities(member, allMembers, maxSuggestions);
    }

    /**
     * Creates a formal proposal for a swap cycle.
     */
    public synchronized SwapProposal createProposal(String initiatorMemberId, SwapCycle cycle) {
        Objects.requireNonNull(cycle, "Swap cycle cannot be null");
        Member initiator = memberRepository.findById(initiatorMemberId)
                .orElseThrow(() -> new IllegalArgumentException("Initiator member not found"));

        if (!cycle.containsMember(initiator)) {
            throw new IllegalArgumentException("Initiator must be a participant in the swap cycle");
        }

        // Check if any member is locked in an ACTIVE proposal
        for (Member m : cycle.getMembers()) {
            Optional<Member> fresh = memberRepository.findById(m.getId());
            if (fresh.isPresent() && fresh.get().isLocked()) {
                throw new IllegalStateException("Member " + fresh.get().getName() +
                        " is currently participating in an active swap proposal");
            }
        }

        // Re-validate cycle integrity
        if (!cycle.isValid()) {
            throw new IllegalStateException("One or more skills in this cycle are no longer offered or desired");
        }

        String proposalId = "prop-" + UUID.randomUUID().toString().substring(0, 8);
        SwapProposal proposal = new SwapProposal(proposalId, cycle, initiatorMemberId);

        proposalRepository.save(proposal);

        // If all members already accepted (e.g. 1-person cycle edge case), lock them
        if (proposal.isActive()) {
            lockCycleMembers(proposal);
        }

        return proposal;
    }

    /**
     * Concurrency-safe acceptance of a proposal.
     * Re-validates the cycle at accept time as required by PRD Section 6.
     */
    public synchronized boolean acceptProposal(String proposalId, String memberId) {
        SwapProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        if (proposal.isExpired()) {
            unlockCycleMembers(proposal);
            proposalRepository.save(proposal);
            throw new IllegalStateException("This proposal has expired after 48 hours");
        }

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new IllegalStateException("Proposal is not in pending state");
        }

        // Re-validate cycle integrity before accepting
        if (!proposal.getCycle().isValid()) {
            proposal.cancel(memberId);
            unlockCycleMembers(proposal);
            proposalRepository.save(proposal);
            throw new IllegalStateException("A participant's skills changed; this proposal has been automatically cancelled");
        }

        boolean accepted = proposal.accept(memberId);
        if (accepted) {
            // Check if this acceptance made the proposal fully ACTIVE!
            if (proposal.isActive()) {
                lockCycleMembers(proposal);
            }
            proposalRepository.save(proposal);
        }
        return accepted;
    }

    /**
     * Declines a proposal. Declining by any participant cancels the proposal and frees everyone.
     */
    public synchronized boolean declineProposal(String proposalId, String memberId) {
        SwapProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        boolean declined = proposal.decline(memberId);
        if (declined) {
            unlockCycleMembers(proposal);
            proposalRepository.save(proposal);
        }
        return declined;
    }

    /**
     * Cancels a proposal.
     */
    public synchronized boolean cancelProposal(String proposalId, String memberId) {
        SwapProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        boolean cancelled = proposal.cancel(memberId);
        if (cancelled) {
            unlockCycleMembers(proposal);
            proposalRepository.save(proposal);
        }
        return cancelled;
    }

    /**
     * Marks an active proposal as completed, enabling teacher ratings.
     */
    public synchronized boolean completeProposal(String proposalId) {
        SwapProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        boolean completed = proposal.complete();
        if (completed) {
            unlockCycleMembers(proposal);
            proposalRepository.save(proposal);
        }
        return completed;
    }

    /**
     * Submits a rating (1-5) for a teacher following a completed swap.
     */
    public synchronized void rateTeacher(String proposalId, String fromMemberId, String teacherId, int score, String feedback) {
        SwapProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        if (proposal.getStatus() != ProposalStatus.COMPLETED && proposal.getStatus() != ProposalStatus.ACTIVE) {
            throw new IllegalStateException("Ratings can only be submitted for active or completed swaps");
        }

        // Verify that fromMember was taught by teacher in this cycle
        SwapLink link = proposal.getCycle().getLinkForLearner(fromMemberId);
        if (link == null || !link.getTeacher().getId().equalsIgnoreCase(teacherId)) {
            throw new IllegalArgumentException("Member " + fromMemberId + " did not learn from teacher " + teacherId + " in this swap");
        }

        Member learner = memberRepository.findById(fromMemberId)
                .orElseThrow(() -> new IllegalArgumentException("Learner member not found"));
        Member teacher = memberRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher member not found"));

        Rating rating = new Rating(learner.getId(), learner.getName(), score, feedback, System.currentTimeMillis());
        teacher.addRating(rating);
        memberRepository.save(teacher);
    }

    /**
     * Scans and expires proposals older than 48 hours, unlocking members.
     */
    public synchronized void cleanupExpiredProposals() {
        for (SwapProposal p : proposalRepository.findAll()) {
            if (p.isExpired()) {
                unlockCycleMembers(p);
                proposalRepository.save(p);
            }
        }
    }

    private void lockCycleMembers(SwapProposal proposal) {
        for (Member m : proposal.getCycle().getMembers()) {
            Optional<Member> fresh = memberRepository.findById(m.getId());
            fresh.ifPresent(member -> {
                member.lockInProposal(proposal.getId());
                memberRepository.save(member);
            });
        }
    }

    private void unlockCycleMembers(SwapProposal proposal) {
        for (Member m : proposal.getCycle().getMembers()) {
            Optional<Member> fresh = memberRepository.findById(m.getId());
            fresh.ifPresent(member -> {
                member.unlockFromProposal(proposal.getId());
                memberRepository.save(member);
            });
        }
    }
}
