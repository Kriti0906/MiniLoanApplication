package com.newgen.loan.model;

import java.time.LocalDateTime;

/**
 * Module 1 - Model Layer
 * Represents a system alert raised by the background utility.
 * Maps to the 'system_alerts' table in the database.
 *
 * Key constraint: unique(application_id, alert_type)
 * This ensures the utility never creates duplicate alerts (idempotency).
 *
 * Alert types used:
 *   - OVERDUE_REVIEW  : Application in UNDER_REVIEW for more than 2 days
 *   - CORE_BANKING_PUSH_FAILED : Push to NG_CORE_BANKING failed
 */
public class SystemAlert {

    // ─────────────────────────────────────────────────────────────────────────
    // Fields
    // ─────────────────────────────────────────────────────────────────────────

    private int alertId;
    private int applicationId;      // Foreign key → applications table
    private String alertType;       // e.g., "OVERDUE_REVIEW", "CORE_BANKING_PUSH_FAILED"
    private LocalDateTime createdAt;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────

    /** Default no-arg constructor */
    public SystemAlert() {}

    /** Full constructor */
    public SystemAlert(int alertId, int applicationId, String alertType, LocalDateTime createdAt) {
        this.alertId = alertId;
        this.applicationId = applicationId;
        this.alertType = alertType;
        this.createdAt = createdAt;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters & Setters
    // ─────────────────────────────────────────────────────────────────────────

    public int getAlertId() { return alertId; }
    public void setAlertId(int alertId) { this.alertId = alertId; }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ─────────────────────────────────────────────────────────────────────────
    // toString
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "SystemAlert{" +
                "alertId=" + alertId +
                ", applicationId=" + applicationId +
                ", alertType='" + alertType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
