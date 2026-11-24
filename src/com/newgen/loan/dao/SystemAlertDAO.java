package com.newgen.loan.dao;

import com.newgen.loan.db.DBConnection;
import com.newgen.loan.model.SystemAlert;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 3 - Data Access Layer
 * Handles alerts in 'system_alerts' table.
 * Enforces uniqueness on (application_id, alert_type) so repeat utility runs don't create duplicate alerts.
 */
public class SystemAlertDAO {

    public boolean createAlertIfNotExists(int applicationId, String alertType) throws SQLException {
        String sql = "INSERT IGNORE INTO system_alerts (application_id, alert_type, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, applicationId);
            ps.setString(2, alertType);
            return ps.executeUpdate() > 0;
        }
    }

    public List<SystemAlert> findAlertsByApplication(int applicationId) throws SQLException {
        List<SystemAlert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM system_alerts WHERE application_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SystemAlert alert = new SystemAlert();
                    alert.setAlertId(rs.getInt("alert_id"));
                    alert.setApplicationId(rs.getInt("application_id"));
                    alert.setAlertType(rs.getString("alert_type"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) alert.setCreatedAt(ts.toLocalDateTime());
                    alerts.add(alert);
                }
            }
        }
        return alerts;
    }
}
