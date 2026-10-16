package com.skillswap.finder;

import com.skillswap.model.*;

import java.util.*;

/**
 * Discovers barter swap cycles of length 2 up to N (default 4) using depth-limited DFS.
 * Normalizes rotational duplicates, respects skill level compatibility, and excludes
 * locked members.
 */
public class CycleSwapFinder implements SwapFinder {
    public static final int DEFAULT_MAX_CYCLE_LENGTH = 4;

    private final int maxCycleLength;

    public CycleSwapFinder() {
        this(DEFAULT_MAX_CYCLE_LENGTH);
    }

    public CycleSwapFinder(int maxCycleLength) {
        if (maxCycleLength < 2) {
            throw new IllegalArgumentException("Max cycle length must be at least 2");
        }
        this.maxCycleLength = maxCycleLength;
    }

    public int getMaxCycleLength() {
        return maxCycleLength;
    }

    @Override
    public List<SwapCycle> find(Member start, List<Member> allMembers) {
        if (start == null || allMembers == null || start.isLocked()) {
            return Collections.emptyList();
        }

        // Build adjacency map for fast DFS traversal: learner -> list of outgoing links (learner learns from teacher)
        Map<String, List<SwapLink>> outgoingLinks = buildAdjacencyGraph(allMembers);

        Set<String> seenCanonicalSignatures = new HashSet<>();
        List<SwapCycle> discoveredCycles = new ArrayList<>();

        List<Member> memberPath = new ArrayList<>();
        List<SwapLink> linkPath = new ArrayList<>();
        Set<String> visitedMemberIds = new HashSet<>();

        memberPath.add(start);
        visitedMemberIds.add(start.getId());

        dfs(start, start, outgoingLinks, memberPath, linkPath, visitedMemberIds,
            seenCanonicalSignatures, discoveredCycles);

        return discoveredCycles;
    }

    private void dfs(Member startMember,
                     Member currentMember,
                     Map<String, List<SwapLink>> graph,
                     List<Member> memberPath,
                     List<SwapLink> linkPath,
                     Set<String> visitedMemberIds,
                     Set<String> seenSignatures,
                     List<SwapCycle> results) {

        List<SwapLink> edges = graph.getOrDefault(currentMember.getId(), Collections.emptyList());

        for (SwapLink edge : edges) {
            Member teacher = edge.getTeacher();

            // Case 1: Cycle closure back to start member
            if (teacher.getId().equalsIgnoreCase(startMember.getId())) {
                if (linkPath.size() + 1 >= 2 && linkPath.size() + 1 <= maxCycleLength) {
                    List<SwapLink> fullCycle = new ArrayList<>(linkPath);
                    fullCycle.add(edge);

                    SwapCycle cycle = new SwapCycle(fullCycle);
                    String sig = cycle.getCanonicalSignature();
                    if (seenSignatures.add(sig)) {
                        results.add(cycle);
                    }
                }
            }
            // Case 2: Extend path to an unvisited member if depth allows
            else if (memberPath.size() < maxCycleLength &&
                     !visitedMemberIds.contains(teacher.getId()) &&
                     !teacher.isLocked()) {

                visitedMemberIds.add(teacher.getId());
                memberPath.add(teacher);
                linkPath.add(edge);

                dfs(startMember, teacher, graph, memberPath, linkPath, visitedMemberIds,
                    seenSignatures, results);

                // Backtrack
                linkPath.remove(linkPath.size() - 1);
                memberPath.remove(memberPath.size() - 1);
                visitedMemberIds.remove(teacher.getId());
            }
        }
    }

    /**
     * Builds directed edges: edge from learner to teacher exists if
     * teacher can teach a skill that learner wants with level compatibility.
     */
    private Map<String, List<SwapLink>> buildAdjacencyGraph(List<Member> members) {
        Map<String, List<SwapLink>> graph = new HashMap<>();
        for (Member m : members) {
            graph.put(m.getId(), new ArrayList<>());
        }

        for (Member learner : members) {
            if (learner.isLocked()) continue;

            for (Member teacher : members) {
                if (teacher.equals(learner) || teacher.isLocked()) continue;

                for (Skill wanted : learner.getWants()) {
                    Skill offered = teacher.findMatchingTeaches(wanted);
                    if (offered != null) {
                        graph.get(learner.getId()).add(new SwapLink(learner, teacher, offered));
                    }
                }
            }
        }
        return graph;
    }
}
