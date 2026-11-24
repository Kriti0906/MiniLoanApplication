package com.newgen.loan.reports;

import com.newgen.loan.db.DBConnection;
import com.newgen.loan.service.BusinessDateConfig;
import com.newgen.loan.service.CalculationService;

import java.sql.*;
import java.time.LocalDate;

/**
 * Module 6 - SQL Reports Layer
 * Executes the six parameterized SQL reports specified in the assignment.
 */
public class ReportService {

    /**
     * Report 1: Application Register
     * Filters by product and status (pass null or empty string for all).
     */
    public void runApplicationRegister(String productFilter, String statusFilter) {
        String sql = "SELECT la.application_number, c.name AS customer_name, la.product, " +
                     "la.requested_amount, la.proposed_emi, la.status, la.submitted_at " +
                     "FROM loan_application la " +
                     "JOIN customer c ON la.customer_id = c.customer_id " +
                     "WHERE (? IS NULL OR la.product = ?) " +
                     "  AND (? IS NULL OR la.status = ?) " +
                     "ORDER BY la.submitted_at DESC";

        System.out.println("\n" + "=".repeat(95));
        System.out.printf("%-15s %-20s %-12s %-15s %-12s %-15s %-10s\n",
                "App Number", "Customer", "Product", "Amount", "EMI", "Status", "Submitted At");
        System.out.println("=".repeat(95));

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String p = (productFilter == null || productFilter.trim().isEmpty() || productFilter.equalsIgnoreCase("ALL")) ? null : productFilter.trim().toUpperCase();
            String s = (statusFilter == null || statusFilter.trim().isEmpty() || statusFilter.equalsIgnoreCase("ALL")) ? null : statusFilter.trim().toUpperCase();

            ps.setString(1, p);
            ps.setString(2, p);
            ps.setString(3, s);
            ps.setString(4, s);

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.printf("%-15s %-20s %-12s %-15s %-12s %-15s %-10s\n",
                            rs.getString("application_number"),
                            truncate(rs.getString("customer_name"), 19),
                            rs.getString("product"),
                            CalculationService.formatCurrency(rs.getDouble("requested_amount")),
                            CalculationService.formatCurrency(rs.getDouble("proposed_emi")),
                            rs.getString("status"),
                            rs.getTimestamp("submitted_at") != null ? rs.getTimestamp("submitted_at").toLocalDateTime().toLocalDate().toString() : "DRAFT");
                }
                if (count == 0) System.out.println("  No applications matched the criteria.");
            }
        } catch (SQLException e) {
            System.err.println("Database error running Report 1: " + e.getMessage());
        }
        System.out.println("=".repeat(95));
    }

    /**
     * Report 2: Product Pipeline
     * Aggregation: Count and Sum by Product & Status, and Branch & Product.
     */
    public void runProductPipeline() {
        String sqlProductStatus = "SELECT product, status, COUNT(*) AS total_applications, " +
                                  "COALESCE(SUM(requested_amount), 0.00) AS total_amount " +
                                  "FROM loan_application GROUP BY product, status ORDER BY product, status";

        String sqlBranchProduct = "SELECT branch_code, branch_name, product, COUNT(*) AS total_applications, " +
                                  "COALESCE(SUM(requested_amount), 0.00) AS total_amount " +
                                  "FROM loan_application GROUP BY branch_code, branch_name, product ORDER BY branch_code, product";

        System.out.println("\n" + "=".repeat(75));
        System.out.println("  PART A: PRODUCT PIPELINE BY PRODUCT & STATUS");
        System.out.println("=".repeat(75));
        System.out.printf("%-15s %-20s %-15s %-20s\n", "Product", "Status", "Applications", "Total Requested Amt");
        System.out.println("-".repeat(75));

        try (Connection conn = DBConnection.getConnection()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlProductStatus)) {
                while (rs.next()) {
                    System.out.printf("%-15s %-20s %-15d %-20s\n",
                            rs.getString("product"),
                            rs.getString("status"),
                            rs.getInt("total_applications"),
                            CalculationService.formatCurrency(rs.getDouble("total_amount")));
                }
            }

            System.out.println("\n" + "=".repeat(75));
            System.out.println("  PART B: PIPELINE BY BRANCH & PRODUCT");
            System.out.println("=".repeat(75));
            System.out.printf("%-12s %-25s %-15s %-10s %-15s\n", "Branch Code", "Branch Name", "Product", "Count", "Total Amount");
            System.out.println("-".repeat(75));

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlBranchProduct)) {
                while (rs.next()) {
                    System.out.printf("%-12s %-25s %-15s %-10d %-15s\n",
                            rs.getString("branch_code"),
                            truncate(rs.getString("branch_name"), 24),
                            rs.getString("product"),
                            rs.getInt("total_applications"),
                            CalculationService.formatCurrency(rs.getDouble("total_amount")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error running Report 2: " + e.getMessage());
        }
        System.out.println("=".repeat(75));
    }

    /**
     * Report 3: Daily / Monthly / Quarterly Disbursement
     * Filters by supplied date range and shows branch breakdown.
     */
    public void runDisbursementReport(LocalDate startDate, LocalDate endDate) {
        String sqlDateWise = "SELECT DATE(disbursed_at) AS disb_date, COUNT(*) AS total_count, " +
                             "COALESCE(SUM(amount), 0.00) AS total_amount " +
                             "FROM disbursement " +
                             "WHERE DATE(disbursed_at) BETWEEN ? AND ? " +
                             "GROUP BY DATE(disbursed_at) ORDER BY disb_date ASC";

        String sqlBranchWise = "SELECT la.branch_code, la.branch_name, COUNT(d.disbursement_id) AS total_count, " +
                               "COALESCE(SUM(d.amount), 0.00) AS total_amount " +
                               "FROM disbursement d " +
                               "JOIN loan_application la ON d.application_id = la.application_id " +
                               "WHERE DATE(d.disbursed_at) BETWEEN ? AND ? " +
                               "GROUP BY la.branch_code, la.branch_name ORDER BY la.branch_code ASC";

        System.out.println("\n" + "=".repeat(70));
        System.out.println("  DISBURSEMENT REPORT (" + startDate + " to " + endDate + ")");
        System.out.println("=".repeat(70));
        System.out.printf("%-20s %-15s %-25s\n", "Disbursement Date", "Count", "Total Disbursed Amount");
        System.out.println("-".repeat(70));

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlDateWise)) {
                ps.setDate(1, Date.valueOf(startDate));
                ps.setDate(2, Date.valueOf(endDate));
                try (ResultSet rs = ps.executeQuery()) {
                    boolean hasRows = false;
                    while (rs.next()) {
                        hasRows = true;
                        System.out.printf("%-20s %-15d %-25s\n",
                                rs.getDate("disb_date"),
                                rs.getInt("total_count"),
                                CalculationService.formatCurrency(rs.getDouble("total_amount")));
                    }
                    if (!hasRows) System.out.println("  No disbursements found in this date range.");
                }
            }

            System.out.println("\n--- BRANCH-WISE BREAKDOWN ---");
            System.out.printf("%-12s %-25s %-10s %-20s\n", "Branch Code", "Branch Name", "Count", "Total Amount");
            System.out.println("-".repeat(70));

            try (PreparedStatement ps = conn.prepareStatement(sqlBranchWise)) {
                ps.setDate(1, Date.valueOf(startDate));
                ps.setDate(2, Date.valueOf(endDate));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.printf("%-12s %-25s %-10d %-20s\n",
                                rs.getString("branch_code"),
                                truncate(rs.getString("branch_name"), 24),
                                rs.getInt("total_count"),
                                CalculationService.formatCurrency(rs.getDouble("total_amount")));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error running Report 3: " + e.getMessage());
        }
        System.out.println("=".repeat(70));
    }

    /**
     * Report 4: Overdue Reviews
     * Current UNDER_REVIEW applications older than 2 calendar days.
     */
    public void runOverdueReviews() {
        LocalDate bDate = BusinessDateConfig.getBusinessDate();
        String sql = "SELECT la.application_id, la.application_number, c.name AS customer_name, " +
                     "la.product, la.requested_amount, la.submitted_at, " +
                     "DATEDIFF(?, DATE(la.submitted_at)) AS days_waiting " +
                     "FROM loan_application la " +
                     "JOIN customer c ON la.customer_id = c.customer_id " +
                     "WHERE la.status = 'UNDER_REVIEW' " +
                     "  AND DATEDIFF(?, DATE(la.submitted_at)) > 2 " +
                     "ORDER BY days_waiting DESC";

        System.out.println("\n" + "=".repeat(85));
        System.out.println("  OVERDUE REVIEWS REPORT (As of Business Date: " + bDate + ")");
        System.out.println("  Criteria: Status = UNDER_REVIEW AND Days Waiting > 2 Calendar Days");
        System.out.println("=".repeat(85));
        System.out.printf("%-10s %-15s %-20s %-12s %-15s %-12s\n",
                "App ID", "App Number", "Customer", "Product", "Amount", "Days Waiting");
        System.out.println("-".repeat(85));

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(bDate));
            ps.setDate(2, Date.valueOf(bDate));

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.printf("%-10d %-15s %-20s %-12s %-15s %-12d\n",
                            rs.getInt("application_id"),
                            rs.getString("application_number"),
                            truncate(rs.getString("customer_name"), 19),
                            rs.getString("product"),
                            CalculationService.formatCurrency(rs.getDouble("requested_amount")),
                            rs.getInt("days_waiting"));
                }
                if (count == 0) System.out.println("  ✅ No overdue review applications found.");
            }
        } catch (SQLException e) {
            System.err.println("Database error running Report 4: " + e.getMessage());
        }
        System.out.println("=".repeat(85));
    }

    /**
     * Report 5: Customers Without Disbursement
     * Customers with at least 1 application and NO disbursements across any application.
     */
    public void runCustomersWithoutDisbursement() {
        String sql = "SELECT DISTINCT c.customer_id, c.name, c.mobile, c.monthly_income, c.created_at " +
                     "FROM customer c " +
                     "JOIN loan_application la ON c.customer_id = la.customer_id " +
                     "WHERE c.customer_id NOT IN (" +
                     "    SELECT DISTINCT la2.customer_id " +
                     "    FROM loan_application la2 " +
                     "    JOIN disbursement d ON la2.application_id = d.application_id" +
                     ") ORDER BY c.customer_id ASC";

        System.out.println("\n" + "=".repeat(80));
        System.out.println("  CUSTOMERS WITH APPLICATIONS BUT NO DISBURSEMENT");
        System.out.println("=".repeat(80));
        System.out.printf("%-10s %-25s %-15s %-18s %-12s\n", "Cust ID", "Name", "Mobile", "Monthly Income", "Registered");
        System.out.println("-".repeat(80));

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("%-10d %-25s %-15s %-18s %-12s\n",
                        rs.getInt("customer_id"),
                        truncate(rs.getString("name"), 24),
                        rs.getString("mobile"),
                        CalculationService.formatCurrency(rs.getDouble("monthly_income")),
                        rs.getDate("created_at"));
            }
            if (count == 0) System.out.println("  No customers matching this condition.");
        } catch (SQLException e) {
            System.err.println("Database error running Report 5: " + e.getMessage());
        }
        System.out.println("=".repeat(80));
    }

    /**
     * Report 6: Repeat Applicants
     * Customers with >1 application using GROUP BY and HAVING.
     */
    public void runRepeatApplicants() {
        String sql = "SELECT c.customer_id, c.name, c.mobile, COUNT(la.application_id) AS total_apps, " +
                     "COALESCE(SUM(la.requested_amount), 0.00) AS total_requested_amount " +
                     "FROM customer c " +
                     "JOIN loan_application la ON c.customer_id = la.customer_id " +
                     "GROUP BY c.customer_id, c.name, c.mobile " +
                     "HAVING COUNT(la.application_id) > 1 " +
                     "ORDER BY total_apps DESC, total_requested_amount DESC";

        System.out.println("\n" + "=".repeat(80));
        System.out.println("  REPEAT APPLICANTS REPORT (GROUP BY + HAVING)");
        System.out.println("=".repeat(80));
        System.out.printf("%-10s %-25s %-15s %-15s %-20s\n", "Cust ID", "Customer Name", "Mobile", "Total Apps", "Total Requested Amt");
        System.out.println("-".repeat(80));

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("%-10d %-25s %-15s %-15d %-20s\n",
                        rs.getInt("customer_id"),
                        truncate(rs.getString("name"), 24),
                        rs.getString("mobile"),
                        rs.getInt("total_apps"),
                        CalculationService.formatCurrency(rs.getDouble("total_requested_amount")));
            }
            if (count == 0) System.out.println("  No repeat applicants found (no customers with > 1 application).");
        } catch (SQLException e) {
            System.err.println("Database error running Report 6: " + e.getMessage());
        }
        System.out.println("=".repeat(80));
    }

    private String truncate(String val, int maxLen) {
        if (val == null) return "";
        return val.length() <= maxLen ? val : val.substring(0, maxLen - 2) + "..";
    }
}
