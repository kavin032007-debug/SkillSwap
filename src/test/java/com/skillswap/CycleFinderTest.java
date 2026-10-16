package com.skillswap;

import com.skillswap.finder.CycleSwapFinder;
import com.skillswap.finder.DirectSwapFinder;
import com.skillswap.model.*;

import java.util.List;

/**
 * Unit tests for barter cycle detection, level rules, and deduplication.
 */
public class CycleFinderTest {

    public static void runAllTests() {
        System.out.println("--- Running CycleFinderTest ---");
        testDirect2WaySwap();
        testCyclic3WaySwap();
        testNoMatchScenario();
        testRotationDeduplication();
        testLevelCompatibilityRules();
        System.out.println("✓ All CycleFinderTest tests passed!\n");
    }

    public static void testDirect2WaySwap() {
        Member alice = new Member("m1", "Alice", "h", "s", "alice@test.com");
        alice.addTeaches(new Skill("Python", SkillCategory.PROGRAMMING, SkillLevel.ADVANCED));
        alice.addWants(new Skill("Guitar", SkillCategory.MUSIC, SkillLevel.BEGINNER));

        Member bob = new Member("m2", "Bob", "h", "s", "bob@test.com");
        bob.addTeaches(new Skill("Guitar", SkillCategory.MUSIC, SkillLevel.INTERMEDIATE));
        bob.addWants(new Skill("Python", SkillCategory.PROGRAMMING, SkillLevel.BEGINNER));

        DirectSwapFinder directFinder = new DirectSwapFinder();
        List<SwapCycle> directCycles = directFinder.find(alice, List.of(alice, bob));
        assertEqual(1, directCycles.size(), "Should discover exactly 1 direct 2-way swap");
        assertEqual(2, directCycles.get(0).size(), "Cycle size should be 2");

        CycleSwapFinder cycleFinder = new CycleSwapFinder(4);
        List<SwapCycle> allCycles = cycleFinder.find(alice, List.of(alice, bob));
        assertEqual(1, allCycles.size(), "CycleFinder should also find the 2-way swap");
        System.out.println("  ✓ testDirect2WaySwap passed");
    }

    public static void testCyclic3WaySwap() {
        // Asha -> Ravi -> Meena -> Asha
        Member asha = new Member("m-asha", "Asha", "h", "s", "a@test.com");
        asha.addTeaches(new Skill("Guitar", SkillCategory.MUSIC, SkillLevel.ADVANCED));
        asha.addWants(new Skill("Excel", SkillCategory.BUSINESS, SkillLevel.BEGINNER));

        Member ravi = new Member("m-ravi", "Ravi", "h", "s", "r@test.com");
        ravi.addTeaches(new Skill("Excel", SkillCategory.BUSINESS, SkillLevel.ADVANCED));
        ravi.addWants(new Skill("Photography", SkillCategory.DESIGN, SkillLevel.BEGINNER));

        Member meena = new Member("m-meena", "Meena", "h", "s", "m@test.com");
        meena.addTeaches(new Skill("Photography", SkillCategory.DESIGN, SkillLevel.ADVANCED));
        meena.addWants(new Skill("Guitar", SkillCategory.MUSIC, SkillLevel.BEGINNER));

        CycleSwapFinder finder = new CycleSwapFinder(4);
        List<SwapCycle> cycles = finder.find(asha, List.of(asha, ravi, meena));

        assertEqual(1, cycles.size(), "Should find exactly 1 3-way cycle for Asha");
        SwapCycle cycle = cycles.get(0);
        assertEqual(3, cycle.size(), "Cycle size must be 3");
        assertTrue(cycle.containsMember(asha), "Cycle contains Asha");
        assertTrue(cycle.containsMember(ravi), "Cycle contains Ravi");
        assertTrue(cycle.containsMember(meena), "Cycle contains Meena");
        System.out.println("  ✓ testCyclic3WaySwap passed");
    }

    public static void testNoMatchScenario() {
        Member alice = new Member("m1", "Alice", "h", "s", "alice@test.com");
        alice.addTeaches(new Skill("Python", SkillCategory.PROGRAMMING, SkillLevel.ADVANCED));
        alice.addWants(new Skill("Guitar", SkillCategory.MUSIC, SkillLevel.BEGINNER));

        Member bob = new Member("m2", "Bob", "h", "s", "bob@test.com");
        bob.addTeaches(new Skill("French", SkillCategory.LANGUAGE, SkillLevel.ADVANCED));
        bob.addWants(new Skill("Chess", SkillCategory.LIFESTYLE, SkillLevel.BEGINNER));

        CycleSwapFinder finder = new CycleSwapFinder(4);
        List<SwapCycle> cycles = finder.find(alice, List.of(alice, bob));
        assertEqual(0, cycles.size(), "Disjoint skills must yield 0 swap cycles");
        System.out.println("  ✓ testNoMatchScenario passed");
    }

