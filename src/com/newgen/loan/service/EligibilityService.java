package com.newgen.loan.service;

import com.newgen.loan.model.Application;
import com.newgen.loan.model.Customer;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 4 - Service Layer
 * Evaluates lending rules, EMI, and FOIR eligibility.
 */
public class EligibilityService {

    public static class EligibilityResult {
        private final boolean eligible;
        private final double emi;
        private final double foir;
        private final List<String> failureReasons;

        public EligibilityResult(boolean eligible, double emi, double foir, List<String> failureReasons) {
            this.eligible = eligible;
            this.emi = emi;
            this.foir = foir;
            this.failureReasons = failureReasons;
        }

        public boolean isEligible() { return eligible; }
        public double getEmi() { return emi; }
        public double getFoir() { return foir; }
        public List<String> getFailureReasons() { return failureReasons; }
    }

    public EligibilityResult evaluate(Customer customer, Application app, LocalDate businessDate) {
        List<String> failedRules = new ArrayList<>();

        // 1. Age Rule (21 to 60 completed years)
        if (customer.getDateOfBirth() != null) {
            int age = Period.between(customer.getDateOfBirth(), businessDate).getYears();
            if (age < ProductConfig.MIN_AGE || age > ProductConfig.MAX_AGE) {
                failedRules.add("Age " + age + " is outside permitted range (" +
                                ProductConfig.MIN_AGE + " to " + ProductConfig.MAX_AGE + " years).");
            }
        }

        // 2. Product Rule Checks
        double minAmount, maxAmount, minIncome, interestRate;
        int minTenure, maxTenure;

        if (app.getProductType() == Application.ProductType.PERSONAL) {
            minAmount = ProductConfig.PERSONAL_MIN_AMOUNT;
            maxAmount = ProductConfig.PERSONAL_MAX_AMOUNT;
            minTenure = ProductConfig.PERSONAL_MIN_TENURE;
            maxTenure = ProductConfig.PERSONAL_MAX_TENURE;
            minIncome = ProductConfig.PERSONAL_MIN_INCOME;
            interestRate = ProductConfig.PERSONAL_INTEREST_RATE;
        } else {
            minAmount = ProductConfig.EDUCATION_MIN_AMOUNT;
            maxAmount = ProductConfig.EDUCATION_MAX_AMOUNT;
            minTenure = ProductConfig.EDUCATION_MIN_TENURE;
            maxTenure = ProductConfig.EDUCATION_MAX_TENURE;
            minIncome = ProductConfig.EDUCATION_MIN_INCOME;
            interestRate = ProductConfig.EDUCATION_INTEREST_RATE;
        }

        // Validate Requested Amount
        if (app.getRequestedAmount() < minAmount || app.getRequestedAmount() > maxAmount) {
            failedRules.add("Requested amount " + CalculationService.formatCurrency(app.getRequestedAmount()) +
                            " is outside product range (" + CalculationService.formatCurrency(minAmount) +
                            " to " + CalculationService.formatCurrency(maxAmount) + ").");
        }

        // Validate Tenure
        if (app.getTenureMonths() < minTenure || app.getTenureMonths() > maxTenure) {
            failedRules.add("Tenure " + app.getTenureMonths() + " months is outside product range (" +
                            minTenure + " to " + maxTenure + " months).");
        }

        // Validate Minimum Income
        if (customer.getMonthlyIncome() < minIncome) {
            failedRules.add("Monthly net income " + CalculationService.formatCurrency(customer.getMonthlyIncome()) +
                            " is below minimum requirement of " + CalculationService.formatCurrency(minIncome) + ".");
        }

        // 3. EMI & FOIR Calculation
        double emi = CalculationService.calculateEMI(app.getRequestedAmount(), interestRate, app.getTenureMonths());
        double foir = CalculationService.calculateFOIR(customer.getMonthlyIncome(), customer.getExistingObligations(), emi);

        if (foir > ProductConfig.MAX_FOIR_PERCENTAGE) {
            failedRules.add("FOIR " + foir + "% exceeds maximum permitted limit of " +
                            ProductConfig.MAX_FOIR_PERCENTAGE + "%.");
        }

        boolean eligible = failedRules.isEmpty();
        return new EligibilityResult(eligible, emi, foir, failedRules);
    }
}
