package com.newgen.loan.console;

import com.newgen.loan.utility.UtilityRunner;

/**
 * Module 7 - Console Layer
 * Utility control desk: Run utilities immediately on demand or configure background schedules.
 */
public class UtilityMenu {

    public void showMenu() {
        while (true) {
            ConsoleHelper.printHeader("⚙️ BACKGROUND UTILITY CONTROL DESK");
            System.out.println("1. Run Overdue Reviews Utility NOW (Check >2 days ageing & export CSV)");
            System.out.println("2. Run CoreDataPush Utility NOW (Push Disbursed cases to Core Banking & Move to EXIT)");
            System.out.println("3. Run Both Utilities NOW");
            System.out.println("4. Start Automated Background Scheduler");
            System.out.println("5. Back to Main Menu");

            int choice = ConsoleHelper.readInt("Select option (1-5)");
            switch (choice) {
                case 1 -> {
                    System.out.println("\nExecuting Overdue Review Utility...");
                    UtilityRunner.runOverdueUtilityNow();
                }
                case 2 -> {
                    System.out.println("\nExecuting CoreDataPush Utility...");
                    UtilityRunner.runCorePushUtilityNow();
                }
                case 3 -> {
                    System.out.println("\nExecuting All Background Utilities...");
                    UtilityRunner.runOverdueUtilityNow();
                    UtilityRunner.runCorePushUtilityNow();
                }
                case 4 -> {
                    long interval = ConsoleHelper.readInt("Enter background schedule interval in seconds (e.g. 60)");
                    UtilityRunner.startBackgroundScheduler(5, interval);
                }
                case 5 -> { return; }
                default -> System.out.println("❌ Invalid option. Try again.");
            }
        }
    }
}
