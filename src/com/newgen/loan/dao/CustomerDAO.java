package com.newgen.loan.dao;

import com.newgen.loan.db.DBConnection;
import com.newgen.loan.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 3 - Data Access Layer
 * Handles CRUD and lookup queries for 'customer' table.
 */
public class CustomerDAO {

    public int createCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customer (name, date_of_birth, mobile, address, kyc_details, monthly_income, existing_monthly_obligations) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getName());
            ps.setDate(2, Date.valueOf(customer.getDateOfBirth()));
            ps.setString(3, customer.getMobile());
            ps.setString(4, customer.getAddress());
            ps.setString(5, customer.getKycDetails());
            ps.setDouble(6, customer.getMonthlyIncome());
            ps.setDouble(7, customer.getExistingObligations());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating customer failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    customer.setCustomerId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("Creating customer failed, no ID obtained.");
                }
            }
        }
    }

    public Customer findById(int customerId) throws SQLException {
        String sql = "SELECT * FROM customer WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCustomer(rs);
                }
            }
        }
        return null;
    }

    public Customer findByMobile(String mobile) throws SQLException {
        String sql = "SELECT * FROM customer WHERE mobile = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, mobile);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCustomer(rs);
                }
            }
        }
        return null;
    }

    public List<Customer> searchByName(String nameKeyword) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customer WHERE name LIKE ? ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nameKeyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapRowToCustomer(rs));
                }
            }
        }
        return customers;
    }

    public boolean updateCustomerFinancials(int customerId, double monthlyIncome, double obligations) throws SQLException {
        String sql = "UPDATE customer SET monthly_income = ?, existing_monthly_obligations = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, monthlyIncome);
            ps.setDouble(2, obligations);
            ps.setInt(3, customerId);

            return ps.executeUpdate() > 0;
        }
    }

    private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setName(rs.getString("name"));
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            c.setDateOfBirth(dob.toLocalDate());
        }
        c.setMobile(rs.getString("mobile"));
        c.setAddress(rs.getString("address"));
        c.setKycDetails(rs.getString("kyc_details"));
        c.setMonthlyIncome(rs.getDouble("monthly_income"));
        c.setExistingObligations(rs.getDouble("existing_monthly_obligations"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            c.setCreatedAt(ts.toLocalDateTime().toLocalDate());
        }
        return c;
    }
}
