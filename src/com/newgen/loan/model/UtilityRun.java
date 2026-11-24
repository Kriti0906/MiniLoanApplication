package com.newgen.loan.model;

import java.time.LocalDateTime;

/**
 * Module 1 - Model Layer
 * Represents one execution run of the background utility.
 * Maps to the 'utility_runs' table in the database.
 *
 * Every time the OverdueAlertUtility or CoreBankingPushUtility runs,
 * it logs a row here — this helps track when utilities ran, what they did,
 * and if any errors occurred.
 */
public class UtilityRun {

    // ─────────────────────────────────────────────────────────────────────────
    // Fields
    // ─────────────────────────────────────────────────────────────────────────

    private int runId;
    private String utilityName;           // e.g., "OverdueAlertUtility", "CoreBankingPushUtility"
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String result;                // "SUCCESS" or "FAILURE"
    private int applicationsProcessed;    // Number of applications acted on this run
    private int alertsCreated;            // Alerts inserted (for OverdueAlertUtility)
    private String errorMessage;          // Populated only on FAILURE

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────

    /** Default no-arg constructor */
    public UtilityRun() {}

    /** Full constructor */
    public UtilityRun(int runId, String utilityName, LocalDateTime startedAt,
                      LocalDateTime completedAt, String result,
                      int applicationsProcessed, int alertsCreated, String errorMessage) {
        this.runId = runId;
        this.utilityName = utilityName;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.result = result;
        this.applicationsProcessed = applicationsProcessed;
        this.alertsCreated = alertsCreated;
        this.errorMessage = errorMessage;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters & Setters
    // ─────────────────────────────────────────────────────────────────────────

    public int getRunId() { return runId; }
    public void setRunId(int runId) { this.runId = runId; }

    public String getUtilityName() { return utilityName; }
    public void setUtilityName(String utilityName) { this.utilityName = utilityName; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public int getApplicationsProcessed() { return applicationsProcessed; }
    public void setApplicationsProcessed(int applicationsProcessed) { this.applicationsProcessed = applicationsProcessed; }

    public int getAlertsCreated() { return alertsCreated; }
    public void setAlertsCreated(int alertsCreated) { this.alertsCreated = alertsCreated; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    // ─────────────────────────────────────────────────────────────────────────
    // toString
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "UtilityRun{" +
                "runId=" + runId +
                ", utilityName='" + utilityName + '\'' +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                ", result='" + result + '\'' +
                ", applicationsProcessed=" + applicationsProcessed +
                ", alertsCreated=" + alertsCreated +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
