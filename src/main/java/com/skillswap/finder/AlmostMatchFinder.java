package com.skillswap.finder;

import com.skillswap.model.*;

import java.util.*;

/**
 * Discovers high-potential "almost-matches" (Feature F7):
 * Recommends skills that, if added to a member's desired skills, would unlock new barter cycles.
 */
public class AlmostMatchFinder {

    public static class Opportunity {
        private final Skill suggestedSkill;
        private final int unlockedCyclesCount;
        private final List<SwapCycle> sampleCycles;

        public Opportunity(Skill suggestedSkill, int unlockedCyclesCount, List<SwapCycle> sampleCycles) {
            this.suggestedSkill = suggestedSkill;
            this.unlockedCyclesCount = unlockedCyclesCount;
            this.sampleCycles = sampleCycles != null ? sampleCycles : Collections.emptyList();
        }

        public Skill getSuggestedSkill() {
            return suggestedSkill;
        }

        public int getUnlockedCyclesCount() {
            return unlockedCyclesCount;
        }

        public List<SwapCycle> getSampleCycles() {
            return sampleCycles;
        }
    }

    private final CycleSwapFinder cycleFinder;

    public AlmostMatchFinder() {
        this(new CycleSwapFinder(3)); // Up to 3-way for almost-matches
    }

    public AlmostMatchFinder(CycleSwapFinder cycleFinder) {
        this.cycleFinder = cycleFinder;
    }

    /**
     * Finds opportunities where adding a single wanted skill opens up new cycles.
     */
    public List<Opportunity> findOpportunities(Member member, List<Member> allMembers, int maxSuggestions) {
        if (member == null || allMembers == null || allMembers.isEmpty()) {
            return Collections.emptyList();
        }

        // Collect all distinct skills taught across the community that the member neither teaches nor wants
        Set<Skill> candidateSkills = new LinkedHashSet<>();
        for (Member other : allMembers) {
            if (other.equals(member)) continue;
            for (Skill taught : other.getTeaches()) {
                boolean alreadyInvolved = false;
                for (Skill s : member.getWants()) {
                    if (s.sameSkillAs(taught)) {
                        alreadyInvolved = true;
                        break;
                    }
                }
                for (Skill s : member.getTeaches()) {
                    if (s.sameSkillAs(taught)) {
                        alreadyInvolved = true;
                        break;
                    }
                }
                if (!alreadyInvolved) {
                    // Test with beginner level by default
                    candidateSkills.add(new Skill(taught.getName(), taught.getCategory(), SkillLevel.BEGINNER));
                }
            }
        }

        // Base cycle count without any new skill
        List<SwapCycle> currentCycles = cycleFinder.find(member, allMembers);
        Set<String> currentSigs = new HashSet<>();
        for (SwapCycle c : currentCycles) {
            currentSigs.add(c.getCanonicalSignature());
        }

        List<Opportunity> opportunities = new ArrayList<>();

        for (Skill candidate : candidateSkills) {
            // Create a temporary clone/proxy of member with the candidate skill in wants
            Member tempMember = new Member(member.getId(), member.getName(), member.getPinHash(),
                    member.getPinSalt(), member.getContact());
            for (Skill t : member.getTeaches()) tempMember.addTeaches(t);
            for (Skill w : member.getWants()) tempMember.addWants(w);
            tempMember.addWants(candidate);

            List<Member> testMemberList = new ArrayList<>(allMembers);
            for (int i = 0; i < testMemberList.size(); i++) {
                if (testMemberList.get(i).equals(member)) {
                    testMemberList.set(i, tempMember);
                    break;
                }
            }

            List<SwapCycle> potentialCycles = cycleFinder.find(tempMember, testMemberList);
            List<SwapCycle> newCycles = new ArrayList<>();
            for (SwapCycle c : potentialCycles) {
                if (!currentSigs.contains(c.getCanonicalSignature())) {
                    newCycles.add(c);
                }
            }

            if (!newCycles.isEmpty()) {
                opportunities.add(new Opportunity(candidate, newCycles.size(),
                        newCycles.subList(0, Math.min(2, newCycles.size()))));
            }
        }

        // Rank by number of unlocked cycles descending
        opportunities.sort((a, b) -> Integer.compare(b.getUnlockedCyclesCount(), a.getUnlockedCyclesCount()));

        if (opportunities.size() > maxSuggestions) {
            return opportunities.subList(0, maxSuggestions);
        }
        return opportunities;
    }
}
