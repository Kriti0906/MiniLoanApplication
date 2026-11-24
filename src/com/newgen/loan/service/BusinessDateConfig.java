package com.newgen.loan.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Module 4 - Service Layer
 * Manages configurable business date (defaults to today).
 * Business timestamps combine this date with current local time.
 */
public class BusinessDateConfig {

    private static LocalDate businessDate = LocalDate.now();

    public static synchronized LocalDate getBusinessDate() {
        return businessDate;
    }

    public static synchronized void setBusinessDate(LocalDate newDate) {
        businessDate = newDate;
    }

    public static synchronized void resetToToday() {
        businessDate = LocalDate.now();
    }

    public static synchronized LocalDateTime getBusinessTimestamp() {
        return LocalDateTime.of(businessDate, LocalTime.now());
    }
}
