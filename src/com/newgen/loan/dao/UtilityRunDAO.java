package com.newgen.loan.dao;

import com.newgen.loan.db.DBConnection;
import com.newgen.loan.model.UtilityRun;

import java.sql.*;

/**
 * Module 3 - Data Access Layer
 * Handles logging utility execution runs in 'utility_run' table.
 */
public class UtilityRunDAO {

    public int logRunStart(String utilityName) throws SQLException {
        String sql = "INSERT INTO utility_run (utility_name, started_at, result) VALUES (?, NOW(), 'SUCCESS')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, utilityName);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public void updateRunComplete(int runId, String result, int processed, int alerts, String errorMessage) throws SQLException {
        String sql = "UPDATE utility_run SET completed_at = NOW(), result = ?, applications_processed = ?, " +
                     "alerts_created = ?, error_message = ? WHERE run_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, result);
            ps.setInt(2, processed);
            ps.setInt(3, alerts);
            ps.setString(4, errorMessage);
            ps.setInt(5, runId);

            ps.executeUpdate();
        }
    }
}
