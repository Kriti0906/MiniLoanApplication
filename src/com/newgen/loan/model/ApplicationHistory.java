package com.newgen.loan.model;

import java.time.LocalDateTime;

/**
 * Module 1 - Model Layer
 * Represents a single audit trail entry for an application's status change.
 * Maps to the 'application_history' table in the database.
 *
 * Every time an application moves from one status to another,
 * a history record is inserted — this gives a full audit trail.
 */
public class ApplicationHistory {

    // ─────────────────────────────────────────────────────────────────────────
    // Fields
    // ─────────────────────────────────────────────────────────────────────────

    private int historyId;
    private int applicationId;             // Foreign key → applications table
    private String action;                 // Human-readable action label (e.g., "SUBMITTED", "APPROVED")
    private String oldStatus;             // Status before this transition
    private String newStatus;             // Status after this transition
    private String performedBy;           // User/system that triggered the transition
    private LocalDateTime performedAt;    // When the transition happened
    private String remarks;               // Optional notes (e.g., rejection reason echo)

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────

    /** Default no-arg constructor */
    public ApplicationHistory() {}

    /** Full constructor */
    public ApplicationHistory(int historyId, int applicationId, String action,
                               String oldStatus, String newStatus,
                               String performedBy, LocalDateTime performedAt,
                               String remarks) {
        this.historyId = historyId;
        this.applicationId = applicationId;
        this.action = action;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.performedBy = performedBy;
        this.performedAt = performedAt;
        this.remarks = remarks;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters & Setters
    // ─────────────────────────────────────────────────────────────────────────

    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    // ─────────────────────────────────────────────────────────────────────────
    // toString
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "ApplicationHistory{" +
                "historyId=" + historyId +
                ", applicationId=" + applicationId +
                ", action='" + action + '\'' +
                ", oldStatus='" + oldStatus + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", performedBy='" + performedBy + '\'' +
                ", performedAt=" + performedAt +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}
