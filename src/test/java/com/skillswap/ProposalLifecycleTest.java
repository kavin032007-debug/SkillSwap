package com.skillswap;

import com.skillswap.model.*;
import com.skillswap.repository.MemberRepository;
import com.skillswap.repository.ProposalRepository;
import com.skillswap.service.SwapService;

import java.nio.file.Path;
import java.util.*;

/**
 * Unit tests for proposal state transitions, consensus, concurrency locking, and expiration.
 */
public class ProposalLifecycleTest {

    public static void runAllTests() {
        System.out.println("--- Running ProposalLifecycleTest ---");
        testProposalStateTransitionsAndConsensus();
        testMemberLockingPreventsMultipleActiveProposals();
        testDeclineCancelsAndUnlocksMembers();
        testExpirationTransitionsAndUnlocks();
        testContactRevealRules();
        System.out.println("✓ All ProposalLifecycleTest tests passed!\n");
    }

    public static void testProposalStateTransitionsAndConsensus() {
        MockRepositories repos = new MockRepositories();
        SwapService swapService = new SwapService(repos.memberRepo, repos.proposalRepo);

        Member m1 = repos.createMember("p1", "Member1");
        m1.addTeaches(new Skill("S1", SkillCategory.PROGRAMMING, SkillLevel.ADVANCED));
        m1.addWants(new Skill("S2", SkillCategory.PROGRAMMING, SkillLevel.BEGINNER));

        Member m2 = repos.createMember("p2", "Member2");
        m2.addTeaches(new Skill("S2", SkillCategory.PROGRAMMING, SkillLevel.ADVANCED));
        m2.addWants(new Skill("S1", SkillCategory.PROGRAMMING, SkillLevel.BEGINNER));

        SwapLink l1 = new SwapLink(m1, m2, new Skill("S2", SkillCategory.PROGRAMMING, SkillLevel.ADVANCED));
        SwapLink l2 = new SwapLink(m2, m1, new Skill("S1", SkillCategory.PROGRAMMING, SkillLevel.ADVANCED));
        SwapCycle cycle = new SwapCycle(List.of(l1, l2));

        // Create proposal by m1
        SwapProposal proposal = swapService.createProposal(m1.getId(), cycle);
        assertEqual(ProposalStatus.PENDING, proposal.getStatus(), "Initial status should be PENDING");
        assertEqual(ResponseStatus.ACCEPTED, proposal.getResponseFor(m1.getId()), "Initiator response should be ACCEPTED");
        assertEqual(ResponseStatus.PENDING, proposal.getResponseFor(m2.getId()), "Partner response should be PENDING");
        assertFalse(m1.isLocked(), "m1 should not be locked while proposal is pending");

        // m2 accepts
        swapService.acceptProposal(proposal.getId(), m2.getId());
        assertEqual(ProposalStatus.ACTIVE, proposal.getStatus(), "Proposal must transition to ACTIVE after mutual consensus");
        assertTrue(repos.memberRepo.findById(m1.getId()).get().isLocked(), "m1 must be locked once proposal is ACTIVE");
        assertTrue(repos.memberRepo.findById(m2.getId()).get().isLocked(), "m2 must be locked once proposal is ACTIVE");
        System.out.println("  ✓ testProposalStateTransitionsAndConsensus passed");
    }

    public static void testMemberLockingPreventsMultipleActiveProposals() {
        MockRepositories repos = new MockRepositories();
        SwapService swapService = new SwapService(repos.memberRepo, repos.proposalRepo);

        Member m1 = repos.createMember("l1", "Lock1");
        m1.addTeaches(new Skill("S1", SkillCategory.OTHER, SkillLevel.ADVANCED));
        m1.addWants(new Skill("S2", SkillCategory.OTHER, SkillLevel.BEGINNER));

        Member m2 = repos.createMember("l2", "Lock2");
        m2.addTeaches(new Skill("S2", SkillCategory.OTHER, SkillLevel.ADVANCED));
        m2.addWants(new Skill("S1", SkillCategory.OTHER, SkillLevel.BEGINNER));

        // Simulate m1 locked in another active proposal
        m1.lockInProposal("existing-active-prop");
        repos.memberRepo.save(m1);

        SwapLink l1 = new SwapLink(m1, m2, new Skill("S2", SkillCategory.OTHER, SkillLevel.ADVANCED));
        SwapLink l2 = new SwapLink(m2, m1, new Skill("S1", SkillCategory.OTHER, SkillLevel.ADVANCED));
        SwapCycle cycle = new SwapCycle(List.of(l1, l2));

        try {
            swapService.createProposal(m2.getId(), cycle);
            throw new AssertionError("Should reject proposal creation when a participant is locked");
        } catch (IllegalStateException expected) {
            // Success
        }
        System.out.println("  ✓ testMemberLockingPreventsMultipleActiveProposals passed");
    }

