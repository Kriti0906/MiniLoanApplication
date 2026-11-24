package com.newgen.loan.console;

import com.newgen.loan.dao.DisbursementDAO;
import com.newgen.loan.model.Application;
import com.newgen.loan.model.Disbursement;
import com.newgen.loan.service.ApplicationService;
import com.newgen.loan.service.CalculationService;
import com.newgen.loan.service.DisbursementService;

/**
 * Module 5 - Console Layer
 * Disbursement operations: Trigger simulated disbursement and view voucher details.
 */
public class DisbursementMenu {

    private final DisbursementService disbursementService = new DisbursementService();
    private final ApplicationService appService = new ApplicationService();
    private final DisbursementDAO disbursementDAO = new DisbursementDAO();

    public void showMenu() {
        while (true) {
            ConsoleHelper.printHeader("LOAN DISBURSEMENT DESK");
            System.out.println("1. Disburse Approved Loan");
            System.out.println("2. View Disbursement Details by Application ID");
            System.out.println("3. Back to Main Menu");

            int choice = ConsoleHelper.readInt("Enter choice (1-3)");
            switch (choice) {
                case 1 -> performDisbursement();
                case 2 -> viewDisbursement();
                case 3 -> { return; }
                default -> System.out.println("❌ Invalid option. Try again.");
            }
        }
    }

    private void performDisbursement() {
        ConsoleHelper.printHeader("SIMULATE LOAN DISBURSEMENT");
        try {
            int appId = ConsoleHelper.readInt("Enter Application ID");
            Application app = appService.getApplicationById(appId);
            if (app == null) {
                System.out.println("❌ Application not found.");
                return;
            }
            if (app.getStatus() != Application.Status.DISBURSEMENT) {
                System.out.println("❌ Application is not in DISBURSEMENT stage! Current status: " + app.getStatus());
                return;
            }

            System.out.println("Approved Disbursal Amount: " + CalculationService.formatCurrency(app.getRequestedAmount()));
            String refNo = ConsoleHelper.readString("Enter Unique Disbursement Reference (e.g., REF-" + System.currentTimeMillis() % 100000 + ")");
            String operator = ConsoleHelper.readString("Enter Disbursing Officer Code");

            Disbursement d = disbursementService.disburseLoan(appId, refNo, operator);

            System.out.println("\n🎉 DISBURSEMENT COMPLETED SUCCESSFULLY!");
            System.out.println("Disbursement ID  : " + d.getDisbursementId());
            System.out.println("Application ID   : " + d.getApplicationId());
            System.out.println("Disbursed Amount : " + CalculationService.formatCurrency(d.getDisbursedAmount()));
            System.out.println("Reference No.    : " + d.getReferenceNumber());
            System.out.println("Disbursed Date   : " + d.getDisbursedAt());
            System.out.println("Next Stage       : Moved to CORE_BANKING_PUSH");
        } catch (Exception e) {
            System.out.println("\n❌ Disbursement failed: " + e.getMessage());
        }
    }

    private void viewDisbursement() {
        int appId = ConsoleHelper.readInt("Enter Application ID");
        try {
            Disbursement d = disbursementDAO.findByApplicationId(appId);
            if (d == null) {
                System.out.println("❌ No disbursement found for Application ID: " + appId);
            } else {
                System.out.println("\n--- DISBURSEMENT RECORD ---");
                System.out.println("Disbursement ID  : " + d.getDisbursementId());
                System.out.println("Application ID   : " + d.getApplicationId());
                System.out.println("Reference Number : " + d.getReferenceNumber());
                System.out.println("Disbursed Amount : " + CalculationService.formatCurrency(d.getDisbursedAmount()));
                System.out.println("Disbursed Date   : " + d.getDisbursedAt());
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}
