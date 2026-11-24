package com.newgen.loan.console;

import com.newgen.loan.model.Application;
import com.newgen.loan.service.ApplicationService;
import com.newgen.loan.service.CalculationService;
import com.newgen.loan.service.ReviewService;

/**
 * Module 5 - Console Layer
 * Reviewer and Credit Approval decision workflows.
 */
public class ReviewMenu {

    private final ReviewService reviewService = new ReviewService();
    private final ApplicationService appService = new ApplicationService();

    public void showMenu() {
        while (true) {
            ConsoleHelper.printHeader("REVIEWER & CREDIT APPROVAL DESK");
            System.out.println("1. Process UNDER_REVIEW Application");
            System.out.println("2. Process CREDIT_APPROVAL Stage");
            System.out.println("3. Back to Main Menu");

            int choice = ConsoleHelper.readInt("Enter choice (1-3)");
            switch (choice) {
                case 1 -> processUnderReview();
                case 2 -> processCreditApproval();
                case 3 -> { return; }
                default -> System.out.println("❌ Invalid option. Try again.");
            }
        }
    }

    private void processUnderReview() {
        ConsoleHelper.printHeader("REVIEWER ACTION (UNDER_REVIEW)");
        try {
            int appId = ConsoleHelper.readInt("Enter Application ID");
            Application app = appService.getApplicationById(appId);
            if (app == null) {
                System.out.println("❌ Application not found.");
                return;
            }
            if (app.getStatus() != Application.Status.UNDER_REVIEW) {
                System.out.println("❌ Application is not in UNDER_REVIEW status. Current status: " + app.getStatus());
                return;
            }

            System.out.println("Product: " + app.getProductType() + " | Amount: " + CalculationService.formatCurrency(app.getRequestedAmount()));
            System.out.println("Tenure : " + app.getTenureMonths() + " mos | EMI: " + CalculationService.formatCurrency(app.getEmi()));

            boolean approve = ConsoleHelper.readBoolean("Do you want to APPROVE this loan?");
            String reviewerCode = ConsoleHelper.readString("Enter Reviewer Code/ID");
            String reason = "";
            if (!approve) {
                reason = ConsoleHelper.readString("Enter Mandatory Rejection Reason");
            }

            reviewService.decideReview(appId, approve, reviewerCode, reason);
            if (approve) {
                System.out.println("\n✅ Application approved! Moved to CREDIT_APPROVAL stage.");
            } else {
                System.out.println("\n🚫 Application rejected! Moved to REJECTED terminal stage.");
            }
        } catch (Exception e) {
            System.out.println("\n❌ Action failed: " + e.getMessage());
        }
    }

    private void processCreditApproval() {
        ConsoleHelper.printHeader("CREDIT APPROVAL ACTION");
        try {
            int appId = ConsoleHelper.readInt("Enter Application ID");
            Application app = appService.getApplicationById(appId);
            if (app == null) {
                System.out.println("❌ Application not found.");
                return;
            }
            if (app.getStatus() != Application.Status.CREDIT_APPROVAL) {
                System.out.println("❌ Application is not in CREDIT_APPROVAL status. Current status: " + app.getStatus());
                return;
            }

            boolean approve = ConsoleHelper.readBoolean("Grant final Credit Approval?");
            String officerCode = ConsoleHelper.readString("Enter Credit Officer Code/ID");
            String reason = "";
            if (!approve) {
                reason = ConsoleHelper.readString("Enter Mandatory Rejection Reason");
            }

            reviewService.decideCreditApproval(appId, approve, officerCode, reason);
            if (approve) {
                System.out.println("\n✅ Credit approved! Moved to DISBURSEMENT stage.");
                System.out.println("📄 [Document Generation Pointer]: Application Form generated.");
            } else {
                System.out.println("\n🚫 Credit approval denied! Moved to REJECTED terminal stage.");
            }
        } catch (Exception e) {
            System.out.println("\n❌ Action failed: " + e.getMessage());
        }
    }
}
