# Test Batch Report

- Batch: `2026-05-15T10-16-37Z-phase-3-transaction-balance-test`
- Scope: Phase 3 transaction flows for `income`, `expense`, `transfer`, plus regression check for auth and wallet/category CRUD.
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
- protected route with valid JWT
- refresh token flow
- logout + revoked refresh token

2. Wallet/category regression
- wallet CRUD still works
- category CRUD still works
- pagination/search/filter behavior still works

3. Transaction flow
- create income transaction
- income increases wallet balance
- create expense transaction
- expense decreases wallet balance
- create transfer transaction
- transfer decreases source wallet balance
- transfer increases destination wallet balance
- transfer creates 2 transaction rows linked by `reference_transaction_id`

4. Negative cases
- income with wrong category type returns `400`
- expense over balance returns `INSUFFICIENT_BALANCE`
- transfer over balance returns `INSUFFICIENT_BALANCE`
- transfer to the same wallet returns `400`

## Notes

- No failing test remained in final batch.
- `package` showed a non-blocking warning about generated annotation files cleanup in `target/`, but build still completed successfully.
