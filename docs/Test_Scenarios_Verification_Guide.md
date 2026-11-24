# 🧪 Verification & Demonstration Guide (12 Test Scenarios)
**Mini Loan Origination Application | Newgen Campus Assignment**

This guide is designed to be executed **sequentially on a fresh/empty database** (`loan_db` initialized via `schema.sql`).
Assume reference **Business Date** is set to default: `2026-11-24`.

---

## Pre-requisite Setup (One-Time)
Run `schema.sql` in MySQL to make sure all tables exist:
```sql
SOURCE C:/Users/tyagi/OneDrive/Desktop/Loan/sql/schema.sql;
```

---

## 📌 Scenario 01: Product Minimum/Maximum Boundaries
**Goal:** Verify validation blocks amounts & tenures outside product limits for both `PERSONAL` (₹50k–₹5L, 12–60 mos) and `EDUCATION` (₹1L–₹10L, 12–84 mos).

### Step A: Register a test customer
- **Menu:** Option `1` (Customer Management) → Option `1` (Register New Customer)
- **Inputs:**
  - Name: `Rahul Sharma`
  - Date of Birth: `1995-05-15` (Age: 31)
  - Mobile: `9876543210`
  - Address: `Delhi`
  - KYC: `PANABC1234D`
  - Monthly Income: `50000`
  - Existing Obligations: `0`
- **Expected Output:**
  ```text
  ✅ Customer registered successfully! Assigned Customer ID: 1
  ```

### Step B: Test PERSONAL Product Below Minimum Amount (₹40,000)
- **Menu:** Option `2` (Loan Application Desk) → Option `1` (Create DRAFT)
- **Inputs:**
  - Customer ID: `1`
  - Product: `1` (`PERSONAL`)
  - Amount: `40000` *(Below ₹50,000 min)*
  - Tenure: `12`
  - Branch Code: `DEL01`
- **Output:** Draft created (e.g. App ID `1`).
- **Now Submit:** Option `2` → Option `3` (Submit Application) → Enter App ID: `1`
- **Expected Output:**
  ```text
  Status          : ❌ INELIGIBLE -> Moved to REJECTED stage
  Failed Rules / Reasons:
    • Requested amount ₹ 40,000.00 is outside product range (₹ 50,000.00 to ₹ 5,00,000.00).
  ```

### Step C: Test EDUCATION Product Exceeding Maximum Amount (₹12,00,000)
- **Menu:** Option `2` → Option `1` (Create DRAFT)
- **Inputs:**
  - Customer ID: `1`
  - Product: `2` (`EDUCATION`)
  - Amount: `1200000` *(Above ₹10,00,000 max)*
  - Tenure: `24`
  - Branch Code: `DEL01`
- **Submit:** Option `2` → Option `3` → Enter App ID: `2`
- **Expected Output:**
  ```text
  Status          : ❌ INELIGIBLE -> Moved to REJECTED stage
  Failed Rules / Reasons:
    • Requested amount ₹ 12,00,000.00 is outside product range (₹ 1,00,000.00 to ₹ 10,00,000.00).
  ```

---

## 📌 Scenario 02: Ages 20, 21, 60, and 61 (Reject vs. Accept Boundary Test)
**Goal:** Boundary rule: Completed age must be **21 to 60** at submission.
*(Reference Business Date: `2026-11-24`)*

| Test Case | DOB to Enter | Age on 2026-11-24 | Expected Result |
|---|---|---|---|
| **Age 20** | `2006-11-25` | 19 completed yrs, 364 days | ❌ **Rejected** (Outside 21–60) |
| **Age 21** | `2005-11-24` | Exactly 21 completed yrs | ✅ **Accepted** |
| **Age 60** | `1966-11-25` | 59 yrs, 364 days (turning 60) | ✅ **Accepted** |
| **Age 61** | `1965-11-20` | 61 completed yrs | ❌ **Rejected** (Outside 21–60) |

- **Menu:** Option `1` (Customer Management) → Option `1` (Register)
- **Sample Input for Age 20:**
  - Name: `Underage User`, DOB: `2006-11-25`, Mobile: `9811111111`, Income: `30000`, Obligations: `0`
- **Expected Output:**
  ```text
  ❌ Registration Failed: Customer age (19 years) must be between 21 and 60 completed years.
  ```

---

## 📌 Scenario 03: Duplicate Mobile & Duplicate Active Application Rule
**Goal:**
1. Block duplicate mobile registration.
2. Block customer from creating >1 active application for the same product.

### Test 3.1: Duplicate Mobile
- **Menu:** Option `1` → Option `1` (Register)
- **Input:** Mobile: `9876543210` *(Already used by Rahul Sharma in Scenario 01)*
- **Expected Output:**
  ```text
  ❌ Registration Failed: Customer with mobile 9876543210 already exists (ID: 1).
  ```

