-- =============================================================================
-- Database Schema for Mini Loan Origination Application
-- Newgen Campus Assignment
-- Compatible with MySQL 8.0+
-- =============================================================================

CREATE DATABASE IF NOT EXISTS loan_db;
USE loan_db;

-- 1. Customers Table
CREATE TABLE IF NOT EXISTS customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    mobile VARCHAR(10) NOT NULL UNIQUE,
    address VARCHAR(255),
    kyc_details VARCHAR(50),
    monthly_income DECIMAL(15, 2) NOT NULL,
    existing_monthly_obligations DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_mobile_length CHECK (CHAR_LENGTH(mobile) = 10)
);

-- 2. Loan Applications Table
CREATE TABLE IF NOT EXISTS loan_application (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    application_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    product ENUM('PERSONAL', 'EDUCATION') NOT NULL,
    branch_code VARCHAR(20) NOT NULL DEFAULT 'DEL01',
    branch_name VARCHAR(100) NOT NULL DEFAULT 'Main Branch',
    requested_amount DECIMAL(15, 2) NOT NULL,
    tenure_months INT NOT NULL,
    monthly_income DECIMAL(15, 2) NOT NULL,
    existing_monthly_obligations DECIMAL(15, 2) NOT NULL,
    annual_interest_rate DECIMAL(5, 2) NOT NULL,
    proposed_emi DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    foir_percentage DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    eligible BOOLEAN NOT NULL DEFAULT FALSE,
    eligibility_reasons TEXT,
    status ENUM('DRAFT', 'UNDER_REVIEW', 'CREDIT_APPROVAL', 'REJECTED', 'DISBURSEMENT', 'CORE_BANKING_PUSH', 'EXIT') NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    submitted_at DATETIME NULL,
    decided_at DATETIME NULL,
    reviewer_code VARCHAR(50) NULL,
    decision_reason VARCHAR(255) NULL,
    CONSTRAINT fk_app_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE RESTRICT
);

-- 3. Disbursements Table
CREATE TABLE IF NOT EXISTS disbursement (
    disbursement_id INT AUTO_INCREMENT PRIMARY KEY,
    application_id INT NOT NULL UNIQUE,
    reference_number VARCHAR(100) NOT NULL UNIQUE,
    amount DECIMAL(15, 2) NOT NULL,
    disbursed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_disb_app FOREIGN KEY (application_id) REFERENCES loan_application(application_id) ON DELETE RESTRICT
);

-- 4. Application History Table (Audit Trail)
CREATE TABLE IF NOT EXISTS application_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    application_id INT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    performed_by VARCHAR(50) NOT NULL,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remarks VARCHAR(255),
    CONSTRAINT fk_hist_app FOREIGN KEY (application_id) REFERENCES loan_application(application_id) ON DELETE RESTRICT
);

-- 5. System Alerts Table
CREATE TABLE IF NOT EXISTS system_alerts (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    application_id INT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_app_alert UNIQUE (application_id, alert_type),
    CONSTRAINT fk_alert_app FOREIGN KEY (application_id) REFERENCES loan_application(application_id) ON DELETE RESTRICT
);

-- 6. Utility Runs Table
CREATE TABLE IF NOT EXISTS utility_run (
    run_id INT AUTO_INCREMENT PRIMARY KEY,
    utility_name VARCHAR(100) NOT NULL,
    started_at DATETIME NOT NULL,
    completed_at DATETIME NULL,
    result ENUM('SUCCESS', 'FAILURE') NOT NULL,
    applications_processed INT DEFAULT 0,
    alerts_created INT DEFAULT 0,
    error_message TEXT NULL
);

-- 7. Core Banking Push Table
CREATE TABLE IF NOT EXISTS NG_CORE_BANKING (
    core_banking_id INT AUTO_INCREMENT PRIMARY KEY,
    CustomerID INT NOT NULL,
    ApplicationID INT NOT NULL UNIQUE,
    CustomerName VARCHAR(100) NOT NULL,
    Address VARCHAR(255),
    DOB DATE NOT NULL,
    KYCDetails VARCHAR(50),
    ApprovedAmount DECIMAL(15, 2) NOT NULL,
    DisbursedAmount DECIMAL(15, 2) NOT NULL,
    DisbursedDate DATETIME NOT NULL,
    pushed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_core_app FOREIGN KEY (ApplicationID) REFERENCES loan_application(application_id)
);
