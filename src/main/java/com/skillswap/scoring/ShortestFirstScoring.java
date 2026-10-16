package com.skillswap.scoring;

import com.skillswap.model.SwapCycle;

/**
 * Prioritizes shorter swap cycles over longer ones, as direct 2-way and 3-way
 * exchanges have significantly higher coordination success rates and lower scheduling friction.
 */
public class ShortestFirstScoring implements ScoringStrategy {

    @Override
    public double score(SwapCycle cycle) {
        if (cycle == null || cycle.size() == 0) {
            return 0.0;
        }
        int len = cycle.size();
        return switch (len) {
            case 2 -> 1.0;
            case 3 -> 0.80;
            case 4 -> 0.60;
            default -> Math.max(0.2, 2.0 / len);
        };
    }
}