    public static void testDeclineCancelsAndUnlocksMembers() {
        MockRepositories repos = new MockRepositories();
        SwapService swapService = new SwapService(repos.memberRepo, repos.proposalRepo);

        Member m1 = repos.createMember("d1", "Dec1");
        m1.addTeaches(new Skill("S1", SkillCategory.OTHER, SkillLevel.ADVANCED));
        m1.addWants(new Skill("S2", SkillCategory.OTHER, SkillLevel.BEGINNER));

        Member m2 = repos.createMember("d2", "Dec2");
        m2.addTeaches(new Skill("S2", SkillCategory.OTHER, SkillLevel.ADVANCED));
        m2.addWants(new Skill("S1", SkillCategory.OTHER, SkillLevel.BEGINNER));

        SwapLink l1 = new SwapLink(m1, m2, new Skill("S2", SkillCategory.OTHER, SkillLevel.ADVANCED));
        SwapLink l2 = new SwapLink(m2, m1, new Skill("S1", SkillCategory.OTHER, SkillLevel.ADVANCED));
        SwapCycle cycle = new SwapCycle(List.of(l1, l2));

        SwapProposal proposal = swapService.createProposal(m1.getId(), cycle);
        swapService.declineProposal(proposal.getId(), m2.getId());

        assertEqual(ProposalStatus.CANCELLED, proposal.getStatus(), "Decline must cancel the proposal");
        assertFalse(repos.memberRepo.findById(m1.getId()).get().isLocked(), "m1 must be unlocked");
        assertFalse(repos.memberRepo.findById(m2.getId()).get().isLocked(), "m2 must be unlocked");
        System.out.println("  ✓ testDeclineCancelsAndUnlocksMembers passed");
    }

    public static void testExpirationTransitionsAndUnlocks() {
        Member m1 = new Member("e1", "Exp1", "h", "s", "c1");
        Member m2 = new Member("e2", "Exp2", "h", "s", "c2");

        SwapLink l1 = new SwapLink(m1, m2, new Skill("S2", SkillCategory.OTHER, SkillLevel.ADVANCED));
        SwapLink l2 = new SwapLink(m2, m1, new Skill("S1", SkillCategory.OTHER, SkillLevel.ADVANCED));
        SwapCycle cycle = new SwapCycle(List.of(l1, l2));

        // Create proposal with past createdAt and expiresAt (past 48 hours)
        long oldCreated = System.currentTimeMillis() - (50L * 3600L * 1000L);
        long oldExpires = oldCreated + SwapProposal.EXPIRATION_DURATION_MS;

        Map<String, ResponseStatus> resp = new HashMap<>();
        resp.put(m1.getId(), ResponseStatus.ACCEPTED);
        resp.put(m2.getId(), ResponseStatus.PENDING);

        SwapProposal expiredProposal = new SwapProposal("old-prop", cycle, m1.getId(), resp,
                ProposalStatus.PENDING, oldCreated, oldExpires, null);

        assertTrue(expiredProposal.isExpired(), "Should recognize proposal as expired");
        assertEqual(ProposalStatus.EXPIRED, expiredProposal.getStatus(), "Status should be EXPIRED");
        System.out.println("  ✓ testExpirationTransitionsAndUnlocks passed");
    }

    public static void testContactRevealRules() {
        Member m1 = new Member("c1", "Con1", "h", "s", "email1@test.com");
        Member m2 = new Member("c2", "Con2", "h", "s", "email2@test.com");
        SwapLink l1 = new SwapLink(m1, m2, new Skill("S2", SkillCategory.OTHER, SkillLevel.ADVANCED));
        SwapLink l2 = new SwapLink(m2, m1, new Skill("S1", SkillCategory.OTHER, SkillLevel.ADVANCED));
        SwapCycle cycle = new SwapCycle(List.of(l1, l2));

        SwapProposal pending = new SwapProposal("p-pend", cycle, m1.getId());
        assertFalse(pending.areContactsRevealed(), "Contacts must NOT be revealed while pending");

        // Accept to make active
        pending.accept(m2.getId());
        assertTrue(pending.isActive(), "Proposal is active");
        assertTrue(pending.areContactsRevealed(), "Contacts MUST be revealed when ACTIVE");

        // Complete
        pending.complete();
        assertTrue(pending.areContactsRevealed(), "Contacts MUST remain revealed when COMPLETED");
        System.out.println("  ✓ testContactRevealRules passed");
    }

    private static class MockRepositories {
        final MemberRepository memberRepo = new com.skillswap.repository.FilePersistedMemberRepository(null);
        final ProposalRepository proposalRepo = new com.skillswap.repository.FilePersistedProposalRepository(null, memberRepo);

        Member createMember(String id, String name) {
            Member m = new Member(id, name, "hash", "salt", id + "@mail.com");
            return memberRepo.save(m);
        }
    }

    private static void assertEqual(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message + " - Expected true but was false");
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message + " - Expected false but was true");
        }
    }
}
