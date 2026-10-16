package com.skillswap;

/**
 * Standalone test suite runner for SkillSwap.
 * Can be executed via javac and java without any build tool or external library dependencies!
 */
public class SkillSwapTestRunner {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   ⚡ SkillSwap Automated Test Suite Runner ⚡   ");
        System.out.println("=================================================\n");

        long start = System.currentTimeMillis();
        int suitesPassed = 0;
        int totalSuites = 3;

        try {
            CycleFinderTest.runAllTests();
            suitesPassed++;

            ProposalLifecycleTest.runAllTests();
            suitesPassed++;

            ScoringStrategyTest.runAllTests();
            suitesPassed++;

            long elapsed = System.currentTimeMillis() - start;
            System.out.println("=================================================");
            System.out.println("   🎉 ALL " + suitesPassed + "/" + totalSuites + " TEST SUITES PASSED IN " + elapsed + "ms!   ");
            System.out.println("=================================================");
        } catch (Throwable t) {
            System.err.println("\n❌ TEST SUITE FAILED:");
            t.printStackTrace();
            System.exit(1);
        }
    }
}
