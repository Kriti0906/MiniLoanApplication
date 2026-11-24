package com.newgen.loan.model;

import java.time.LocalDateTime;

/**
 * Module 1 - Model Layer
 * Represents a loan application.
 * Maps to the 'applications' table in the database.
 *
 * Product types: PERSONAL, EDUCATION
 * Status values (State Machine):
 *   DRAFT → UNDER_REVIEW → CREDIT_APPROVAL → DISBURSEMENT → CORE_BANKING_PUSH → EXIT
 *                        ↘ REJECTED         ↗ REJECTED
 */
public class Application {

    // ─────────────────────────────────────────────────────────────────────────
    // Inner Enum: Product Type
    // ─────────────────────────────────────────────────────────────────────────

    public enum ProductType {
        PERSONAL,
        EDUCATION
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner Enum: Application Status (State Machine stages)
    // ─────────────────────────────────────────────────────────────────────────

    public enum Status {
        DRAFT,
        UNDER_REVIEW,
        CREDIT_APPROVAL,
        REJECTED,
        DISBURSEMENT,
        CORE_BANKING_PUSH,
        EXIT
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fields
    // ─────────────────────────────────────────────────────────────────────────

    private int applicationId;
    private int customerId;              // Foreign key → customers table
    private ProductType productType;     // PERSONAL or EDUCATION
    private double requestedAmount;      // Amount requested by applicant
    private int tenureMonths;            // Repayment period in months
    private double annualInterestRate;   // Interest rate (e.g., 12.0 for 12%)
    private double emi;                  // Calculated EMI (computed in service layer)
    private double foir;                 // Calculated FOIR % (computed in service layer)
    private Status status;               // Current stage in the lifecycle
    private String branchCode;           // Branch where application was filed
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime decidedAt;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────

    /** Default no-arg constructor */
    public Application() {}

    /** Full constructor — used when loading from DB */
    public Application(int applicationId, int customerId, ProductType productType,
                       double requestedAmount, int tenureMonths, double annualInterestRate,
                       double emi, double foir, Status status, String branchCode,
                       LocalDateTime createdAt, LocalDateTime submittedAt, LocalDateTime decidedAt) {
        this.applicationId = applicationId;
        this.customerId = customerId;
        this.productType = productType;
        this.requestedAmount = requestedAmount;
        this.tenureMonths = tenureMonths;
        this.annualInterestRate = annualInterestRate;
        this.emi = emi;
        this.foir = foir;
        this.status = status;
        this.branchCode = branchCode;
        this.createdAt = createdAt;
        this.submittedAt = submittedAt;
        this.decidedAt = decidedAt;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters & Setters
    // ─────────────────────────────────────────────────────────────────────────

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public ProductType getProductType() { return productType; }
    public void setProductType(ProductType productType) { this.productType = productType; }

    public double getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(double requestedAmount) { this.requestedAmount = requestedAmount; }

    public int getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(int tenureMonths) { this.tenureMonths = tenureMonths; }

    public double getAnnualInterestRate() { return annualInterestRate; }
    public void setAnnualInterestRate(double annualInterestRate) { this.annualInterestRate = annualInterestRate; }

    public double getEmi() { return emi; }
    public void setEmi(double emi) { this.emi = emi; }

    public double getFoir() { return foir; }
    public void setFoir(double foir) { this.foir = foir; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }

    // ─────────────────────────────────────────────────────────────────────────
    // toString
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Application{" +
                "applicationId=" + applicationId +
                ", customerId=" + customerId +
                ", productType=" + productType +
                ", requestedAmount=" + requestedAmount +
                ", tenureMonths=" + tenureMonths +
                ", annualInterestRate=" + annualInterestRate +
                ", emi=" + emi +
                ", foir=" + foir +
                ", status=" + status +
                ", branchCode='" + branchCode + '\'' +
                ", createdAt=" + createdAt +
                ", submittedAt=" + submittedAt +
                ", decidedAt=" + decidedAt +
                '}';
    }
}
