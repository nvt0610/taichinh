# Phase 1-4 Audit Hardening Test

- Timestamp: `2026-05-15T12:03:26Z`
- Scope: bịt 2 khe hở còn lại sau audit Phase 1 đến Phase 4

## Mục tiêu của batch này

1. Verify `updated_at` trigger thật sự chạy ở DB layer, không chỉ dựa vào JPA lifecycle callback.
2. Verify ownership/isolation giữa 2 user khác nhau cho:
   - wallet
   - category
   - transaction
   - dashboard

## Test mới thêm

### 1. `DatabaseTriggerIntegrationTest`

File:

- `backend/src/test/java/com/taichinh/app/foundation/DatabaseTriggerIntegrationTest.java`

Ý nghĩa:

- tạo `user`
- tạo `wallet`
- đọc `updated_at`
- update row trực tiếp bằng SQL qua `JdbcTemplate`
- đọc lại `updated_at`
- assert timestamp sau lớn hơn timestamp trước

Kết luận:

- trigger `set_updated_at()` ở DB đang hoạt động thật

### 2. `DataOwnershipIntegrationTest`

File:

- `backend/src/test/java/com/taichinh/app/domain/DataOwnershipIntegrationTest.java`

Ý nghĩa:

- tạo user A và user B
- user A tạo wallet/category/transaction
- user B cố đọc resource detail của user A
- user B gọi list API
- user B gọi dashboard API

Kết quả mong đợi:

- detail API của user B với resource của user A trả `404`
- list API của user B không thấy dữ liệu của user A
- dashboard của user B không ăn dữ liệu của user A

## Commands đã chạy

### 1. Package build

```bash
./mvnw -DskipTests package
```

Kết quả: `PASS`

### 2. Full backend test suite

```bash
./mvnw test
```

Kết quả: `PASS`

- Tests run: `10`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

## Kết luận batch

Batch này `PASS`.

Sau batch hardening này:

- khe hở verify của `updated_at` trigger đã được lấp
- khe hở verify ownership/isolation đa user cho Phase 3-4 đã được lấp
- audit Phase 1 đến Phase 4 hiện đã có bằng chứng test tốt hơn để chốt mức hoàn thiện
