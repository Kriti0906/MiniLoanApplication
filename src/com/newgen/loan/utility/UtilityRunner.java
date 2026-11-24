package com.newgen.loan.utility;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Module 7 - Background Utility Layer
 * Manages ScheduledExecutorService background scheduler and immediate manual triggers.
 */
public class UtilityRunner {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static boolean isSchedulerStarted = false;

    private static final OverdueReviewUtility overdueUtility = new OverdueReviewUtility();
    private static final CoreDataPushUtility corePushUtility = new CoreDataPushUtility();

    /**
     * Starts the automated background schedulers:
     * - Overdue Review Utility runs periodically (e.g. daily interval).
     * - CoreDataPush Utility runs periodically to process newly disbursed loans.
     */
    public static synchronized void startBackgroundScheduler(long initialDelaySeconds, long periodSeconds) {
        if (!isSchedulerStarted) {
            scheduler.scheduleAtFixedRate(overdueUtility, initialDelaySeconds, periodSeconds, TimeUnit.SECONDS);
            scheduler.scheduleAtFixedRate(corePushUtility, initialDelaySeconds + 5, periodSeconds, TimeUnit.SECONDS);
            isSchedulerStarted = true;
            System.out.println("⏰ [Scheduler] Background utilities active. Running every " + periodSeconds + " seconds.");
        }
    }

    /**
     * Run Overdue Review Utility immediately on demand.
     */
    public static void runOverdueUtilityNow() {
        overdueUtility.executeUtility();
    }

    /**
     * Run CoreDataPush Utility immediately on demand.
     */
    public static void runCorePushUtilityNow() {
        corePushUtility.executeUtility();
    }

    public static synchronized void shutdownScheduler() {
        if (isSchedulerStarted) {
            scheduler.shutdown();
            isSchedulerStarted = false;
            System.out.println("🛑 [Scheduler] Background utilities shut down.");
        }
    }
}
