package com.skillswap.scoring;

import com.skillswap.model.Member;
import com.skillswap.model.SwapCycle;

import java.util.List;

/**
 * Scores swap cycles based on the peer review ratings and reputation of the teachers.
 * Highly rated mentors yield higher cycle reliability.
 */
public class RatingScoring implements ScoringStrategy {

    @Override
    public double score(SwapCycle cycle) {
        if (cycle == null || cycle.size() == 0) {
            return 0.0;
        }

        List<Member> members = cycle.getMembers();
        double sumRating = 0;
        for (Member m : members) {
            sumRating += m.getAverageRating();
        }

        double avgRating = sumRating / members.size();
        // Rating is between 1.0 and 5.0; normalize to 0.0 - 1.0
        return Math.max(0.0, Math.min(1.0, avgRating / 5.0));
    }
}
