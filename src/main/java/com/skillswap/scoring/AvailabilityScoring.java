package com.skillswap.scoring;

import com.skillswap.model.Member;
import com.skillswap.model.SwapCycle;

import java.util.List;

/**
 * Scores swap cycles based on the weekly time commitment and availability
 * of participating members. Cycles with ample, well-matched availability score higher.
 */
public class AvailabilityScoring implements ScoringStrategy {
    private static final double TARGET_HOURS = 6.0;

    @Override
    public double score(SwapCycle cycle) {
        if (cycle == null || cycle.size() == 0) {
            return 0.0;
        }

        List<Member> members = cycle.getMembers();
        int minHours = Integer.MAX_VALUE;
        double sumHours = 0;

        for (Member m : members) {
            int h = m.getWeeklyAvailabilityHours();
            if (h < minHours) {
                minHours = h;
            }
            sumHours += h;
        }

        double avgHours = sumHours / members.size();

        // Bottleneck ratio (0.0 to 1.0)
        double bottleneckScore = Math.min(1.0, minHours / TARGET_HOURS);
        // Average commitment factor
        double avgScore = Math.min(1.0, avgHours / TARGET_HOURS);

        // Blended score weighting the bottleneck more heavily
        return (0.7 * bottleneckScore) + (0.3 * avgScore);
    }
}
