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
- JPA entities đang làm dở: mới có `Wallet`.

Current focus:

```text
Hoàn thiện Backend Foundation
-> hoàn thiện JPA entities còn lại
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
- [~] JPA entities
  - [ ] `User`
  - [ ] `Role`
  - [x] `Wallet`
  - [ ] `Category`
  - [ ] `Transaction`
  - [ ] `RefreshToken`
- [ ] Spring Data repositories
  - [ ] `UserRepository`
  - [ ] `RoleRepository`
  - [ ] `WalletRepository`
  - [ ] `CategoryRepository`
  - [ ] `TransactionRepository`
  - [ ] `RefreshTokenRepository`
- [ ] Global exception handler
- [ ] API response/error DTO chuẩn

Definition of Done:

- App build được bằng `./mvnw -DskipTests package`.
- JPA validate schema không lỗi khi app start.
- Entity map đúng schema trong Flyway migration.
- Repository có query cơ bản cần cho auth và domain.
- API response/error có format thống nhất cho controller sau này.

---

## Phase 2. Authentication

Mục tiêu: người dùng có thể đăng ký, đăng nhập, dùng JWT access token và refresh token.

- [ ] BCrypt password hashing
- [ ] Register API
  - [ ] Request DTO
  - [ ] Response DTO
  - [ ] Validate username/email/password
  - [ ] Hash password trước khi lưu
  - [ ] Gán role mặc định `USER`
- [ ] Login API
  - [ ] Verify username/email + password
  - [ ] Trả access token
  - [ ] Tạo refresh token
  - [ ] Cập nhật `last_login_at`
- [ ] JWT access token
  - [ ] Token service
  - [ ] Claims cơ bản: user id, username/email, roles
  - [ ] Expiration config
- [ ] Refresh token flow
  - [ ] Lưu refresh token vào DB
  - [ ] Kiểm tra expired/revoked
  - [ ] Cấp access token mới
- [ ] Logout/revoke refresh token
- [ ] Security filter + protected routes
  - [ ] JWT filter
  - [ ] Public routes: health, register, login, refresh
  - [ ] Protected routes: wallet/category/transaction/dashboard

Definition of Done:

- Register tạo user mới thành công và không lưu plain password.
- Login trả access token + refresh token hợp lệ.
- API protected từ chối request không có token.
- Refresh token bị revoke/expired không thể dùng lại.
- Auth errors trả về format chuẩn.

---

## Phase 3. Core Domain

Mục tiêu: người dùng quản lý dữ liệu tài chính cá nhân và hệ thống xử lý số dư chính xác.

- [ ] Wallet CRUD
  - [ ] Create wallet
  - [ ] List active wallets theo user
  - [ ] Update wallet info
  - [ ] Soft delete wallet
- [ ] Category CRUD
  - [ ] Create category
  - [ ] List categories theo user và type
  - [ ] Update category
  - [ ] Soft delete category
  - [ ] Default categories nếu cần
- [ ] Income transaction
  - [ ] Tạo giao dịch thu nhập
  - [ ] Cộng tiền vào wallet
- [ ] Expense transaction
  - [ ] Tạo giao dịch chi tiêu
  - [ ] Trừ tiền khỏi wallet
- [ ] Transfer transaction
  - [ ] Chuyển tiền giữa 2 wallet
  - [ ] Tạo cặp transaction liên kết bằng `reference_transaction_id`
  - [ ] Cập nhật cả ví nguồn và ví đích
- [ ] Soft delete behavior
  - [ ] Không hard delete dữ liệu tài chính
  - [ ] List API chỉ trả record `deleted_at IS NULL`
  - [ ] Soft delete transaction có xử lý lại balance
- [ ] Balance update rules
  - [ ] Validate amount > 0
  - [ ] Không cho expense/transfer vượt số dư nếu rule MVP yêu cầu
  - [ ] Transaction update/delete không làm lệch balance

Definition of Done:

- CRUD wallet/category chạy được qua API.
- Income/expense/transfer cập nhật balance đúng.
- Dữ liệu của user này không lộ sang user khác.
- Soft delete không làm mất row trong DB.
- Các lỗi domain trả về message rõ ràng theo format chuẩn.

---

## Phase 4. Dashboard

Mục tiêu: hiển thị tổng quan tài chính dựa trên dữ liệu thật của user.

- [ ] Total balance
- [ ] Income/expense summary
  - [ ] Theo tháng hiện tại
  - [ ] Theo khoảng thời gian nếu cần
- [ ] Recent transactions
- [ ] Top spending categories
- [ ] Monthly statistics

Definition of Done:

- Dashboard API chỉ đọc dữ liệu của user hiện tại.
- Số liệu tổng hợp khớp với wallet và transaction trong DB.
- Query có filter thời gian rõ ràng.
- Response đủ dữ liệu để frontend render dashboard MVP.

---

## Phase 5. Frontend

Mục tiêu: người dùng thao tác được các flow chính trên UI.

- [ ] App shell + routing
  - [ ] Layout chính
  - [ ] Protected routes
  - [ ] Navigation
- [ ] Auth pages
  - [ ] Register page
  - [ ] Login page
  - [ ] Logout action
  - [ ] Token persistence
- [ ] Dashboard page
  - [ ] Total balance card
  - [ ] Income/expense summary
  - [ ] Recent transactions
  - [ ] Top categories/monthly statistics
- [ ] Wallet pages
  - [ ] List wallets
  - [ ] Create wallet
  - [ ] Edit wallet
  - [ ] Delete wallet
- [ ] Transaction pages
  - [ ] List transactions
  - [ ] Create income
  - [ ] Create expense
  - [ ] Create transfer
  - [ ] Filter/search basic
- [ ] API services
  - [ ] Axios client
  - [ ] Auth token interceptor
  - [ ] Error handling
- [ ] Zustand stores
  - [ ] Auth store
  - [ ] Wallet store
  - [ ] Transaction/dashboard state nếu cần

Definition of Done:

- User có thể đi từ register/login đến dashboard.
- Frontend gọi API backend thật, không dùng mock cho flow MVP chính.
- Protected route hoạt động.
- Form có loading/error/success state cơ bản.
- `npm run build` pass.

---

## Phase 6. Final Integration & QA

Mục tiêu: ráp toàn bộ MVP thành luồng dùng được từ đầu đến cuối.

- [ ] End-to-end happy path
  - [ ] Register
  - [ ] Login
  - [ ] Create wallet
  - [ ] Create income
  - [ ] Create expense
  - [ ] Create transfer
  - [ ] View dashboard
  - [ ] Logout
- [ ] API error cases
  - [ ] Unauthorized
  - [ ] Validation failed
  - [ ] Not found
  - [ ] Business rule violation
- [ ] Data ownership checks
- [ ] Docker/local run instructions verified
- [ ] README cập nhật cách chạy MVP
- [ ] Final build checks
  - [ ] Backend package
  - [ ] Frontend build

Definition of Done:

- MVP chạy được local theo README.
- Backend và frontend build pass.
- Một user không truy cập được dữ liệu user khác.
- Các flow chính dùng được từ UI.
- Checklist này phản ánh đúng trạng thái cuối cùng.

---

## Current Next Steps

Thứ tự làm tiếp khuyến nghị:

1. Hoàn thiện các JPA entities còn lại: `User`, `Role`, `Category`, `Transaction`, `RefreshToken`.
2. Tạo Spring Data repositories cho các entity chính.
3. Thêm API response/error DTO chuẩn và global exception handler.
4. Bắt đầu Authentication với BCrypt, register API và login API.
5. Sau khi auth ổn, chuyển sang Wallet CRUD.

Ghi chú trạng thái hiện tại:

- `JPA enums` đã done vì đã có `WalletType`, `CategoryType`, `TransactionType`.
- `JPA entities` đang `[~]` vì mới có `Wallet`, chưa có đủ entity theo schema MVP.
- Chưa tick repository/auth/core domain cho tới khi có code và verify tương ứng.
