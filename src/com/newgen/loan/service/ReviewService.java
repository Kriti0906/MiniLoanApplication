package com.newgen.loan.service;

import com.newgen.loan.dao.ApplicationDAO;
import com.newgen.loan.dao.ApplicationHistoryDAO;
import com.newgen.loan.db.DBConnection;
import com.newgen.loan.model.Application;
import com.newgen.loan.model.ApplicationHistory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Module 4 - Service Layer
 * Reviewer decision flow (UNDER_REVIEW -> CREDIT_APPROVAL or REJECTED)
 * and Credit Approval decision (CREDIT_APPROVAL -> DISBURSEMENT or REJECTED).
 */
public class ReviewService {

    private final ApplicationDAO applicationDAO;
    private final ApplicationHistoryDAO historyDAO;

    public ReviewService() {
        this.applicationDAO = new ApplicationDAO();
        this.historyDAO = new ApplicationHistoryDAO();
    }

    /**
     * Performs reviewer action on UNDER_REVIEW stage.
     * Approvals retain requested amount and tenure, moving to CREDIT_APPROVAL.
     * Rejections require an explicit reason and move to terminal REJECTED.
     */
    public void decideReview(int applicationId, boolean approve, String reviewerCode, String reason) throws Exception {
        Application app = applicationDAO.findById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("Application not found.");
        }

        if (app.getStatus() != Application.Status.UNDER_REVIEW) {
            throw new IllegalStateException("Only applications in UNDER_REVIEW can be decided by reviewer. Current status: " + app.getStatus());
        }

        if (!approve && (reason == null || reason.trim().isEmpty())) {
            throw new IllegalArgumentException("Rejection requires a specific decision reason.");
        }

        Application.Status targetStatus = approve ? Application.Status.CREDIT_APPROVAL : Application.Status.REJECTED;
        LocalDateTime now = BusinessDateConfig.getBusinessTimestamp();
        Timestamp timestamp = Timestamp.valueOf(now);

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            applicationDAO.updateReviewDecision(
                    conn, applicationId, targetStatus, reviewerCode,
                    approve ? "Approved by reviewer" : reason, timestamp
            );

            ApplicationHistory hist = new ApplicationHistory(
                    0, applicationId, approve ? "REVIEW_APPROVE" : "REVIEW_REJECT",
                    Application.Status.UNDER_REVIEW.name(), targetStatus.name(),
                    reviewerCode, now, approve ? "Reviewer approved application" : reason
            );
            historyDAO.insertHistory(conn, hist);

            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new RuntimeException("Reviewer decision failed and was rolled back: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Performs final Credit Approval decision on CREDIT_APPROVAL stage.
     */
    public void decideCreditApproval(int applicationId, boolean approve, String reviewerCode, String reason) throws Exception {
        Application app = applicationDAO.findById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("Application not found.");
        }

        if (app.getStatus() != Application.Status.CREDIT_APPROVAL) {
            throw new IllegalStateException("Only applications in CREDIT_APPROVAL can be processed. Current status: " + app.getStatus());
        }

        if (!approve && (reason == null || reason.trim().isEmpty())) {
            throw new IllegalArgumentException("Credit rejection requires a reason.");
        }

        Application.Status targetStatus = approve ? Application.Status.DISBURSEMENT : Application.Status.REJECTED;
        LocalDateTime now = BusinessDateConfig.getBusinessTimestamp();
        Timestamp timestamp = Timestamp.valueOf(now);

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            applicationDAO.updateReviewDecision(
                    conn, applicationId, targetStatus, reviewerCode,
                    approve ? "Credit Approval Granted" : reason, timestamp
            );

            ApplicationHistory hist = new ApplicationHistory(
                    0, applicationId, approve ? "CREDIT_APPROVE" : "CREDIT_REJECT",
                    Application.Status.CREDIT_APPROVAL.name(), targetStatus.name(),
                    reviewerCode, now, approve ? "Credit approval completed; ready for disbursement" : reason
            );
            historyDAO.insertHistory(conn, hist);

            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new RuntimeException("Credit approval failed and was rolled back: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }
}
