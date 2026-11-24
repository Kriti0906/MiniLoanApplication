-- =============================================================================
-- Six Required SQL Reports for Mini Loan Origination Application
-- Newgen Campus Assignment
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Report 1: Application Register
-- Application number, customer name, product, amount, EMI, status and submission date.
-- Parameterised: Filter by product (? or NULL for all) and status (? or NULL for all).
-- -----------------------------------------------------------------------------
SELECT 
    la.application_number,
    c.name AS customer_name,
    la.product,
    la.requested_amount,
    la.proposed_emi,
    la.status,
    la.submitted_at
FROM loan_application la
JOIN customer c ON la.customer_id = c.customer_id
WHERE (? IS NULL OR la.product = ?)
  AND (? IS NULL OR la.status = ?)
ORDER BY la.submitted_at DESC;

-- -----------------------------------------------------------------------------
-- Report 2: Product Pipeline
-- Part A: Application count and sum of requested amounts grouped by product and status.
-- Part B: Application count and sum of requested amounts grouped by branch and product.
-- -----------------------------------------------------------------------------
-- Part A: Grouped by Product and Status
SELECT 
    product,
    status,
    COUNT(*) AS total_applications,
    COALESCE(SUM(requested_amount), 0.00) AS total_requested_amount
FROM loan_application
GROUP BY product, status
ORDER BY product, status;

-- Part B: Grouped by Branch and Product
SELECT 
    branch_code,
    branch_name,
    product,
    COUNT(*) AS total_applications,
    COALESCE(SUM(requested_amount), 0.00) AS total_requested_amount
FROM loan_application
GROUP BY branch_code, branch_name, product
ORDER BY branch_code, product;

-- -----------------------------------------------------------------------------
-- Report 3: Daily / Monthly / Quarterly Disbursement
-- Date, count and total amount for a supplied date range.
-- Parameterised: Start Date (?) and End Date (?).
-- Also includes branch-wise disbursement view.
-- -----------------------------------------------------------------------------
-- Date-wise breakdown
SELECT 
    DATE(d.disbursed_at) AS disbursement_date,
    COUNT(d.disbursement_id) AS total_disbursed_count,
    COALESCE(SUM(d.amount), 0.00) AS total_disbursed_amount
FROM disbursement d
WHERE DATE(d.disbursed_at) BETWEEN ? AND ?
GROUP BY DATE(d.disbursed_at)
ORDER BY disbursement_date ASC;

-- Branch-wise disbursement within date range
SELECT 
    la.branch_code,
    la.branch_name,
    COUNT(d.disbursement_id) AS total_disbursed_count,
    COALESCE(SUM(d.amount), 0.00) AS total_disbursed_amount
FROM disbursement d
JOIN loan_application la ON d.application_id = la.application_id
WHERE DATE(d.disbursed_at) BETWEEN ? AND ?
GROUP BY la.branch_code, la.branch_name
ORDER BY la.branch_code ASC;

-- -----------------------------------------------------------------------------
-- Report 4: Overdue Reviews
-- Current UNDER_REVIEW applications older than 2 calendar days from submission.
-- Parameterised: Reference / Current Business Date (?).
-- Returns customer name, amount, and exact calendar days waiting.
-- -----------------------------------------------------------------------------
SELECT 
    la.application_id,
    la.application_number,
    c.name AS customer_name,
    la.product,
    la.requested_amount,
    la.submitted_at,
    DATEDIFF(?, DATE(la.submitted_at)) AS days_waiting
FROM loan_application la
JOIN customer c ON la.customer_id = c.customer_id
WHERE la.status = 'UNDER_REVIEW'
  AND DATEDIFF(?, DATE(la.submitted_at)) > 2
ORDER BY days_waiting DESC;

-- -----------------------------------------------------------------------------
-- Report 5: Customers Without Disbursement
-- Customers with at least one application and no disbursement across any application.
-- Returns each customer once.
-- -----------------------------------------------------------------------------
SELECT DISTINCT 
    c.customer_id,
    c.name AS customer_name,
    c.mobile,
    c.monthly_income,
    c.created_at
FROM customer c
JOIN loan_application la ON c.customer_id = la.customer_id
WHERE c.customer_id NOT IN (
    SELECT DISTINCT la2.customer_id
    FROM loan_application la2
    JOIN disbursement d ON la2.application_id = d.application_id
)
ORDER BY c.customer_id ASC;

-- -----------------------------------------------------------------------------
-- Report 6: Repeat Applicants
-- Customers with more than one application, including application count
-- and total requested amount across all applications.
-- Uses GROUP BY and HAVING.
-- -----------------------------------------------------------------------------
SELECT 
    c.customer_id,
    c.name AS customer_name,
    c.mobile,
    COUNT(la.application_id) AS total_applications,
    COALESCE(SUM(la.requested_amount), 0.00) AS total_requested_amount
FROM customer c
JOIN loan_application la ON c.customer_id = la.customer_id
GROUP BY c.customer_id, c.name, c.mobile
HAVING COUNT(la.application_id) > 1
ORDER BY total_applications DESC, total_requested_amount DESC;
