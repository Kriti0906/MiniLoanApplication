package com.newgen.loan.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Module 4 - Service Layer
 * Financial calculations using BigDecimal for precision.
 * Implements exact EMI & FOIR formulas per specification.
 */
public class CalculationService {

    /**
     * Calculates EMI using formula:
     * EMI = P * r * (1+r)^n / ((1+r)^n - 1)
     * where:
     *   P = requested amount
     *   r = annualRate / 12 / 100 (monthly rate)
     *   n = tenure in months
     */
    public static double calculateEMI(double principal, double annualRate, int tenureMonths) {
        if (principal <= 0 || tenureMonths <= 0 || annualRate <= 0) {
            return 0.0;
        }

        double monthlyRate = annualRate / 12.0 / 100.0;
        double factor = Math.pow(1.0 + monthlyRate, tenureMonths);
        double emi = principal * monthlyRate * factor / (factor - 1.0);

        // Round to 2 decimal places using BigDecimal
        BigDecimal bd = BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Calculates FOIR (Fixed Obligation to Income Ratio) percentage:
     * FOIR = (existing monthly obligations + rounded EMI) / monthly net income * 100
     */
    public static double calculateFOIR(double monthlyIncome, double existingObligations, double emi) {
        if (monthlyIncome <= 0) {
            return 999.99; // Ineligible / division safety
        }

        double totalObligations = existingObligations + emi;
        double foir = (totalObligations / monthlyIncome) * 100.0;

        BigDecimal bd = BigDecimal.valueOf(foir).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Formats amount according to Indian numbering format: ##,##,###,###.##
     */
    public static String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("##,##,###,###.00");
        return "₹ " + df.format(amount);
    }
}
