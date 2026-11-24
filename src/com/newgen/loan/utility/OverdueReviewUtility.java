package com.newgen.loan.utility;

import com.newgen.loan.dao.SystemAlertDAO;
import com.newgen.loan.dao.UtilityRunDAO;
import com.newgen.loan.db.DBConnection;
import com.newgen.loan.service.BusinessDateConfig;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Module 7 - Background Utility Layer
 * Overdue Review Utility:
 * 1. Finds all applications in UNDER_REVIEW status.
 * 2. Checks review ageing: if older than 2 calendar days from submission,
 *    creates a persistent 'REVIEW_OVERDUE' alert without duplicates (idempotent).
 * 3. Exports all pending reviews to CSV (e.g. pending_reviews_YYYYMMDD.csv)
 *    with an 'OverdueFlag' column (Yes/No).
 * 4. Logs run details to utility_run table.
 */
public class OverdueReviewUtility implements Runnable {

    private final SystemAlertDAO alertDAO = new SystemAlertDAO();
    private final UtilityRunDAO runDAO = new UtilityRunDAO();

    @Override
    public void run() {
        executeUtility();
    }

    public void executeUtility() {
        System.out.println("\n[OverdueReviewUtility] Starting execution...");
        LocalDate businessDate = BusinessDateConfig.getBusinessDate();
        int runId = 0;
        int processedCount = 0;
        int alertsCreated = 0;

        try {
            runId = runDAO.logRunStart("OverdueReviewUtility");
        } catch (Exception ignored) {}

        String querySql = "SELECT la.application_id, la.application_number, c.name AS customer_name, " +
                          "la.product, la.requested_amount, la.submitted_at, " +
                          "DATEDIFF(?, DATE(la.submitted_at)) AS days_waiting " +
                          "FROM loan_application la " +
                          "JOIN customer c ON la.customer_id = c.customer_id " +
                          "WHERE la.status = 'UNDER_REVIEW'";

        String csvFileName = "pending_reviews_" + businessDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(querySql);
             PrintWriter csvWriter = new PrintWriter(new FileWriter(csvFileName))) {

            ps.setDate(1, Date.valueOf(businessDate));

            // Write CSV Header
            csvWriter.println("ApplicationID,ApplicationNumber,CustomerName,Product,RequestedAmount,SubmittedAt,DaysWaiting,OverdueFlag");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    processedCount++;
                    int appId = rs.getInt("application_id");
                    String appNum = rs.getString("application_number");
                    String custName = rs.getString("customer_name");
                    String product = rs.getString("product");
                    double amount = rs.getDouble("requested_amount");
                    String submittedAt = rs.getString("submitted_at");
                    int daysWaiting = rs.getInt("days_waiting");

                    boolean isOverdue = daysWaiting > 2;
                    String overdueFlag = isOverdue ? "Yes" : "No";

                    if (isOverdue) {
                        boolean created = alertDAO.createAlertIfNotExists(appId, "REVIEW_OVERDUE");
                        if (created) {
                            alertsCreated++;
                        }
                    }

                    // Write CSV Row
                    csvWriter.printf("%d,%s,\"%s\",%s,%.2f,%s,%d,%s\n",
                            appId, appNum, custName, product, amount, submittedAt, daysWaiting, overdueFlag);
                }
            }

            System.out.printf("[OverdueReviewUtility] Completed. Processed: %d, Alerts Created: %d, Exported to: %s\n",
                    processedCount, alertsCreated, csvFileName);

            if (runId > 0) {
                runDAO.updateRunComplete(runId, "SUCCESS", processedCount, alertsCreated, null);
            }

        } catch (Exception e) {
            System.err.println("[OverdueReviewUtility] Execution failed: " + e.getMessage());
            if (runId > 0) {
                try {
                    runDAO.updateRunComplete(runId, "FAILURE", processedCount, alertsCreated, e.getMessage());
                } catch (Exception ignored) {}
            }
        }
    }
}
