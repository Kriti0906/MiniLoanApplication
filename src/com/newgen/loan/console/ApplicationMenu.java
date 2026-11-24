package com.newgen.loan.console;

import com.newgen.loan.dao.ApplicationHistoryDAO;
import com.newgen.loan.model.Application;
import com.newgen.loan.model.ApplicationHistory;
import com.newgen.loan.service.ApplicationService;
import com.newgen.loan.service.CalculationService;
import com.newgen.loan.service.EligibilityService;

import java.util.List;

/**
 * Module 5 - Console Layer
 * Application operations: Create Draft, Edit Draft, Submit with Auto-Eligibility, View Details & History.
 */
public class ApplicationMenu {

    private final ApplicationService appService = new ApplicationService();
    private final ApplicationHistoryDAO historyDAO = new ApplicationHistoryDAO();

    public void showMenu() {
        while (true) {
            ConsoleHelper.printHeader("LOAN APPLICATION OPERATIONS");
            System.out.println("1. Create DRAFT Loan Application");
            System.out.println("2. Edit DRAFT Application");
            System.out.println("3. Submit Application for Review (Automatic Eligibility Check)");
            System.out.println("4. View Application Details & Audit History");
            System.out.println("5. View All Applications of a Customer");
            System.out.println("6. Back to Main Menu");

            int choice = ConsoleHelper.readInt("Enter choice (1-6)");
            switch (choice) {
                case 1 -> createDraft();
                case 2 -> editDraft();
                case 3 -> submitApplication();
                case 4 -> viewDetails();
                case 5 -> viewCustomerApplications();
                case 6 -> { return; }
                default -> System.out.println("❌ Invalid option. Try again.");
            }
        }
    }

    private void createDraft() {
        ConsoleHelper.printHeader("CREATE DRAFT LOAN APPLICATION");
        try {
            int custId = ConsoleHelper.readInt("Enter Customer ID");
            System.out.println("Select Product Type:");
            System.out.println("  1. PERSONAL");
            System.out.println("  2. EDUCATION");
            int pChoice = ConsoleHelper.readInt("Choice (1-2)");
            Application.ProductType pType = (pChoice == 1) ? Application.ProductType.PERSONAL : Application.ProductType.EDUCATION;

            double amount = ConsoleHelper.readDouble("Enter Requested Loan Amount (INR)");
            int tenure = ConsoleHelper.readInt("Enter Loan Tenure (Months)");
            String branch = ConsoleHelper.readString("Enter Branch Code (e.g. DEL01)");

            int appId = appService.createDraftApplication(custId, pType, amount, tenure, branch);
            System.out.println("\n✅ DRAFT Application created successfully! Application ID: " + appId);
        } catch (Exception e) {
            System.out.println("\n❌ Could not create draft: " + e.getMessage());
        }
    }

    private void editDraft() {
        ConsoleHelper.printHeader("EDIT DRAFT APPLICATION");
        try {
            int appId = ConsoleHelper.readInt("Enter Application ID to edit");
            Application app = appService.getApplicationById(appId);
            if (app == null) {
                System.out.println("❌ Application not found.");
                return;
            }
            if (app.getStatus() != Application.Status.DRAFT) {
                System.out.println("❌ Only DRAFT applications can be edited. Current status: " + app.getStatus());
                return;
            }

            System.out.println("Current Requested Amount : " + CalculationService.formatCurrency(app.getRequestedAmount()));
            System.out.println("Current Tenure (Months)  : " + app.getTenureMonths());

            double newAmount = ConsoleHelper.readDouble("Enter New Requested Amount");
            int newTenure = ConsoleHelper.readInt("Enter New Tenure (Months)");

            appService.updateDraft(appId, newAmount, newTenure);
            System.out.println("\n✅ Draft application updated successfully!");
        } catch (Exception e) {
            System.out.println("\n❌ Update failed: " + e.getMessage());
        }
    }

    private void submitApplication() {
        ConsoleHelper.printHeader("SUBMIT APPLICATION");
        try {
            int appId = ConsoleHelper.readInt("Enter Application ID to submit");
            EligibilityService.EligibilityResult result = appService.submitApplication(appId);

            System.out.println("\n--- SUBMISSION RESULT ---");
            System.out.println("Proposed EMI    : " + CalculationService.formatCurrency(result.getEmi()));
            System.out.println("Calculated FOIR : " + result.getFoir() + "%");

            if (result.isEligible()) {
                System.out.println("Status          : ✅ ELIGIBLE -> Moved to UNDER_REVIEW stage");
            } else {
                System.out.println("Status          : ❌ INELIGIBLE -> Moved to REJECTED stage");
                System.out.println("Failed Rules / Reasons:");
                for (String r : result.getFailureReasons()) {
                    System.out.println("  • " + r);
                }
            }
        } catch (Exception e) {
            System.out.println("\n❌ Submission failed: " + e.getMessage());
        }
    }

    private void viewDetails() {
        int appId = ConsoleHelper.readInt("Enter Application ID");
        try {
            Application a = appService.getApplicationById(appId);
            if (a == null) {
                System.out.println("❌ Application not found.");
                return;
            }

            System.out.println("\n--- APPLICATION DETAILS ---");
            System.out.println("Application ID    : " + a.getApplicationId());
            System.out.println("Customer ID       : " + a.getCustomerId());
            System.out.println("Product           : " + a.getProductType());
            System.out.println("Status            : " + a.getStatus());
            System.out.println("Requested Amount  : " + CalculationService.formatCurrency(a.getRequestedAmount()));
            System.out.println("Tenure (Months)   : " + a.getTenureMonths());
            System.out.println("Interest Rate     : " + a.getAnnualInterestRate() + "%");
            System.out.println("Proposed EMI      : " + CalculationService.formatCurrency(a.getEmi()));
            System.out.println("Calculated FOIR   : " + a.getFoir() + "%");
            System.out.println("Created At        : " + a.getCreatedAt());
            System.out.println("Submitted At      : " + a.getSubmittedAt());
            System.out.println("Decided At        : " + a.getDecidedAt());

            // Display audit history
            List<ApplicationHistory> histList = historyDAO.findByApplicationId(appId);
            System.out.println("\n--- AUDIT TRAIL / STATUS HISTORY ---");
            if (histList.isEmpty()) {
                System.out.println("No history records found.");
            } else {
                for (ApplicationHistory h : histList) {
                    System.out.printf("  [%s] Action: %s | %s -> %s | By: %s | Note: %s\n",
                            h.getPerformedAt(), h.getAction(), h.getOldStatus(),
                            h.getNewStatus(), h.getPerformedBy(), h.getRemarks());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error fetching details: " + e.getMessage());
        }
    }

    private void viewCustomerApplications() {
        int custId = ConsoleHelper.readInt("Enter Customer ID");
        try {
            List<Application> list = appService.getApplicationsByCustomer(custId);
            if (list.isEmpty()) {
                System.out.println("❌ No applications found for this customer.");
            } else {
                System.out.println("\nCustomer's Applications (" + list.size() + "):");
                for (Application a : list) {
                    System.out.printf("  [App ID: %d] Product: %s | Amount: %s | Tenure: %d mos | Status: %s\n",
                            a.getApplicationId(), a.getProductType(),
                            CalculationService.formatCurrency(a.getRequestedAmount()),
                            a.getTenureMonths(), a.getStatus());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}
