package com.skillswap.scoring;

import com.skillswap.model.SwapCycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Composite scoring strategy that aggregates multiple {@link ScoringStrategy} instances
 * using user-configurable weights.
 */
public class CompositeScoring implements ScoringStrategy {

    private record WeightedStrategy(ScoringStrategy strategy, double weight) {}

    private final List<WeightedStrategy> strategies = new ArrayList<>();

    public CompositeScoring() {}

    /**
     * Factory method creating a balanced default scoring strategy:
     * - 40% Shortest cycle preference
     * - 35% Member rating reputation
     * - 25% Time commitment & availability
     */
    public static CompositeScoring defaultStrategy() {
        CompositeScoring composite = new CompositeScoring();
        composite.addStrategy(new ShortestFirstScoring(), 0.40);
        composite.addStrategy(new RatingScoring(), 0.35);
        composite.addStrategy(new AvailabilityScoring(), 0.25);
        return composite;
    }

    public CompositeScoring addStrategy(ScoringStrategy strategy, double weight) {
        Objects.requireNonNull(strategy, "Strategy cannot be null");
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be greater than zero");
        }
        strategies.add(new WeightedStrategy(strategy, weight));
        return this;
    }

    @Override
    public double score(SwapCycle cycle) {
        if (cycle == null || strategies.isEmpty()) {
            return 0.0;
        }

        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;

        for (WeightedStrategy ws : strategies) {
            double s = ws.strategy().score(cycle);
            totalWeightedScore += s * ws.weight();
            totalWeight += ws.weight();
        }

        if (totalWeight <= 0) {
            return 0.0;
        }

        double finalScore = totalWeightedScore / totalWeight;
        return Math.round(finalScore * 100.0) / 100.0;
    }
}
