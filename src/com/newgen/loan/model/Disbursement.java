package com.newgen.loan.model;

import java.time.LocalDateTime;

/**
 * Module 1 - Model Layer
 * Represents the disbursement record for an approved loan.
 * Maps to the 'disbursements' table in the database.
 *
 * A disbursement record is created when an application moves from
 * CREDIT_APPROVAL → DISBURSEMENT stage.
 * Duplicate disbursement attempts must be blocked (unique constraint on applicationId).
 */
public class Disbursement {

    // ─────────────────────────────────────────────────────────────────────────
    // Fields
    // ─────────────────────────────────────────────────────────────────────────

    private int disbursementId;
    private int applicationId;        // Foreign key → applications (unique — one disbursement per app)
    private String referenceNumber;   // Unique disbursement reference (e.g., DISB-20241124-001)
    private double disbursedAmount;   // Actual amount disbursed (= approved amount)
    private LocalDateTime disbursedAt;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────

    /** Default no-arg constructor */
    public Disbursement() {}

    /** Full constructor */
    public Disbursement(int disbursementId, int applicationId, String referenceNumber,
                        double disbursedAmount, LocalDateTime disbursedAt) {
        this.disbursementId = disbursementId;
        this.applicationId = applicationId;
        this.referenceNumber = referenceNumber;
        this.disbursedAmount = disbursedAmount;
        this.disbursedAt = disbursedAt;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters & Setters
    // ─────────────────────────────────────────────────────────────────────────

    public int getDisbursementId() { return disbursementId; }
    public void setDisbursementId(int disbursementId) { this.disbursementId = disbursementId; }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public double getDisbursedAmount() { return disbursedAmount; }
    public void setDisbursedAmount(double disbursedAmount) { this.disbursedAmount = disbursedAmount; }

    public LocalDateTime getDisbursedAt() { return disbursedAt; }
    public void setDisbursedAt(LocalDateTime disbursedAt) { this.disbursedAt = disbursedAt; }

    // ─────────────────────────────────────────────────────────────────────────
    // toString
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Disbursement{" +
                "disbursementId=" + disbursementId +
                ", applicationId=" + applicationId +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", disbursedAmount=" + disbursedAmount +
                ", disbursedAt=" + disbursedAt +
                '}';
    }
}
