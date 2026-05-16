# Test Batch Report

- Batch: `2026-05-15T10-27-25Z-phase-3-completion-test`
- Scope: complete Phase 3 verification for transaction history, transaction update/delete, soft delete rollback behavior, and regression coverage for auth + wallet/category CRUD.
- Environment: local backend with PostgreSQL container on `localhost:5433`

## Commands

```bash
cd backend
./mvnw test
./mvnw -DskipTests package
```

## Result

- Overall status: `PASS`
- `./mvnw test`: `PASS`
- `./mvnw -DskipTests package`: `PASS`

## What Was Verified

1. Auth regression
- register
- login
- protected route with JWT
- refresh
- logout

2. Wallet/category regression
- wallet CRUD still works
- category CRUD still works
- pagination/search/filter still works

3. Transaction create flow
- income updates wallet balance
- expense updates wallet balance
- transfer creates linked pair and updates both wallets

4. Transaction history
- `GET /api/transactions` returns only active rows
- filtering by `walletId`, `type`, `q`, and sorting works for tested cases

5. Transaction update
- income update recalculates balance correctly
- expense update recalculates balance correctly
- transfer update recalculates both wallets correctly

6. Transaction soft delete
- deleting income/expense/transfer does not hard delete DB rows
- `deleted_at` is set
- delete rolls back wallet balance correctly
- deleted rows are excluded from history/detail active queries

7. Guard rails
- wrong category type rejected
- expense over balance rejected
- transfer over balance rejected
- same-wallet transfer rejected
- deleting consumed income that would make wallet negative is rejected

## Final Status

- Phase 3 backend behavior is complete for current MVP scope, excluding optional default category seed and dashboard work from Phase 4.
