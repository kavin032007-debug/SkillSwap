package com.skillswap.finder;

import com.skillswap.model.*;

import java.util.*;

/**
 * Discovers direct 2-way swaps between two members:
 * Member A teaches Member B, and Member B teaches Member A.
 */
public class DirectSwapFinder implements SwapFinder {

    @Override
    public List<SwapCycle> find(Member start, List<Member> allMembers) {
        if (start == null || allMembers == null || start.isLocked()) {
            return Collections.emptyList();
        }

        Set<String> seenSignatures = new HashSet<>();
        List<SwapCycle> results = new ArrayList<>();

        for (Member partner : allMembers) {
            // Cannot match with yourself or locked members
            if (partner.equals(start) || partner.isLocked()) {
                continue;
            }

            // Check if partner can teach start something start wants
            List<SwapLink> startLearnsLinks = findDirectEdges(start, partner);
            if (startLearnsLinks.isEmpty()) {
                continue;
            }

            // Check if start can teach partner something partner wants
            List<SwapLink> partnerLearnsLinks = findDirectEdges(partner, start);
            if (partnerLearnsLinks.isEmpty()) {
                continue;
            }

            // Construct 2-way swap cycles for all matching combinations
            for (SwapLink link1 : startLearnsLinks) {
                for (SwapLink link2 : partnerLearnsLinks) {
                    List<SwapLink> cycleLinks = List.of(link1, link2);
                    SwapCycle cycle = new SwapCycle(cycleLinks);
                    String sig = cycle.getCanonicalSignature();
                    if (seenSignatures.add(sig)) {
                        results.add(cycle);
                    }
                }
            }
        }

        return results;
    }

    /**
     * Finds directed edges from learner to teacher where teacher teaches a skill learner wants.
     */
    static List<SwapLink> findDirectEdges(Member learner, Member teacher) {
        List<SwapLink> links = new ArrayList<>();
        for (Skill wanted : learner.getWants()) {
            Skill offered = teacher.findMatchingTeaches(wanted);
            if (offered != null) {
                links.add(new SwapLink(learner, teacher, offered));
            }
        }
        return links;
    }
}
