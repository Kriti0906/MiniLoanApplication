package com.newgen.loan.service;

import com.newgen.loan.dao.CustomerDAO;
import com.newgen.loan.model.Customer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 4 - Service Layer
 * Business validations for customers:
 * - Age must be 21 to 60 completed years on submission date.
 * - Mobile must be exactly 10 digits and unique.
 * - DOB cannot be in the future.
 * - Financial inputs validation.
 */
public class CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public List<String> validateCustomer(Customer customer, LocalDate referenceDate) {
        List<String> errors = new ArrayList<>();

        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            errors.add("Customer name is required.");
        }

        // Mobile validation: exactly 10 digits
        if (customer.getMobile() == null || !customer.getMobile().matches("^[0-9]{10}$")) {
            errors.add("Mobile number must be exactly 10 digits.");
        }

        // DOB & Age validation
        if (customer.getDateOfBirth() == null) {
            errors.add("Date of birth is required.");
        } else {
            if (customer.getDateOfBirth().isAfter(referenceDate)) {
                errors.add("Date of birth cannot be in the future.");
            } else {
                int age = Period.between(customer.getDateOfBirth(), referenceDate).getYears();
                if (age < ProductConfig.MIN_AGE || age > ProductConfig.MAX_AGE) {
                    errors.add("Customer age (" + age + " years) must be between " +
                               ProductConfig.MIN_AGE + " and " + ProductConfig.MAX_AGE + " completed years.");
                }
            }
        }

        // Income validation
        if (customer.getMonthlyIncome() <= 0) {
            errors.add("Monthly net income must be positive.");
        }

        // Obligations validation
        if (customer.getExistingObligations() < 0) {
            errors.add("Existing monthly obligations cannot be negative.");
        }

        return errors;
    }

    public int registerCustomer(Customer customer) throws Exception {
        LocalDate businessDate = BusinessDateConfig.getBusinessDate();
        List<String> errors = validateCustomer(customer, businessDate);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }

        // Mobile uniqueness check
        Customer existing = customerDAO.findByMobile(customer.getMobile());
        if (existing != null) {
            throw new IllegalArgumentException("Customer with mobile " + customer.getMobile() + " already exists (ID: " + existing.getCustomerId() + ").");
        }

        return customerDAO.createCustomer(customer);
    }

    public Customer getCustomerById(int customerId) throws SQLException {
        return customerDAO.findById(customerId);
    }

    public Customer getCustomerByMobile(String mobile) throws SQLException {
        return customerDAO.findByMobile(mobile);
    }

    public List<Customer> searchCustomers(String name) throws SQLException {
        return customerDAO.searchByName(name);
    }
}
