package com.newgen.loan.console;

import com.newgen.loan.reports.ReportService;

import java.time.LocalDate;

/**
 * Module 6 - Console Layer
 * Exposes the six SQL reports through an interactive menu.
 */
public class ReportMenu {

    private final ReportService reportService = new ReportService();

    public void showMenu() {
        while (true) {
            ConsoleHelper.printHeader("📊 EXECUTIVE SQL REPORTS DESK");
            System.out.println("1. Application Register (Filter by Product & Status)");
            System.out.println("2. Product Pipeline (By Product/Status & Branch/Product)");
            System.out.println("3. Daily / Monthly / Quarterly Disbursement Summary");
            System.out.println("4. Overdue Reviews Report (> 2 Calendar Days in UNDER_REVIEW)");
            System.out.println("5. Customers with Applications but No Disbursement");
            System.out.println("6. Repeat Applicants (GROUP BY + HAVING count > 1)");
            System.out.println("7. Back to Main Menu");

            int choice = ConsoleHelper.readInt("Select report to run (1-7)");
            switch (choice) {
                case 1 -> {
                    String prod = ConsoleHelper.readString("Enter Product filter (PERSONAL / EDUCATION / ALL)");
                    String status = ConsoleHelper.readString("Enter Status filter (e.g. DRAFT, UNDER_REVIEW, APPROVED, or ALL)");
                    reportService.runApplicationRegister(prod, status);
                }
                case 2 -> reportService.runProductPipeline();
                case 3 -> {
                    LocalDate start = ConsoleHelper.readDate("Enter Report Start Date");
                    LocalDate end = ConsoleHelper.readDate("Enter Report End Date");
                    reportService.runDisbursementReport(start, end);
                }
                case 4 -> reportService.runOverdueReviews();
                case 5 -> reportService.runCustomersWithoutDisbursement();
                case 6 -> reportService.runRepeatApplicants();
                case 7 -> { return; }
                default -> System.out.println("❌ Invalid option. Try again.");
            }
        }
    }
}
