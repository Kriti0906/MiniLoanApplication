package com.newgen.loan.dao;

import com.newgen.loan.db.DBConnection;
import com.newgen.loan.model.Disbursement;

import java.sql.*;

/**
 * Module 3 - Data Access Layer
 * Handles operations on the 'disbursement' table.
 */
public class DisbursementDAO {

    public int insertDisbursement(Connection conn, Disbursement disbursement) throws SQLException {
        String sql = "INSERT INTO disbursement (application_id, reference_number, amount, disbursed_at) " +
                     "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, disbursement.getApplicationId());
            ps.setString(2, disbursement.getReferenceNumber());
            ps.setDouble(3, disbursement.getDisbursedAmount());
            ps.setTimestamp(4, Timestamp.valueOf(disbursement.getDisbursedAt()));

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating disbursement record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    disbursement.setDisbursementId(id);
                    return id;
                } else {
                    throw new SQLException("Creating disbursement failed, no ID obtained.");
                }
            }
        }
    }

    public Disbursement findByApplicationId(int applicationId) throws SQLException {
        String sql = "SELECT * FROM disbursement WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToDisbursement(rs);
                }
            }
        }
        return null;
    }

    public Disbursement findByReferenceNumber(String referenceNumber) throws SQLException {
        String sql = "SELECT * FROM disbursement WHERE reference_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, referenceNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToDisbursement(rs);
                }
            }
        }
        return null;
    }

    private Disbursement mapRowToDisbursement(ResultSet rs) throws SQLException {
        Disbursement d = new Disbursement();
        d.setDisbursementId(rs.getInt("disbursement_id"));
        d.setApplicationId(rs.getInt("application_id"));
        d.setReferenceNumber(rs.getString("reference_number"));
        d.setDisbursedAmount(rs.getDouble("amount"));
        Timestamp ts = rs.getTimestamp("disbursed_at");
        if (ts != null) {
            d.setDisbursedAt(ts.toLocalDateTime());
        }
        return d;
    }
}
