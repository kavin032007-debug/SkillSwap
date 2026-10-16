package com.skillswap;

import com.skillswap.model.*;
import com.skillswap.scoring.*;

import java.util.List;

/**
 * Unit tests for cycle scoring strategies (ShortestFirst, Availability, Rating, Composite).
 */
public class ScoringStrategyTest {

    public static void runAllTests() {
        System.out.println("--- Running ScoringStrategyTest ---");
        testShortestFirstScoring();
        testRatingScoring();
        testAvailabilityScoring();
        testCompositeScoring();
        System.out.println("✓ All ScoringStrategyTest tests passed!\n");
    }

    public static void testShortestFirstScoring() {
        ShortestFirstScoring scoring = new ShortestFirstScoring();

        SwapCycle cycle2 = createDummyCycle(2);
        SwapCycle cycle3 = createDummyCycle(3);
        SwapCycle cycle4 = createDummyCycle(4);

        assertTrue(scoring.score(cycle2) > scoring.score(cycle3), "2-way cycle must score higher than 3-way");
        assertTrue(scoring.score(cycle3) > scoring.score(cycle4), "3-way cycle must score higher than 4-way");
        System.out.println("  ✓ testShortestFirstScoring passed");
    }

    public static void testRatingScoring() {
        RatingScoring scoring = new RatingScoring();

        Member high = new Member("h", "High", "h", "s", "c");
        high.addRating(new Rating("other", "Tester", 5, "Superb", 0));

        Member low = new Member("l", "Low", "h", "s", "c");
        low.addRating(new Rating("other", "Tester", 2, "Average", 0));

        SwapCycle highCycle = createCycleWithMembers(List.of(high, high));
        SwapCycle lowCycle = createCycleWithMembers(List.of(low, low));

        assertTrue(scoring.score(highCycle) > scoring.score(lowCycle),
                "Cycle with higher teacher ratings must score higher");
        System.out.println("  ✓ testRatingScoring passed");
    }

    public static void testAvailabilityScoring() {
        AvailabilityScoring scoring = new AvailabilityScoring();

        Member busy = new Member("b", "Busy", "h", "s", "c");
        busy.setWeeklyAvailabilityHours(1);

        Member open = new Member("o", "Open", "h", "s", "c");
        open.setWeeklyAvailabilityHours(8);

        SwapCycle openCycle = createCycleWithMembers(List.of(open, open));
        SwapCycle busyCycle = createCycleWithMembers(List.of(busy, open));

        assertTrue(scoring.score(openCycle) > scoring.score(busyCycle),
                "Cycle with higher availability must score higher");
        System.out.println("  ✓ testAvailabilityScoring passed");
    }

    public static void testCompositeScoring() {
        CompositeScoring composite = CompositeScoring.defaultStrategy();
        SwapCycle cycle = createDummyCycle(2);
        double s = composite.score(cycle);

        assertTrue(s > 0.0 && s <= 1.0, "Composite score must be between 0.0 and 1.0");
        System.out.println("  ✓ testCompositeScoring passed");
    }

    private static SwapCycle createDummyCycle(int length) {
        List<Member> members = new java.util.ArrayList<>();
        for (int i = 0; i < length; i++) {
            members.add(new Member("m" + i, "Member" + i, "h", "s", "c"));
        }
        return createCycleWithMembers(members);
    }

    private static SwapCycle createCycleWithMembers(List<Member> members) {
        int n = members.size();
        List<SwapLink> links = new java.util.ArrayList<>();
        Skill dummySkill = new Skill("General", SkillCategory.OTHER, SkillLevel.INTERMEDIATE);
        for (int i = 0; i < n; i++) {
            links.add(new SwapLink(members.get(i), members.get((i + 1) % n), dummySkill));
        }
        return new SwapCycle(links);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message + " - Expected true but was false");
        }
    }
}
