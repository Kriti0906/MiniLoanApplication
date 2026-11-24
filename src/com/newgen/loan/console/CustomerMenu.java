package com.newgen.loan.console;

import com.newgen.loan.model.Customer;
import com.newgen.loan.service.CalculationService;
import com.newgen.loan.service.CustomerService;

import java.time.LocalDate;
import java.util.List;

/**
 * Module 5 - Console Layer
 * Customer management menus: Register, Search, View Details.
 */
public class CustomerMenu {

    private final CustomerService customerService = new CustomerService();

    public void showMenu() {
        while (true) {
            ConsoleHelper.printHeader("CUSTOMER MANAGEMENT");
            System.out.println("1. Register New Customer");
            System.out.println("2. Search Customer by ID");
            System.out.println("3. Search Customer by Mobile Number");
            System.out.println("4. Search Customers by Name");
            System.out.println("5. Back to Main Menu");

            int choice = ConsoleHelper.readInt("Enter choice (1-5)");
            switch (choice) {
                case 1 -> registerCustomer();
                case 2 -> searchById();
                case 3 -> searchByMobile();
                case 4 -> searchByName();
                case 5 -> { return; }
                default -> System.out.println("❌ Invalid option. Try again.");
            }
        }
    }

    private void registerCustomer() {
        ConsoleHelper.printHeader("NEW CUSTOMER REGISTRATION");
        try {
            String name = ConsoleHelper.readString("Enter Customer Full Name");
            LocalDate dob = ConsoleHelper.readDate("Enter Date of Birth");
            String mobile = ConsoleHelper.readString("Enter 10-Digit Mobile Number");
            String address = ConsoleHelper.readString("Enter Residential Address");
            String kyc = ConsoleHelper.readString("Enter KYC Details (e.g., PAN/Aadhaar)");
            double income = ConsoleHelper.readDouble("Enter Monthly Net Income (INR)");
            double obligations = ConsoleHelper.readDouble("Enter Existing Monthly EMI Obligations (INR)");

            Customer c = new Customer();
            c.setName(name);
            c.setDateOfBirth(dob);
            c.setMobile(mobile);
            c.setAddress(address);
            c.setKycDetails(kyc);
            c.setMonthlyIncome(income);
            c.setExistingObligations(obligations);

            int customerId = customerService.registerCustomer(c);
            System.out.println("\n✅ Customer registered successfully! Assigned Customer ID: " + customerId);
        } catch (Exception e) {
            System.out.println("\n❌ Registration Failed: " + e.getMessage());
        }
    }

    private void searchById() {
        int id = ConsoleHelper.readInt("Enter Customer ID to search");
        try {
            Customer c = customerService.getCustomerById(id);
            if (c != null) {
                displayCustomerDetails(c);
            } else {
                System.out.println("❌ No customer found with ID: " + id);
            }
        } catch (Exception e) {
            System.out.println("❌ Database error: " + e.getMessage());
        }
    }

    private void searchByMobile() {
        String mobile = ConsoleHelper.readString("Enter 10-Digit Mobile Number");
        try {
            Customer c = customerService.getCustomerByMobile(mobile);
            if (c != null) {
                displayCustomerDetails(c);
            } else {
                System.out.println("❌ No customer found with Mobile: " + mobile);
            }
        } catch (Exception e) {
            System.out.println("❌ Database error: " + e.getMessage());
        }
    }

    private void searchByName() {
        String keyword = ConsoleHelper.readString("Enter Name Keyword");
        try {
            List<Customer> list = customerService.searchCustomers(keyword);
            if (list.isEmpty()) {
                System.out.println("❌ No matching customers found.");
            } else {
                System.out.println("\nFound " + list.size() + " customer(s):");
                for (Customer c : list) {
                    System.out.printf("  [ID: %d] %s | Mobile: %s | Income: %s\n",
                            c.getCustomerId(), c.getName(), c.getMobile(),
                            CalculationService.formatCurrency(c.getMonthlyIncome()));
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Database error: " + e.getMessage());
        }
    }

    public static void displayCustomerDetails(Customer c) {
        System.out.println("\n--- CUSTOMER PROFILE ---");
        System.out.println("Customer ID      : " + c.getCustomerId());
        System.out.println("Name             : " + c.getName());
        System.out.println("Date of Birth    : " + c.getDateOfBirth());
        System.out.println("Mobile           : " + c.getMobile());
        System.out.println("Address          : " + c.getAddress());
        System.out.println("KYC Info         : " + c.getKycDetails());
        System.out.println("Monthly Income   : " + CalculationService.formatCurrency(c.getMonthlyIncome()));
        System.out.println("Monthly Oblig.   : " + CalculationService.formatCurrency(c.getExistingObligations()));
    }
}
