# Mini Loan Origination Application


A beginner-level, console-driven loan origination system implementing an end-to-end loan lifecycle.

---

## Project Structure

```
loan-origination/
├── src/com/newgen/loan/
│   ├── model/      ← Module 1: Data models (POJOs)
│   ├── db/         ← Module 2: Database setup & JDBC connection
│   ├── dao/        ← Module 3: Data Access Objects (CRUD)
│   ├── service/    ← Module 4: Business logic, validations, EMI/FOIR
│   ├── console/    ← Module 5: Console UI / menus
│   ├── reports/    ← Module 6: SQL Reports
│   └── utility/    ← Module 7: Background utilities
├── sql/
│   ├── schema.sql
│   └── reports.sql
└── docs/
```

## Loan Lifecycle

```
DRAFT → UNDER_REVIEW → CREDIT_APPROVAL → DISBURSEMENT → CORE_BANKING_PUSH → EXIT
                     ↘ REJECTED         ↗ REJECTED
```

## Tech Stack

- Java 17
- MySQL

## Key Formulas

**EMI:** `P × r × (1+r)^n / ((1+r)^n - 1)` where `r = annualRate / 1200`

**FOIR:** `(obligations + EMI) / monthlyIncome × 100` — must be ≤ 50%

---