### Test 3.2: Duplicate Active Application
- Customer `1` currently has previous applications in `REJECTED` (terminal, not active).
- Create a valid DRAFT for Customer `1`:
  - **Menu:** Option `2` → Option `1`
  - Customer ID: `1`, Product: `1` (PERSONAL), Amount: `100000`, Tenure: `12`, Branch: `DEL01`
  - Created: App ID `3` *(Status: DRAFT = Active)*
- Immediately attempt to create **another** `PERSONAL` application for Customer `1`:
  - Customer ID: `1`, Product: `1` (PERSONAL), Amount: `200000`, Tenure: `24`
- **Expected Output:**
  ```text
  ❌ Could not create draft: Customer already has an active application (DRAFT, UNDER_REVIEW, or CREDIT_APPROVAL) for PERSONAL.
  ```

---

## 📌 Scenario 04: Worked EMI Example Matches Assignment Specification
**Goal:** Prove EMI arithmetic matches formula:
EMI = P * r * (1+r)^n / ((1+r)^n - 1)
For **PERSONAL Loan**: ₹100,000 at 12% annual interest over 12 months.
**Expected Specification EMI: ₹8,884.88**

- **Submit App ID `3`** created in Scenario 03:
- **Menu:** Option `2` → Option `3` (Submit Application) → App ID: `3`
- **Expected Output:**
  ```text
  --- SUBMISSION RESULT ---
  Proposed EMI    : ₹ 8,884.88
  Calculated FOIR : 17.77%
  Status          : ✅ ELIGIBLE -> Moved to UNDER_REVIEW stage
  ```

---

## 📌 Scenario 05: FOIR Exactly 50% and Above 50%
**Formula:** FOIR = (existing obligations + rounded EMI) / monthly net income * 100

### Test 5.1: Exactly 50% FOIR (Passes)
- Loan: ₹1,00,000, 12 months @ 12% → EMI = `₹8,884.88`.
- If Net Income = `₹20,000.00` and Existing Obligations = `₹1,115.12`:
  Total obligations = 1115.12 + 8884.88 = 10,000.00
  FOIR = (10000.00 / 20000.00) * 100 = 50.00%
- **Register Customer:**
  - Name: `Exact Fifty`, DOB: `1992-01-01`, Mobile: `9822222222`, Income: `20000`, Obligations: `1115.12`
  - Created Customer ID: `2`
- **Create & Submit Loan:**
  - Personal Loan: ₹100,000, 12 months.
- **Expected Output:**
  ```text
  Calculated FOIR : 50.0%
  Status          : ✅ ELIGIBLE -> Moved to UNDER_REVIEW stage
  ```

### Test 5.2: Above 50% FOIR (Fails - Assignment worked example)
- Customer Income = `₹30,000`, Existing Obligations = `₹7,000`.
- EMI = `₹8,884.88` → Total = `₹15,884.88`.
  FOIR = (15884.88 / 30000.00) * 100 = 52.95%
- **Register Customer:** Mobile: `9833333333`, Income: `30000`, Obligations: `7000`.
- **Submit Personal Loan (₹100,000, 12 mos):**
- **Expected Output:**
  ```text
  Calculated FOIR : 52.95%
  Status          : ❌ INELIGIBLE -> Moved to REJECTED stage
  Failed Rules / Reasons:
    • FOIR 52.95% exceeds maximum permitted limit of 50.0%.
  ```

---

## 📌 Scenario 06: Invalid State Transitions are Blocked
**Goal:** Enforce strict state machine transitions.

- Try to Disburse an application that is in `UNDER_REVIEW` (App ID `3`):
  - **Menu:** Option `4` (Loan Disbursement Desk) → Option `1` (Disburse Approved Loan)
  - Enter App ID: `3`
- **Expected Output:**
  ```text
  ❌ Application is not in DISBURSEMENT stage! Current status: UNDER_REVIEW
  ```
- Try to Edit an application once it has moved to `UNDER_REVIEW`:
  - **Menu:** Option `2` → Option `2` (Edit DRAFT Application)
  - Enter App ID: `3`
- **Expected Output:**
  ```text
  ❌ Only DRAFT applications can be edited. Current status: UNDER_REVIEW
  ```

---

## 📌 Scenario 07: Generated Application Form Trigger
**Goal:** Verify document generation pointer is produced when moving through `CREDIT_APPROVAL`.

1. **Approve Reviewer Step:**
   - **Menu:** Option `3` → Option `1` (Process UNDER_REVIEW)
   - App ID: `3`
   - Approve? `Y`
   - Reviewer Code: `REV_001`
   - **Output:** `✅ Application approved! Moved to CREDIT_APPROVAL stage.`

2. **Credit Officer Approval:**
   - **Menu:** Option `3` → Option `2` (Process CREDIT_APPROVAL)
   - App ID: `3`
   - Approve? `Y`
   - Credit Officer Code: `CRED_OFFICER_01`
   - **Expected Output:**
     ```text
     ✅ Credit approved! Moved to DISBURSEMENT stage.
     📄 [Document Generation Pointer]: Application Form generated.
     ```

---

