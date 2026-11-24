package com.newgen.loan.service;

import com.newgen.loan.dao.ApplicationDAO;
import com.newgen.loan.dao.ApplicationHistoryDAO;
import com.newgen.loan.dao.CustomerDAO;
import com.newgen.loan.db.DBConnection;
import com.newgen.loan.model.Application;
import com.newgen.loan.model.ApplicationHistory;
import com.newgen.loan.model.Customer;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Module 4 - Service Layer
 * Application lifecycle management:
 * - DRAFT creation & editing
 * - Submission with full transactional integrity and state transition (DRAFT -> UNDER_REVIEW or REJECTED)
 * - Active application duplicate rule
 */
public class ApplicationService {

    private final ApplicationDAO applicationDAO;
    private final CustomerDAO customerDAO;
    private final ApplicationHistoryDAO historyDAO;
    private final EligibilityService eligibilityService;

    public ApplicationService() {
        this.applicationDAO = new ApplicationDAO();
        this.customerDAO = new CustomerDAO();
        this.historyDAO = new ApplicationHistoryDAO();
        this.eligibilityService = new EligibilityService();
    }

    /**
     * Creates an application in DRAFT state.
     * Enforces active application duplicate rule.
     */
    public int createDraftApplication(int customerId, Application.ProductType product,
                                      double requestedAmount, int tenureMonths, String branchCode) throws Exception {

        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer with ID " + customerId + " not found.");
        }

        // Active application rule: Only one active application per customer per product
        if (applicationDAO.hasActiveApplication(customerId, product.name())) {
            throw new IllegalStateException("Customer already has an active application (DRAFT, UNDER_REVIEW, or CREDIT_APPROVAL) for " + product.name() + ".");
        }

        double interestRate = (product == Application.ProductType.PERSONAL)
                ? ProductConfig.PERSONAL_INTEREST_RATE
                : ProductConfig.EDUCATION_INTEREST_RATE;

        double emi = CalculationService.calculateEMI(requestedAmount, interestRate, tenureMonths);
        double foir = CalculationService.calculateFOIR(customer.getMonthlyIncome(), customer.getExistingObligations(), emi);

        Application app = new Application();
        app.setCustomerId(customerId);
        app.setProductType(product);
        app.setRequestedAmount(requestedAmount);
        app.setTenureMonths(tenureMonths);
        app.setAnnualInterestRate(interestRate);
        app.setEmi(emi);
        app.setFoir(foir);
        app.setBranchCode(branchCode);

        String appNumber = "APP-" + System.currentTimeMillis() % 1000000;
        int appId = applicationDAO.createDraftApplication(app, appNumber);

        // Record history
        try (Connection conn = DBConnection.getConnection()) {
            ApplicationHistory hist = new ApplicationHistory(
                    0, appId, "CREATED_DRAFT", null, Application.Status.DRAFT.name(),
                    "APPLICANT", LocalDateTime.now(), "Application created in DRAFT stage"
            );
            historyDAO.insertHistory(conn, hist);
        }

        return appId;
    }

    /**
     * Allows editing loan details while in DRAFT status only.
     */
    public void updateDraft(int applicationId, double newAmount, int newTenure) throws Exception {
        Application app = applicationDAO.findById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("Application not found.");
        }
        if (app.getStatus() != Application.Status.DRAFT) {
            throw new IllegalStateException("Editing allowed only in DRAFT stage. Current stage: " + app.getStatus());
        }

        app.setRequestedAmount(newAmount);
        app.setTenureMonths(newTenure);
        applicationDAO.updateDraft(app);
    }

    /**
     * Submits application. Executes inside a database transaction:
     * - Validates customer & loan inputs
     * - Runs EMI & FOIR eligibility
     * - Moves to UNDER_REVIEW if eligible, else REJECTED
     * - Audits in application_history
     * - Rolls back on any failure
     */
    public EligibilityService.EligibilityResult submitApplication(int applicationId) throws Exception {
        Application app = applicationDAO.findById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("Application not found.");
        }

        if (app.getStatus() != Application.Status.DRAFT) {
            throw new IllegalStateException("Only DRAFT applications can be submitted. Current status: " + app.getStatus());
        }

        Customer customer = customerDAO.findById(app.getCustomerId());
        if (customer == null) {
            throw new IllegalStateException("Customer data missing for application ID: " + applicationId);
        }

        LocalDate bDate = BusinessDateConfig.getBusinessDate();
        EligibilityService.EligibilityResult assessment = eligibilityService.evaluate(customer, app, bDate);

        Application.Status targetStatus = assessment.isEligible()
                ? Application.Status.UNDER_REVIEW
                : Application.Status.REJECTED;

        String reasons = assessment.isEligible()
                ? "Eligibility checks passed successfully"
                : String.join("; ", assessment.getFailureReasons());

        LocalDateTime now = BusinessDateConfig.getBusinessTimestamp();
        Timestamp timestamp = Timestamp.valueOf(now);

        // Transactional execution
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Update application status and assessment fields
            applicationDAO.updateStatusAndAssessment(
                    conn, applicationId, targetStatus, customer.getMonthlyIncome(),
                    customer.getExistingObligations(), assessment.getEmi(),
                    assessment.getFoir(), assessment.isEligible(), reasons, timestamp
            );

            // 2. Insert history audit
            ApplicationHistory hist = new ApplicationHistory(
                    0, applicationId, "SUBMIT", Application.Status.DRAFT.name(),
                    targetStatus.name(), "APPLICANT", now, reasons
            );
            historyDAO.insertHistory(conn, hist);

            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new RuntimeException("Submission failed and was rolled back: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }

        return assessment;
    }

    public Application getApplicationById(int applicationId) throws SQLException {
        return applicationDAO.findById(applicationId);
    }

    public Application getApplicationByNumber(String appNumber) throws SQLException {
        return applicationDAO.findByApplicationNumber(appNumber);
    }

    public List<Application> getApplicationsByCustomer(int customerId) throws SQLException {
        return applicationDAO.findByCustomerId(customerId);
    }
}
