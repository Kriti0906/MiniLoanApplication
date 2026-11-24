package com.newgen.loan.dao;

import com.newgen.loan.db.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Module 3 - Data Access Layer
 * Handles push operations to NG_CORE_BANKING table.
 * Includes duplicate safety checks.
 */
public class CoreBankingDAO {

    public boolean existsByApplicationId(int applicationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM NG_CORE_BANKING WHERE ApplicationID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean pushToCoreBanking(Connection conn, int customerId, int applicationId, String customerName,
                                    String address, LocalDate dob, String kycDetails,
                                    double approvedAmount, double disbursedAmount,
                                    LocalDateTime disbursedDate) throws SQLException {

        String sql = "INSERT INTO NG_CORE_BANKING " +
                     "(CustomerID, ApplicationID, CustomerName, Address, DOB, KYCDetails, ApprovedAmount, DisbursedAmount, DisbursedDate) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, applicationId);
            ps.setString(3, customerName);
            ps.setString(4, address);
            ps.setDate(5, Date.valueOf(dob));
            ps.setString(6, kycDetails);
            ps.setDouble(7, approvedAmount);
            ps.setDouble(8, disbursedAmount);
            ps.setTimestamp(9, Timestamp.valueOf(disbursedDate));

            return ps.executeUpdate() > 0;
        }
    }
}
