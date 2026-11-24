package com.newgen.loan.utility;

import com.newgen.loan.dao.ApplicationDAO;
import com.newgen.loan.dao.ApplicationHistoryDAO;
import com.newgen.loan.dao.CoreBankingDAO;
import com.newgen.loan.dao.UtilityRunDAO;
import com.newgen.loan.db.DBConnection;
import com.newgen.loan.model.Application;
import com.newgen.loan.model.ApplicationHistory;
import com.newgen.loan.service.BusinessDateConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Module 7 - Background Utility Layer
 * CoreDataPush Utility:
 * 1. Finds all applications currently in 'CORE_BANKING_PUSH' stage (i.e. successfully disbursed).
 * 2. Pushes records to 'NG_CORE_BANKING' table with all required values:
 *    (CustomerID, ApplicationID, Customer Name, Address, DOB, KYC Details, Approved Amount, Disbursed Amount, Disbursed Date).
 * 3. Enforces idempotency: No duplicate entries allowed, no records missed.
 * 4. Automatically moves each case to 'EXIT' terminal stage upon success.
 * 5. Uses database transaction per case with full rollback safety.
 */
public class CoreDataPushUtility implements Runnable {

    private final CoreBankingDAO coreBankingDAO = new CoreBankingDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final ApplicationHistoryDAO historyDAO = new ApplicationHistoryDAO();
    private final UtilityRunDAO runDAO = new UtilityRunDAO();

    @Override
    public void run() {
        executeUtility();
    }

    public void executeUtility() {
        System.out.println("\n[CoreDataPushUtility] Checking for cases in CORE_BANKING_PUSH stage...");
        int runId = 0;
        int pushedCount = 0;

        try {
            runId = runDAO.logRunStart("CoreDataPushUtility");
        } catch (Exception ignored) {}

        String fetchSql = "SELECT la.application_id, la.customer_id, c.name AS customer_name, " +
                          "c.address, c.date_of_birth, c.kyc_details, la.requested_amount, " +
                          "d.amount AS disbursed_amount, d.disbursed_at " +
                          "FROM loan_application la " +
                          "JOIN customer c ON la.customer_id = c.customer_id " +
                          "JOIN disbursement d ON la.application_id = d.application_id " +
                          "WHERE la.status = 'CORE_BANKING_PUSH'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(fetchSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int appId = rs.getInt("application_id");
                int custId = rs.getInt("customer_id");
                String name = rs.getString("customer_name");
                String address = rs.getString("address");
                LocalDate dob = rs.getDate("date_of_birth").toLocalDate();
                String kyc = rs.getString("kyc_details");
                double approvedAmt = rs.getDouble("requested_amount");
                double disbursedAmt = rs.getDouble("disbursed_amount");
                LocalDateTime disbursedDate = rs.getTimestamp("disbursed_at").toLocalDateTime();

                boolean success = processSingleCase(custId, appId, name, address, dob, kyc, approvedAmt, disbursedAmt, disbursedDate);
                if (success) {
                    pushedCount++;
                }
            }

            System.out.println("[CoreDataPushUtility] Completed. Cases pushed to Core Banking & moved to EXIT: " + pushedCount);

            if (runId > 0) {
                runDAO.updateRunComplete(runId, "SUCCESS", pushedCount, 0, null);
            }

        } catch (Exception e) {
            System.err.println("[CoreDataPushUtility] Error during processing: " + e.getMessage());
            if (runId > 0) {
                try {
                    runDAO.updateRunComplete(runId, "FAILURE", pushedCount, 0, e.getMessage());
                } catch (Exception ignored) {}
            }
        }
    }

    private boolean processSingleCase(int custId, int appId, String name, String address,
                                      LocalDate dob, String kyc, double approvedAmt,
                                      double disbursedAmt, LocalDateTime disbursedDate) {

        Connection conn = null;
        try {
            // Check if already pushed (duplicate safety)
            if (coreBankingDAO.existsByApplicationId(appId)) {
                System.out.println("  [Notice] Application ID " + appId + " already exists in NG_CORE_BANKING. Skipping insert.");
                return false;
            }

            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert into NG_CORE_BANKING
            coreBankingDAO.pushToCoreBanking(conn, custId, appId, name, address, dob, kyc, approvedAmt, disbursedAmt, disbursedDate);

            // 2. Automatically advance stage to EXIT
            applicationDAO.updateStatus(conn, appId, Application.Status.EXIT);

            // 3. Record audit trail
            LocalDateTime now = BusinessDateConfig.getBusinessTimestamp();
            ApplicationHistory hist = new ApplicationHistory(
                    0, appId, "CORE_BANKING_PUSH", Application.Status.CORE_BANKING_PUSH.name(),
                    Application.Status.EXIT.name(), "SYSTEM_UTILITY", now,
                    "Case data pushed to NG_CORE_BANKING; moved to EXIT terminal stage."
            );
            historyDAO.insertHistory(conn, hist);

            conn.commit();
            System.out.println("  ✅ Case [App ID: " + appId + "] pushed to NG_CORE_BANKING -> Status: EXIT");
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("  ❌ Failed pushing App ID " + appId + " to Core Banking: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }
}
