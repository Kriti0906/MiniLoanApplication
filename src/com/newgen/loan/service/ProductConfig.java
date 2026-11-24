package com.newgen.loan.service;

/**
 * Module 4 - Service Layer
 * Holds configurable business product parameters for PERSONAL and EDUCATION loans.
 * These can be changed dynamically by the business without code redesign.
 */
public class ProductConfig {

    // PERSONAL loan parameters
    public static double PERSONAL_MIN_AMOUNT = 50000.0;
    public static double PERSONAL_MAX_AMOUNT = 500000.0;
    public static int PERSONAL_MIN_TENURE = 12;
    public static int PERSONAL_MAX_TENURE = 60;
    public static double PERSONAL_MIN_INCOME = 20000.0;
    public static double PERSONAL_INTEREST_RATE = 12.0;

    // EDUCATION loan parameters
    public static double EDUCATION_MIN_AMOUNT = 100000.0;
    public static double EDUCATION_MAX_AMOUNT = 1000000.0;
    public static int EDUCATION_MIN_TENURE = 12;
    public static int EDUCATION_MAX_TENURE = 84;
    public static double EDUCATION_MIN_INCOME = 15000.0;
    public static double EDUCATION_INTEREST_RATE = 10.0;
    public static boolean EDUCATION_CO_BORROWER_ALLOWED = false;

    // Affordability FOIR max benchmark
    public static double MAX_FOIR_PERCENTAGE = 50.0;

    // Age boundaries
    public static int MIN_AGE = 21;
    public static int MAX_AGE = 60;
}
