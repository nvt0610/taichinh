# Kế hoạch triển khai Frontend (Phase 5)

Tài liệu này chia Phase 5 Frontend thành các phase nhỏ để code theo thứ tự rõ ràng, giống cách backend đã đi theo checklist.

Nguồn bám theo:

- `docs/IMPLEMENTATION_CHECKLIST.md`
- `docs/FRONTEND_DESIGN_PROPOSAL.md`
- stack frontend hiện tại: React + Vite + TypeScript + React Router + Axios + Zustand + React Hook Form + Recharts

---

## Phase FE-0. Frontend baseline và design tokens

Mục tiêu: tạo nền UI có sẵn khả năng đổi theme trước khi code các màn hình nghiệp vụ.

Tasks:

- [x] Tạo app entry files nếu frontend skeleton chưa có (`main.tsx`, `App.tsx`).
- [x] Tạo global CSS reset và layout baseline.
- [x] Tạo CSS variables cho các lớp design token.
- [x] Tạo 3 theme mặc định:
  - `minimal-mint`
  - `minimal-graphite`
  - `minimal-sunrise`
- [x] Tạo theme utilities để set `data-theme`.
- [x] Tạo `themeStore` bằng Zustand và persist vào `localStorage`.
- [x] Tạo fallback về `minimal-mint` khi localStorage lỗi.

Exit criteria:

- [x] App render được màn hình placeholder.
- [x] Đổi theme bằng state/localStorage được.
- [x] Không component nào hardcode màu nghiệp vụ trực tiếp.
- [x] `npm run build` pass.

---

## Phase FE-1. App shell, routing và protected routes

Mục tiêu: tạo khung ứng dụng để tất cả page sau này lắp vào.

Tasks:

- [x] Cấu hình React Router.
- [x] Tạo route groups:
  - public routes: login/register;
  - protected routes: dashboard/wallets/transactions.
- [x] Tạo `AppShell`.
- [x] Tạo desktop sidebar.
- [x] Tạo mobile bottom navigation.
- [x] Tạo topbar với user/account area.
- [x] Tạo protected route wrapper.
- [x] Tạo fallback route/not-found page.

Exit criteria:

- [x] User chưa login bị đẩy về login.
- [x] User đã login vào được dashboard route.
- [x] Navigation hoạt động trên desktop/mobile.
- [x] Layout không vỡ trên mobile.
- [x] `npm run build` pass.

---

## Phase FE-2. API client và auth foundation

Mục tiêu: frontend gọi backend thật qua service layer, không gọi raw Axios rải rác trong components.

Tasks:

- [x] Tạo Axios client trong `frontend/src/services`.
- [x] Cấu hình base URL từ env.
- [x] Tạo auth token interceptor.
- [x] Tạo response/error normalizer.
- [x] Tạo auth service:
  - register;
  - login;
  - refresh;
  - logout.
- [x] Tạo `authStore`.
- [x] Persist token/user state cần thiết.
- [x] Xử lý expired token tối thiểu cho MVP.

Exit criteria:

- [~] Login/register gọi backend thật. Chưa test runtime vì backend không chạy ở `localhost:8080` trong batch này.
- [x] Token được gắn vào request protected.
- [x] Error từ backend hiển thị được dưới dạng user-readable.
- [x] Logout clear local state.
- [x] `npm run build` pass.

---

## Phase FE-3. Auth pages

Mục tiêu: user có thể đăng ký, đăng nhập và đăng xuất bằng UI thật.

Tasks:

- [x] Tạo `LoginPage`.
- [x] Tạo `RegisterPage`.
- [x] Dùng React Hook Form cho form state.
- [x] Validate field cơ bản ở client.
- [x] Hiển thị loading state khi submit.
- [x] Hiển thị error state khi backend reject.
- [x] Redirect sau khi login/register thành công.
- [x] Thêm logout action trong AppShell.

Exit criteria:

- [~] User register/login được từ UI. Chưa test runtime vì backend không chạy ở `localhost:8080` trong batch này.
- [x] Lỗi validation và lỗi backend hiển thị rõ.
- [x] Submit button disabled khi đang loading.
- [x] User có thể logout và quay về login.
- [x] `npm run build` pass.

---

## Phase FE-4. Dashboard page

Mục tiêu: hiển thị tổng quan tài chính dựa trên dashboard API thật.

Tasks:

- [x] Tạo dashboard service.
- [x] Tạo dashboard page layout.
- [x] Tạo `StatCard`.
- [x] Tạo `MoneyText`.
- [x] Hiển thị total balance.
- [x] Hiển thị income/expense summary.
- [x] Hiển thị recent transactions.
- [x] Hiển thị top categories.
- [x] Hiển thị monthly statistics bằng Recharts.
- [x] Thêm loading/empty/error states.

Exit criteria:

- [~] Dashboard render bằng dữ liệu backend thật. Chưa test runtime vì backend chưa chạy trong batch FE-4.
- [x] Số tiền format nhất quán.
- [x] Chart không bị vỡ layout trên desktop/mobile.
- [x] Empty state rõ khi chưa có dữ liệu.
- [x] `npm run build` pass.

---

## Phase FE-5. Wallet pages

Mục tiêu: user quản lý ví tiền từ UI.

