package com.newgen.loan.console;

import com.newgen.loan.service.BusinessDateConfig;

import java.time.LocalDate;

/**
 * Module 5 - Console Layer
 * Main interactive console entry point for the Loan Origination Application.
 */
public class MainMenu {

    private final CustomerMenu customerMenu = new CustomerMenu();
    private final ApplicationMenu applicationMenu = new ApplicationMenu();
    private final ReviewMenu reviewMenu = new ReviewMenu();
    private final DisbursementMenu disbursementMenu = new DisbursementMenu();
    private final ReportMenu reportMenu = new ReportMenu();
    private final UtilityMenu utilityMenu = new UtilityMenu();

    public void start() {
        while (true) {
            ConsoleHelper.printHeader("🏦 MINI LOAN ORIGINATION SYSTEM");
            System.out.println("Current Business Date: " + BusinessDateConfig.getBusinessDate());
            System.out.println("─".repeat(50));
            System.out.println("1. Customer Management (Register / Search)");
            System.out.println("2. Loan Application Desk (Draft / Submit / Track)");
            System.out.println("3. Under Review & Credit Approval Desk");
            System.out.println("4. Loan Disbursement Desk");
            System.out.println("5. Executive SQL Reports Desk (6 Reports)");
            System.out.println("6. Background Utility Desk (Overdue & Core Push)");
            System.out.println("7. Configure Business Date");
            System.out.println("8. Exit Application");
            System.out.println("─".repeat(50));

            int choice = ConsoleHelper.readInt("Select an option (1-8)");
            switch (choice) {
                case 1 -> customerMenu.showMenu();
                case 2 -> applicationMenu.showMenu();
                case 3 -> reviewMenu.showMenu();
                case 4 -> disbursementMenu.showMenu();
                case 5 -> reportMenu.showMenu();
                case 6 -> utilityMenu.showMenu();
                case 7 -> configureBusinessDate();
                case 8 -> {
                    com.newgen.loan.utility.UtilityRunner.shutdownScheduler();
                    System.out.println("\n👋 Thank you for using the Mini Loan Origination System. Goodbye!");
                    return;
                }
                default -> System.out.println("❌ Invalid selection! Please choose between 1 and 8.");
            }
        }
    }

    private void configureBusinessDate() {
        ConsoleHelper.printHeader("CONFIGURE BUSINESS DATE");
        System.out.println("Current Business Date: " + BusinessDateConfig.getBusinessDate());
        System.out.println("1. Set Custom Date (YYYY-MM-DD)");
        System.out.println("2. Reset to System Today (" + LocalDate.now() + ")");
        System.out.println("3. Back");

        int opt = ConsoleHelper.readInt("Option (1-3)");
        if (opt == 1) {
            LocalDate customDate = ConsoleHelper.readDate("Enter New Business Date");
            BusinessDateConfig.setBusinessDate(customDate);
            System.out.println("✅ Business Date successfully updated to: " + customDate);
        } else if (opt == 2) {
            BusinessDateConfig.resetToToday();
            System.out.println("✅ Business Date reset to today: " + BusinessDateConfig.getBusinessDate());
        }
    }

    public static void main(String[] args) {
        new MainMenu().start();
    }
}
