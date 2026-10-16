package com.skillswap.scoring;

import com.skillswap.model.SwapCycle;

/**
 * Strategy interface for calculating the desirability score of a barter swap cycle.
 */
public interface ScoringStrategy {

    /**
     * Calculates a numerical score for the given swap cycle (typically normalized between 0.0 and 1.0).
     *
     * @param cycle the swap cycle to evaluate
     * @return a score where higher values represent more favorable swap opportunities
     */
    double score(SwapCycle cycle);
}