## 📌 Scenario 08: Duplicate & Repeated Disbursement Attempts
**Goal:** A loan can be disbursed only once, with a unique reference number.

### 8.1 Successful Initial Disbursal:
- **Menu:** Option `4` → Option `1` (Disburse Approved Loan)
- App ID: `3`
- Reference Number: `DISB-REF-1001`
- Operator: `DISB_USER_01`
- **Expected Output:**
  ```text
  🎉 DISBURSEMENT COMPLETED SUCCESSFULLY!
  Disbursed Amount : ₹ 1,00,000.00
  Reference No.    : DISB-REF-1001
  Next Stage       : Moved to CORE_BANKING_PUSH
  ```

### 8.2 Attempt Repeated Disbursal on Same Loan:
- **Menu:** Option `4` → Option `1`
- App ID: `3`
- **Expected Output:**
  ```text
  ❌ Application is not in DISBURSEMENT stage! Current status: CORE_BANKING_PUSH
  ```

### 8.3 Attempt Duplicate Reference on Another Loan:
- Create another approved loan and enter reference `DISB-REF-1001`.
- **Expected Output:**
  ```text
  ❌ Disbursement failed: Disbursement reference 'DISB-REF-1001' has already been used.
  ```

---

## 📌 Scenario 09: Controlled Transaction Failure with Full Rollback
**Goal:** Prove multi-table modifications roll back cleanly if an error occurs mid-operation.

- Disbursing touches both `disbursement` and updates `loan_application` + `application_history`.
- In `DisbursementService.java`, if an invalid state or SQL exception is raised, `conn.rollback()` executes.
- **Verification via Menu:**
  - View application audit history for App ID `3` (Menu: Option `2` → Option `4`).
  - Notice the history is clean and consistent: `CREATED_DRAFT` → `SUBMIT` → `REVIEW_APPROVE` → `CREDIT_APPROVE` → `DISBURSE`. No half-written records exist.

---

## 📌 Scenario 10: Review Ages of Exactly 2 and 3 Days (Overdue Boundary)
**Rule:** Application becomes overdue when waiting strictly **> 2 calendar days** from submission date.

1. **Setup Case:**
   - Change Business Date to `2026-11-20` (Menu: Option `5` → Option `1` → `2026-11-20`).
   - Create and Submit a new Personal Loan (App ID `4`, submitted on `2026-11-20`).
   - Case is now in `UNDER_REVIEW`.

2. **Test Age = 2 Days:**
   - Change Business Date to `2026-11-22` (Difference = 2 calendar days).
   - Run Overdue Reviews Report (Menu: Option `5` → Option `4`):
   - **Expected Output:** `✅ No overdue review applications found.` (Not > 2).

3. **Test Age = 3 Days:**
   - Change Business Date to `2026-11-23` (Difference = 3 calendar days).
   - Run Overdue Reviews Report:
   - **Expected Output:**
     ```text
     App ID     App Number      Customer        Product      Amount           Days Waiting
     4          APP-xxxx        Rahul Sharma    PERSONAL     ₹ 1,00,000.00    3
     ```

---

## 📌 Scenario 11: Repeat Utility Execution Without Duplicate Alerts
**Goal:** Idempotency test — running the background utility multiple times must never duplicate alerts in `system_alerts` or double-push into `NG_CORE_BANKING`.

1. **Run Overdue Utility (1st Time):**
   - **Menu:** Option `6` (Background Utility Desk) → Option `1` (Run Overdue Reviews Utility NOW).
   - **Expected Output:**
     ```text
     [OverdueReviewUtility] Completed. Processed: 1, Alerts Created: 1, Exported to: pending_reviews_20261123.csv
     ```
2. **Run Overdue Utility (2nd Time Immediately):**
   - Select Option `1` again.
   - **Expected Output:**
     ```text
     [OverdueReviewUtility] Completed. Processed: 1, Alerts Created: 0, Exported to: pending_reviews_20261123.csv
     ```
     *(Alerts Created is 0 because `system_alerts` UNIQUE constraint prevented duplicate creation!)*

3. **Run CoreDataPush Utility:**
   - **Menu:** Option `6` → Option `2` (Run CoreDataPush Utility NOW).
   - **1st Run:** Pushes App ID `3` to `NG_CORE_BANKING` and advances status to `EXIT`.
   - **2nd Run:** Finds 0 records in `CORE_BANKING_PUSH`, ensuring zero duplicates.

---

## 📌 Scenario 12: Data Persists Correctly After Application Restart
**Goal:** Demonstrate persistence in MySQL DB.

1. Select Option `8` in Main Menu to **Exit** the application.
2. Relaunch using:
   ```cmd
   run.bat
   ```
3. Go to Option `2` (Loan Application Desk) → Option `4` (View Application Details & Audit History).
4. Enter Application ID: `3`.
5. **Expected Output:**
   - All data is intact: Customer info, requested amounts, EMI (`₹ 8,884.88`), FOIR, status (`EXIT`), and the complete chronological audit trail!
