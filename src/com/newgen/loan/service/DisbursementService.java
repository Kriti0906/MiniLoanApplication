package com.newgen.loan.service;

import com.newgen.loan.dao.ApplicationDAO;
import com.newgen.loan.dao.ApplicationHistoryDAO;
import com.newgen.loan.dao.DisbursementDAO;
import com.newgen.loan.db.DBConnection;
import com.newgen.loan.model.Application;
import com.newgen.loan.model.ApplicationHistory;
import com.newgen.loan.model.Disbursement;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Module 4 - Service Layer
 * Simulated loan disbursement:
 * - Disburses only from DISBURSEMENT stage
 * - Unique reference per transaction
 * - Enforces 1 disbursement per application
 * - Atomic transaction with rollback on failure
 * - Moves application to CORE_BANKING_PUSH
 */
public class DisbursementService {

    private final ApplicationDAO applicationDAO;
    private final DisbursementDAO disbursementDAO;
    private final ApplicationHistoryDAO historyDAO;

    public DisbursementService() {
        this.applicationDAO = new ApplicationDAO();
        this.disbursementDAO = new DisbursementDAO();
        this.historyDAO = new ApplicationHistoryDAO();
    }

    public Disbursement disburseLoan(int applicationId, String referenceNumber, String operatorCode) throws Exception {
        Application app = applicationDAO.findById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("Application not found.");
        }

        // Disbursement from any status other than DISBURSEMENT must be rejected
        if (app.getStatus() != Application.Status.DISBURSEMENT) {
            throw new IllegalStateException("Disbursement is only permitted from DISBURSEMENT stage. Current status: " + app.getStatus());
        }

        // Reject repeated disbursement
        if (disbursementDAO.findByApplicationId(applicationId) != null) {
            throw new IllegalStateException("Application ID " + applicationId + " has already been disbursed.");
        }

        // Reject duplicate references
        if (disbursementDAO.findByReferenceNumber(referenceNumber) != null) {
            throw new IllegalArgumentException("Disbursement reference '" + referenceNumber + "' has already been used.");
        }

        LocalDateTime now = BusinessDateConfig.getBusinessTimestamp();

        Disbursement disbursement = new Disbursement();
        disbursement.setApplicationId(applicationId);
        disbursement.setReferenceNumber(referenceNumber);
        disbursement.setDisbursedAmount(app.getRequestedAmount());
        disbursement.setDisbursedAt(now);

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert into disbursement table
            disbursementDAO.insertDisbursement(conn, disbursement);

            // 2. Transition status from DISBURSEMENT to CORE_BANKING_PUSH
            applicationDAO.updateStatus(conn, applicationId, Application.Status.CORE_BANKING_PUSH);

            // 3. Record history audit
            ApplicationHistory hist = new ApplicationHistory(
                    0, applicationId, "DISBURSE", Application.Status.DISBURSEMENT.name(),
                    Application.Status.CORE_BANKING_PUSH.name(), operatorCode, now,
                    "Disbursed " + CalculationService.formatCurrency(disbursement.getDisbursedAmount()) +
                    " Ref: " + referenceNumber
            );
            historyDAO.insertHistory(conn, hist);

            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new RuntimeException("Disbursement failed and was rolled back: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }

        return disbursement;
    }
}
