package com.newgen.loan.model;

import java.time.LocalDate;

/**
 * Module 1 - Model Layer
 * Represents a loan applicant / customer.
 * Maps to the 'customers' table in the database.
 */
public class Customer {

    private int customerId;
    private String name;
    private LocalDate dateOfBirth;   // DOB — used to validate age (21-60)
    private String mobile;            // Must be unique across active applications
    private String address;
    private String kycDetails;        // e.g., Aadhaar / PAN number
    private double monthlyIncome;     // Net monthly income
    private double existingObligations; // Existing EMI obligations per month
    private LocalDate createdAt;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────

    /** Default no-arg constructor */
    public Customer() {}

    /** Full constructor — used when loading from DB */
    public Customer(int customerId, String name, LocalDate dateOfBirth,
                    String mobile, String address, String kycDetails,
                    double monthlyIncome, double existingObligations,
                    LocalDate createdAt) {
        this.customerId = customerId;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.mobile = mobile;
        this.address = address;
        this.kycDetails = kycDetails;
        this.monthlyIncome = monthlyIncome;
        this.existingObligations = existingObligations;
        this.createdAt = createdAt;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters & Setters
    // ─────────────────────────────────────────────────────────────────────────

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getKycDetails() { return kycDetails; }
    public void setKycDetails(String kycDetails) { this.kycDetails = kycDetails; }

    public double getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(double monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public double getExistingObligations() { return existingObligations; }
    public void setExistingObligations(double existingObligations) { this.existingObligations = existingObligations; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    // ─────────────────────────────────────────────────────────────────────────
    // toString — helpful for debugging
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", name='" + name + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", mobile='" + mobile + '\'' +
                ", address='" + address + '\'' +
                ", kycDetails='" + kycDetails + '\'' +
                ", monthlyIncome=" + monthlyIncome +
                ", existingObligations=" + existingObligations +
                ", createdAt=" + createdAt +
                '}';
    }
}
