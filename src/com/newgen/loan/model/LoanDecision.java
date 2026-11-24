package com.newgen.loan.model;

import java.time.LocalDateTime;

/**
 * Module 1 - Model Layer
 * Represents a reviewer's decision on a loan application.
 * Maps to the 'loan_decisions' table in the database.
 *
 * A decision is created when an application moves from:
 *   UNDER_REVIEW  → CREDIT_APPROVAL (approved)
 *   UNDER_REVIEW  → REJECTED        (rejected)
 *   CREDIT_APPROVAL → DISBURSEMENT  (final approval)
 *   CREDIT_APPROVAL → REJECTED      (rejected)
 */
public class LoanDecision {

    // ─────────────────────────────────────────────────────────────────────────
    // Inner Enum: Decision Outcome
    // ─────────────────────────────────────────────────────────────────────────

    public enum DecisionOutcome {
        APPROVED,
        REJECTED
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fields
    // ─────────────────────────────────────────────────────────────────────────

    private int decisionId;
    private int applicationId;          // Foreign key → applications table
    private String reviewerCode;        // ID/code of the reviewer making the decision
    private DecisionOutcome outcome;    // APPROVED or REJECTED
    private String decisionReason;      // Mandatory when REJECTED; optional for APPROVED
    private LocalDateTime decidedAt;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────

    /** Default no-arg constructor */
    public LoanDecision() {}

    /** Full constructor */
    public LoanDecision(int decisionId, int applicationId, String reviewerCode,
                        DecisionOutcome outcome, String decisionReason,
                        LocalDateTime decidedAt) {
        this.decisionId = decisionId;
        this.applicationId = applicationId;
        this.reviewerCode = reviewerCode;
        this.outcome = outcome;
        this.decisionReason = decisionReason;
        this.decidedAt = decidedAt;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters & Setters
    // ─────────────────────────────────────────────────────────────────────────

    public int getDecisionId() { return decisionId; }
    public void setDecisionId(int decisionId) { this.decisionId = decisionId; }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public String getReviewerCode() { return reviewerCode; }
    public void setReviewerCode(String reviewerCode) { this.reviewerCode = reviewerCode; }

    public DecisionOutcome getOutcome() { return outcome; }
    public void setOutcome(DecisionOutcome outcome) { this.outcome = outcome; }

    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }

    // ─────────────────────────────────────────────────────────────────────────
    // toString
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "LoanDecision{" +
                "decisionId=" + decisionId +
                ", applicationId=" + applicationId +
                ", reviewerCode='" + reviewerCode + '\'' +
                ", outcome=" + outcome +
                ", decisionReason='" + decisionReason + '\'' +
                ", decidedAt=" + decidedAt +
                '}';
    }
}