Tasks:

- [x] Tạo wallet service.
- [x] Tạo wallet types.
- [x] Tạo `walletStore` nếu cần share state. Không cần store riêng ở FE-5, dùng local state trong page.
- [x] Tạo wallets list page.
- [x] Tạo `WalletCard` hoặc wallet table compact.
- [x] Tạo create wallet form.
- [x] Tạo edit wallet form.
- [x] Tạo delete confirmation.
- [x] Thêm loading/empty/error/success states.

Exit criteria:

- [~] User list/create/edit/delete wallet được. Chưa test runtime trong batch FE-5.
- [x] Delete có confirmation.
- [x] Form errors hiển thị rõ.
- [x] Wallet state refresh sau mutation.
- [x] `npm run build` pass.

---

## Phase FE-6. Transaction pages

Mục tiêu: user ghi thu/chi/chuyển tiền và xem lịch sử giao dịch.

Tasks:

- [x] Tạo transaction service.
- [x] Tạo transaction types.
- [x] Tạo transactions list page.
- [x] Tạo filter/search cơ bản.
- [x] Tạo `TransactionTable`.
- [x] Tạo `TransactionFormModal` hoặc `TransactionFormDrawer`.
- [x] Tạo segmented control:
  - Income;
  - Expense;
  - Transfer.
- [x] Tạo create income flow.
- [x] Tạo create expense flow.
- [x] Tạo create transfer flow.
- [ ] Tạo edit/delete transaction nếu backend contract sẵn sàng.
- [x] Thêm loading/empty/error/success states.

Exit criteria:

- [~] User tạo được income/expense/transfer từ UI. Chưa test runtime trong batch FE-6.
- [x] List refresh sau khi tạo giao dịch.
- [x] Filter/search cơ bản hoạt động.
- [x] Không cho submit form thiếu field bắt buộc.
- [x] `npm run build` pass.

---

## Phase FE-7. Theme picker

Mục tiêu: user có thể chọn màu chủ đạo và app đổi theme theo token system.

Tasks:

- [x] Tạo theme picker UI.
- [x] Cho user chọn preset theme.
- [x] Cho user chọn `primaryColor`.
- [x] Tạo auto palette generator từ `primaryColor`.
- [~] Tạo manual palette mode nếu MVP còn thời gian.
- [x] Thêm contrast guardrails tối thiểu.
- [x] Thêm `Reset to Recommended`.
- [x] Persist theme settings vào `localStorage`.

Exit criteria:

- User đổi màu chủ đạo và UI cập nhật ngay.
- Reload page vẫn giữ theme.
- Màu text/button vẫn đọc được với palette được sinh.
- Reset đưa về theme mặc định ổn định.
- `npm run build` pass.

---

## Phase FE-8. Integration QA và polish

Mục tiêu: ráp toàn bộ frontend với backend thành flow MVP dùng được.

Tasks:

- [x] Test flow register -> login -> dashboard.
- [x] Test protected routes.
- [x] Test wallet CRUD.
- [x] Test create income/expense/transfer.
- [x] Test dashboard sau khi có transaction.
- [~] Test mobile viewport.
- [x] Test empty/loading/error states.
- [x] Chạy `npm run build`.
- [x] Ghi test batch report vào `docs/test-results/`.
- [x] Cập nhật `docs/IMPLEMENTATION_CHECKLIST.md`.

Exit criteria:

- Main MVP flow dùng được từ UI.
- Frontend gọi API backend thật, không dùng mock cho flow chính.
- Không có lỗi build.
- Test report ghi rõ pass/fail và lỗi đã fix.
- Checklist Phase 5 được cập nhật đúng status.

---

## Thứ tự code khuyến nghị

Nếu cần bắt đầu code ngay, thứ tự nên là:

1. FE-0 Token system + theme baseline.
2. FE-1 AppShell + routing.
3. FE-2 API client + auth store.
4. FE-3 Auth pages.
5. FE-4 Dashboard.
6. FE-5 Wallets.
7. FE-6 Transactions.
8. FE-7 Theme picker.
9. FE-8 Integration QA.

Lý do: theme và shell là nền móng, auth mở khóa protected APIs, sau đó mới vào dashboard/wallet/transaction để tránh lặp lại layout và service pattern.

---

## Ưu tiên MVP

Must-have:

- App shell + protected routes.
- Login/register/logout.
- API client + token handling.
- Dashboard read-only.
- Wallet CRUD.
- Transaction create/list/filter.
- Basic loading/error/empty states.
- Build pass.

Should-have:

- Theme preset picker.
- User primary color picker.
- Auto palette generation.
- Basic mobile polish.

Nice-to-have:

- Manual palette editor đầy đủ.
- Dark mode đầy đủ.
- Advanced chart interactions.
- User profile/settings page riêng.

---

## Ghi chú khi triển khai

- Component không hardcode màu trực tiếp; dùng semantic tokens.
- API calls đi qua `services/`, không đặt raw Axios trong page components.
- Form dùng React Hook Form để giữ code gọn và dễ validate.
- Zustand chỉ dùng cho state cần share/persist; server data có thể fetch trong page/service nếu MVP đơn giản.
- Mỗi batch test explicit cần tạo report trong `docs/test-results/` theo rule của repo.
