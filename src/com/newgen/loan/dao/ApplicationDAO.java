package com.newgen.loan.dao;

import com.newgen.loan.db.DBConnection;
import com.newgen.loan.model.Application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 3 - Data Access Layer
 * Handles CRUD and workflow queries for 'loan_application' table.
 */
public class ApplicationDAO {

    public int createDraftApplication(Application app, String applicationNumber) throws SQLException {
        String sql = "INSERT INTO loan_application (application_number, customer_id, product, branch_code, branch_name, " +
                     "requested_amount, tenure_months, monthly_income, existing_monthly_obligations, annual_interest_rate, " +
                     "proposed_emi, foir_percentage, eligible, eligibility_reasons, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, applicationNumber);
            ps.setInt(2, app.getCustomerId());
            ps.setString(3, app.getProductType().name());
            ps.setString(4, app.getBranchCode() != null ? app.getBranchCode() : "DEL01");
            ps.setString(5, "Main Branch");
            ps.setDouble(6, app.getRequestedAmount());
            ps.setInt(7, app.getTenureMonths());
            ps.setDouble(8, 0.0); // populated on submit
            ps.setDouble(9, 0.0);
            ps.setDouble(10, app.getAnnualInterestRate());
            ps.setDouble(11, app.getEmi());
            ps.setDouble(12, app.getFoir());
            ps.setBoolean(13, false);
            ps.setString(14, "");
            ps.setString(15, Application.Status.DRAFT.name());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating draft application failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    app.setApplicationId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("Creating application failed, no ID obtained.");
                }
            }
        }
    }

    public Application findById(int applicationId) throws SQLException {
        String sql = "SELECT * FROM loan_application WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToApplication(rs);
                }
            }
        }
        return null;
    }

    public Application findByApplicationNumber(String applicationNumber) throws SQLException {
        String sql = "SELECT * FROM loan_application WHERE application_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, applicationNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToApplication(rs);
                }
            }
        }
        return null;
    }

    public List<Application> findByCustomerId(int customerId) throws SQLException {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT * FROM loan_application WHERE customer_id = ? ORDER BY application_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToApplication(rs));
                }
            }
        }
        return list;
    }

    public boolean hasActiveApplication(int customerId, String product) throws SQLException {
        String sql = "SELECT COUNT(*) FROM loan_application WHERE customer_id = ? AND product = ? " +
                     "AND status IN ('DRAFT', 'UNDER_REVIEW', 'CREDIT_APPROVAL')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ps.setString(2, product);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean updateDraft(Application app) throws SQLException {
        String sql = "UPDATE loan_application SET requested_amount = ?, tenure_months = ?, annual_interest_rate = ? " +
                     "WHERE application_id = ? AND status = 'DRAFT'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, app.getRequestedAmount());
            ps.setInt(2, app.getTenureMonths());
            ps.setDouble(3, app.getAnnualInterestRate());
            ps.setInt(4, app.getApplicationId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatusAndAssessment(Connection conn, int applicationId, Application.Status newStatus,
                                            double monthlyIncome, double obligations, double emi, double foir,
                                            boolean eligible, String reasons, Timestamp submittedAt) throws SQLException {
        String sql = "UPDATE loan_application SET status = ?, monthly_income = ?, existing_monthly_obligations = ?, " +
                     "proposed_emi = ?, foir_percentage = ?, eligible = ?, eligibility_reasons = ?, submitted_at = ? " +
                     "WHERE application_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setDouble(2, monthlyIncome);
            ps.setDouble(3, obligations);
            ps.setDouble(4, emi);
            ps.setDouble(5, foir);
            ps.setBoolean(6, eligible);
            ps.setString(7, reasons);
            ps.setTimestamp(8, submittedAt);
            ps.setInt(9, applicationId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateReviewDecision(Connection conn, int applicationId, Application.Status newStatus,
                                       String reviewerCode, String decisionReason, Timestamp decidedAt) throws SQLException {
        String sql = "UPDATE loan_application SET status = ?, reviewer_code = ?, decision_reason = ?, decided_at = ? " +
                     "WHERE application_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setString(2, reviewerCode);
            ps.setString(3, decisionReason);
            ps.setTimestamp(4, decidedAt);
            ps.setInt(5, applicationId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(Connection conn, int applicationId, Application.Status newStatus) throws SQLException {
        String sql = "UPDATE loan_application SET status = ? WHERE application_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, applicationId);
            return ps.executeUpdate() > 0;
        }
    }

    private Application mapRowToApplication(ResultSet rs) throws SQLException {
        Application a = new Application();
        a.setApplicationId(rs.getInt("application_id"));
        a.setCustomerId(rs.getInt("customer_id"));
        a.setProductType(Application.ProductType.valueOf(rs.getString("product")));
        a.setRequestedAmount(rs.getDouble("requested_amount"));
        a.setTenureMonths(rs.getInt("tenure_months"));
        a.setAnnualInterestRate(rs.getDouble("annual_interest_rate"));
        a.setEmi(rs.getDouble("proposed_emi"));
        a.setFoir(rs.getDouble("foir_percentage"));
        a.setStatus(Application.Status.valueOf(rs.getString("status")));
        a.setBranchCode(rs.getString("branch_code"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) a.setCreatedAt(created.toLocalDateTime());

        Timestamp submitted = rs.getTimestamp("submitted_at");
        if (submitted != null) a.setSubmittedAt(submitted.toLocalDateTime());

        Timestamp decided = rs.getTimestamp("decided_at");
        if (decided != null) a.setDecidedAt(decided.toLocalDateTime());

        return a;
    }
}
