# MVP Implementation Plan & Progress Tracker

Tài liệu này là roadmap triển khai MVP và checklist theo dõi tiến độ hằng ngày cho project Personal Finance / Expense Tracker.

Mục tiêu MVP:

- Người dùng có thể đăng ký, đăng nhập, đăng xuất an toàn.
- Người dùng có thể quản lý ví, danh mục và giao dịch thu/chi/chuyển tiền.
- Hệ thống tự cập nhật số dư ví theo giao dịch.
- Dashboard hiển thị tổng quan tài chính cá nhân.
- Frontend gọi API backend ổn định và có flow sử dụng cơ bản.

---

## Tracking Rules

Dùng các ký hiệu sau để cập nhật tiến độ:

- [x] Done: đã hoàn thành và đã verify tối thiểu.
- [~] In progress: đang làm hoặc mới hoàn thành một phần.
- [ ] Not started: chưa bắt đầu.

Nguyên tắc tick:

- Chỉ tick `[x]` khi task đã có code/tài liệu tương ứng và chạy được bước verify hợp lý.
- Dùng `[~]` cho task lớn đã làm một phần nhưng chưa đủ Definition of Done.
- Khi hoàn thành một checklist con quan trọng, cập nhật luôn trạng thái phase cha.

---

## Current Status

Backend foundation đã có nền tảng chạy được:

- PostgreSQL dev container đã setup.
- Flyway baseline + migration đã có.
- DB trigger `updated_at` đã có.
- JPA enums đã có đủ cho MVP hiện tại.
- JPA entities đã có đủ các entity chính theo schema MVP.

Current focus:

```text
Hoàn thiện Backend Foundation
-> tạo Spring Data repositories
-> chuẩn hóa API response/error
-> bắt đầu Authentication
```

---

## Phase 1. Backend Foundation

Mục tiêu: hoàn thiện nền tảng backend để các feature auth/domain có thể xây trên đó.

- [x] PostgreSQL dev container
- [x] Flyway baseline + migration
- [x] `updated_at` trigger ở DB layer
- [x] JPA enums
  - [x] `WalletType`
  - [x] `CategoryType`
  - [x] `TransactionType`
- [x] JPA entities
  - [x] `User`
  - [x] `Role`
  - [x] `Wallet`
  - [x] `Category`
  - [x] `Transaction`
  - [x] `RefreshToken`
- [x] Spring Data repositories
  - [x] `UserRepository`
  - [x] `RoleRepository`
  - [x] `WalletRepository`
  - [x] `CategoryRepository`
  - [x] `TransactionRepository`
  - [x] `RefreshTokenRepository`
- [x] Global exception handler
- [x] API response/error DTO chuẩn

Definition of Done:

- App build được bằng `./mvnw -DskipTests package`.
- JPA validate schema không lỗi khi app start.
- Entity map đúng schema trong Flyway migration.
- Repository có query cơ bản cần cho auth và domain.
- API response/error có format thống nhất cho controller sau này.

---

## Phase 2. Authentication

Mục tiêu: người dùng có thể đăng ký, đăng nhập, dùng JWT access token và refresh token.

- [x] BCrypt password hashing
- [x] Register API
  - [x] Request DTO
  - [x] Response DTO
  - [x] Validate username/email/password
  - [x] Hash password trước khi lưu
  - [x] Gán role mặc định `USER`
- [x] Login API
  - [x] Verify username/email + password
  - [x] Trả access token
  - [x] Tạo refresh token
  - [x] Cập nhật `last_login_at`
- [x] JWT access token
  - [x] Token service
  - [x] Claims cơ bản: user id, username/email, roles
  - [x] Expiration config
- [x] Refresh token flow
  - [x] Lưu refresh token vào DB
  - [x] Kiểm tra expired/revoked
  - [x] Cấp access token mới
- [x] Logout/revoke refresh token
- [x] Security filter + protected routes
  - [x] JWT filter
  - [x] Public routes: health, register, login, refresh, logout
  - [x] Protected routes: wallet/category/transaction/dashboard

Definition of Done:

- Register tạo user mới thành công và không lưu plain password.
- Login trả access token + refresh token hợp lệ.
- API protected từ chối request không có token.
- Refresh token bị revoke/expired không thể dùng lại.
- Auth errors trả về format chuẩn.

---

## Phase 3. Core Domain

Mục tiêu: người dùng quản lý dữ liệu tài chính cá nhân và hệ thống xử lý số dư chính xác.

- [x] Wallet CRUD
  - [x] Create wallet
  - [x] List active wallets theo user
  - [x] Update wallet info
  - [x] Soft delete wallet
- [x] Category CRUD
  - [x] Create category
  - [x] List categories theo user và type
  - [x] Update category
  - [x] Soft delete category
  - [ ] Default categories nếu cần
- [x] Income transaction
  - [x] Tạo giao dịch thu nhập
  - [x] Cộng tiền vào wallet
