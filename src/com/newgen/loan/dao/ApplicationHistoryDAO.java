package com.newgen.loan.dao;

import com.newgen.loan.db.DBConnection;
import com.newgen.loan.model.ApplicationHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 3 - Data Access Layer
 * Handles audit logging in 'application_history' table.
 */
public class ApplicationHistoryDAO {

    public int insertHistory(Connection conn, ApplicationHistory history) throws SQLException {
        String sql = "INSERT INTO application_history (application_id, action, old_status, new_status, performed_by, performed_at, remarks) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, history.getApplicationId());
            ps.setString(2, history.getAction());
            ps.setString(3, history.getOldStatus());
            ps.setString(4, history.getNewStatus());
            ps.setString(5, history.getPerformedBy());
            ps.setTimestamp(6, history.getPerformedAt() != null ? Timestamp.valueOf(history.getPerformedAt()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(7, history.getRemarks());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    history.setHistoryId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public List<ApplicationHistory> findByApplicationId(int applicationId) throws SQLException {
        List<ApplicationHistory> list = new ArrayList<>();
        String sql = "SELECT * FROM application_history WHERE application_id = ? ORDER BY performed_at ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ApplicationHistory h = new ApplicationHistory();
                    h.setHistoryId(rs.getInt("history_id"));
                    h.setApplicationId(rs.getInt("application_id"));
                    h.setAction(rs.getString("action"));
                    h.setOldStatus(rs.getString("old_status"));
                    h.setNewStatus(rs.getString("new_status"));
                    h.setPerformedBy(rs.getString("performed_by"));
                    Timestamp ts = rs.getTimestamp("performed_at");
                    if (ts != null) h.setPerformedAt(ts.toLocalDateTime());
                    h.setRemarks(rs.getString("remarks"));
                    list.add(h);
                }
            }
        }
        return list;
    }
}
