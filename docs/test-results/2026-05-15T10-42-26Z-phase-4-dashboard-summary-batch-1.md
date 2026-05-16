# Phase 4 Dashboard Summary Batch 1

- Timestamp: `2026-05-15T10:42:26Z`
- Scope: lát cắt đầu tiên của Phase 4
- Mục tiêu:
  - `Total balance`
  - `Income/expense summary`
  - mặc định theo tháng hiện tại
  - hỗ trợ custom date range

## Những gì đã test

### 1. Maven test suite

Command:

```bash
./mvnw test
```

Kết quả cuối: `PASS`

- Tests run: `7`
- Failures: `0`
- Errors: `0`
- Skipped: `0`

### 2. Package build

Command:

```bash
./mvnw -DskipTests package
```

Kết quả cuối: `PASS`

## Lỗi phát hiện trong batch này

Có 1 lỗi test trong lần chạy đầu:

- File: `backend/src/test/java/com/taichinh/app/domain/DashboardSummaryIntegrationTest.java`
- Triệu chứng: assertion của `totalBalance` fail
- Nguyên nhân: test đang kỳ vọng `totalBalance` bị filter theo `startDate/endDate`
- Kết luận sau khi kiểm tra service:
  - `totalBalance` được tính từ `wallet.balance` hiện tại
  - `income/expense/netCashFlow` mới là phần bị filter theo kỳ

## Fix đã áp trong batch này

- Cập nhật assertion của `DashboardSummaryIntegrationTest`
- Giữ nguyên logic service vì behavior hiện tại là đúng với ý nghĩa `total balance`
- Bổ sung study note để tránh hiểu sai semantics của field này về sau

## Kết luận batch

Batch này `PASS`.

Lát cắt đầu tiên của Phase 4 hiện đã xong:

- `Total balance`
- `Income/expense summary`
- `Theo tháng hiện tại`
- `Theo khoảng thời gian nếu cần`

Các phần chưa làm trong Phase 4:

- `Recent transactions`
- `Top spending categories`
- `Monthly statistics`