- [x] Expense transaction
  - [x] Tạo giao dịch chi tiêu
  - [x] Trừ tiền khỏi wallet
- [x] Transfer transaction
  - [x] Chuyển tiền giữa 2 wallet
  - [x] Tạo cặp transaction liên kết bằng `reference_transaction_id`
  - [x] Cập nhật cả ví nguồn và ví đích
- [x] Soft delete behavior
  - [x] Không hard delete dữ liệu tài chính
  - [x] List API chỉ trả record `deleted_at IS NULL`
  - [x] Soft delete transaction có xử lý lại balance
- [x] Balance update rules
  - [x] Validate amount > 0
  - [x] Không cho expense/transfer vượt số dư nếu rule MVP yêu cầu
  - [x] Transaction update/delete không làm lệch balance

Definition of Done:

- [x] CRUD wallet/category chạy được qua API.
- [x] Income/expense/transfer cập nhật balance đúng.
- [x] Dữ liệu của user này không lộ sang user khác.
- [x] Soft delete không làm mất row trong DB.
- Các lỗi domain trả về message rõ ràng theo format chuẩn.

---

## Phase 4. Dashboard

Mục tiêu: hiển thị tổng quan tài chính dựa trên dữ liệu thật của user.

- [x] Total balance
- [x] Income/expense summary
  - [x] Theo tháng hiện tại
  - [x] Theo khoảng thời gian nếu cần
- [x] Recent transactions
- [x] Top spending categories
- [x] Monthly statistics

Definition of Done:

- Dashboard API chỉ đọc dữ liệu của user hiện tại.
- Số liệu tổng hợp khớp với wallet và transaction trong DB.
- Query có filter thời gian rõ ràng.
- Response đủ dữ liệu để frontend render dashboard MVP.

---

## Phase 5. Frontend

Mục tiêu: người dùng thao tác được các flow chính trên UI.

- [x] App shell + routing
  - [x] Layout chính
  - [x] Protected routes
  - [x] Navigation
- [x] Auth pages
  - [x] Register page
  - [x] Login page
  - [x] Logout action
  - [x] Token persistence
- [x] Dashboard page
  - [x] Total balance card
  - [x] Income/expense summary
  - [x] Recent transactions
  - [x] Top categories/monthly statistics
- [x] Wallet pages
  - [x] List wallets
  - [x] Create wallet
  - [x] Edit wallet
  - [x] Delete wallet
- [x] Transaction pages
  - [x] List transactions
  - [x] Create income
  - [x] Create expense
  - [x] Create transfer
  - [x] Filter/search basic
  - [x] Edit transaction (đúng loại giao dịch)
  - [x] Delete transaction
- [x] API services
  - [x] Axios client
  - [x] Auth token interceptor
  - [x] Error handling
- [x] Zustand stores
  - [x] Auth store
  - [x] Wallet/transaction/dashboard state theo MVP (local page state + shared store cần thiết)

Definition of Done:

- User có thể đi từ register/login đến dashboard.
- Frontend gọi API backend thật, không dùng mock cho flow MVP chính.
- Protected route hoạt động.
- Form có loading/error/success state cơ bản.
- `npm run build` pass.

---

## Phase 6. Final Integration & QA

Mục tiêu: ráp toàn bộ MVP thành luồng dùng được từ đầu đến cuối.

- [~] End-to-end happy path
  - [x] Register
  - [x] Login
  - [x] Create wallet
  - [x] Create income
  - [x] Create expense
  - [x] Create transfer
  - [x] View dashboard
  - [x] Logout
- [~] API error cases
  - [x] Unauthorized
  - [x] Validation failed
  - [x] Not found
  - [x] Business rule violation
- [x] Data ownership checks
- [~] Docker/local run instructions verified
- [~] README cập nhật cách chạy MVP
- [~] Final build checks
  - [x] Backend package
  - [x] Frontend build

Definition of Done:

- MVP chạy được local theo README.
- Backend và frontend build pass.
- Một user không truy cập được dữ liệu user khác.
- Các flow chính dùng được từ UI.
- Checklist này phản ánh đúng trạng thái cuối cùng.

---

## Current Next Steps

Thứ tự làm tiếp khuyến nghị:

1. Tạo Spring Data repositories cho các entity chính.
2. Thêm API response/error DTO chuẩn và global exception handler.
3. Bắt đầu Authentication với BCrypt, register API và login API.
4. Sau khi auth ổn, chuyển sang Wallet CRUD.

Ghi chú trạng thái hiện tại:

- `JPA enums` đã done vì đã có `WalletType`, `CategoryType`, `TransactionType`.
- `JPA entities` đã done vì đã có `User`, `Role`, `Wallet`, `Category`, `Transaction`, `RefreshToken`.
- Chưa tick repository/auth/core domain cho tới khi có code và verify tương ứng.
