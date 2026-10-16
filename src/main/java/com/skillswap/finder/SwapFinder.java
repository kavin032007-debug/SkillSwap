package com.skillswap.finder;

import com.skillswap.model.Member;
import com.skillswap.model.SwapCycle;

import java.util.List;

/**
 * Strategy interface for discovering barter swap cycles for a given member.
 */
public interface SwapFinder {

    /**
     * Discovers swap cycles involving the specified start member among all candidate members.
     *
     * @param start the logged-in member seeking swap opportunities
     * @param allMembers the population of members in the system
     * @return a list of discovered swap cycles
     */
    List<SwapCycle> find(Member start, List<Member> allMembers);
}