    public static void testRotationDeduplication() {
        Member a = new Member("a", "A", "h", "s", "a@test.com");
        a.addTeaches(new Skill("S1", SkillCategory.OTHER, SkillLevel.ADVANCED));
        a.addWants(new Skill("S3", SkillCategory.OTHER, SkillLevel.BEGINNER));

        Member b = new Member("b", "B", "h", "s", "b@test.com");
        b.addTeaches(new Skill("S2", SkillCategory.OTHER, SkillLevel.ADVANCED));
        b.addWants(new Skill("S1", SkillCategory.OTHER, SkillLevel.BEGINNER));

        Member c = new Member("c", "C", "h", "s", "c@test.com");
        c.addTeaches(new Skill("S3", SkillCategory.OTHER, SkillLevel.ADVANCED));
        c.addWants(new Skill("S2", SkillCategory.OTHER, SkillLevel.BEGINNER));

        // Create cycle starting from A: A learns S3 from C, C learns S2 from B, B learns S1 from A
        SwapLink l1 = new SwapLink(a, c, new Skill("S3", SkillCategory.OTHER, SkillLevel.ADVANCED));
        SwapLink l2 = new SwapLink(c, b, new Skill("S2", SkillCategory.OTHER, SkillLevel.ADVANCED));
        SwapLink l3 = new SwapLink(b, a, new Skill("S1", SkillCategory.OTHER, SkillLevel.ADVANCED));
        SwapCycle cycle1 = new SwapCycle(List.of(l1, l2, l3));

        // Create rotated cycle starting from B: B learns S1 from A, A learns S3 from C, C learns S2 from B
        SwapCycle cycle2 = new SwapCycle(List.of(l3, l1, l2));

        assertEqual(cycle1.getCanonicalSignature(), cycle2.getCanonicalSignature(),
                "Rotations of the same cycle must have identical canonical signatures");
        assertEqual(cycle1, cycle2, "Cycles must equal each other despite rotation");
        System.out.println("  ✓ testRotationDeduplication passed");
    }

    public static void testLevelCompatibilityRules() {
        // Beginner learner needs Intermediate or Advanced teacher
        assertTrue(SkillLevel.isCompatible(SkillLevel.BEGINNER, SkillLevel.INTERMEDIATE), "Beginner learner with Intermediate teacher is compatible");
        assertTrue(SkillLevel.isCompatible(SkillLevel.BEGINNER, SkillLevel.ADVANCED), "Beginner learner with Advanced teacher is compatible");
        assertFalse(SkillLevel.isCompatible(SkillLevel.BEGINNER, SkillLevel.BEGINNER), "Beginner learner with Beginner teacher is NOT compatible");

        // Intermediate learner needs Intermediate or Advanced teacher
        assertTrue(SkillLevel.isCompatible(SkillLevel.INTERMEDIATE, SkillLevel.INTERMEDIATE), "Intermediate with Intermediate is compatible");
        assertTrue(SkillLevel.isCompatible(SkillLevel.INTERMEDIATE, SkillLevel.ADVANCED), "Intermediate with Advanced is compatible");
        assertFalse(SkillLevel.isCompatible(SkillLevel.INTERMEDIATE, SkillLevel.BEGINNER), "Intermediate with Beginner is NOT compatible");

        // Advanced learner needs Advanced teacher
        assertTrue(SkillLevel.isCompatible(SkillLevel.ADVANCED, SkillLevel.ADVANCED), "Advanced with Advanced is compatible");
        assertFalse(SkillLevel.isCompatible(SkillLevel.ADVANCED, SkillLevel.INTERMEDIATE), "Advanced with Intermediate is NOT compatible");
        assertFalse(SkillLevel.isCompatible(SkillLevel.ADVANCED, SkillLevel.BEGINNER), "Advanced with Beginner is NOT compatible");
        System.out.println("  ✓ testLevelCompatibilityRules passed");
    }

    private static void assertEqual(Object expected, Object actual, String message) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message + " - Expected true but was false");
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message + " - Expected false but was true");
        }
    }
}
