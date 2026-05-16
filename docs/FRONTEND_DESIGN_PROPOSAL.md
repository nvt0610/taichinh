# Đề xuất thiết kế Frontend (Phase 5)

## 1. Mục tiêu

Chốt hướng thiết kế frontend trước khi bắt đầu code Phase 5 để:

- giữ giao diện nhất quán;
- code nhanh hơn, ít phải sửa lại phần nền;
- chừa sẵn đường để mở rộng theme trong các phase sau.

Tài liệu này tập trung vào:

- định hướng thiết kế;
- cấu trúc màn hình;
- hệ thống theme để người dùng có thể đổi màu chủ đạo;
- contract cơ bản cho component và state ở mức MVP.

---

## 2. Định hướng thiết kế

Tên hướng thiết kế: **Minimalist Finance Workspace**.

Nguyên tắc:

- tối giản, dễ scan dữ liệu, ưu tiên thao tác;
- không làm kiểu landing page hoặc marketing page;
- visual restrained: border nhẹ, shadow rất ít;
- màu sắc dùng để truyền đạt ý nghĩa nghiệp vụ, ví dụ income, expense, transfer.

Cảm giác mong muốn:

- gọn, sạch, tập trung vào dữ liệu;
- người dùng vào là hiểu tình hình tài chính và biết nên thao tác gì tiếp theo.

---

## 3. Cấu trúc thông tin

Luồng chính:

`Auth -> Dashboard -> Wallets -> Transactions`

App shell:

- Desktop: sidebar bên trái + topbar + vùng nội dung chính.
- Mobile: topbar + bottom navigation.

Các page MVP:

- Login;
- Register;
- Dashboard;
- Wallets;
- Transactions;
- Logout action.

---

## 4. Chiến lược tùy biến theme

Người dùng có thể chọn màu chủ đạo (`primary`). Sau đó hệ thống hỗ trợ 2 chế độ:

1. `Auto palette`

- Hệ thống tự sinh màu phụ từ `primary`.
- Phù hợp với người dùng muốn đơn giản, nhanh và an toàn về mặt tương phản.

2. `Manual palette`

- Người dùng có thể tự chỉnh thêm secondary, accent và các màu trạng thái.
- Dành cho người dùng muốn cá nhân hóa sâu hơn.

### Guardrails bắt buộc

- Giới hạn saturation/lightness để tránh palette khó đọc.
- Tự động kiểm tra contrast cho text, button và badge.
- Có nút `Reset to Recommended` để quay về palette hệ thống đề xuất.

---

## 5. Design token contract (MVP v1)

Dùng CSS variables theo 3 lớp:

1. Base tokens

- `--color-*`: các màu gốc.

2. Semantic tokens

- `--bg`
- `--surface`
- `--surface-muted`
- `--text`
- `--text-muted`
- `--border`
- `--primary`
- `--primary-contrast`
- `--income`
- `--expense`
- `--transfer`
- `--warning`
- `--success`
- `--danger`

3. Component tokens

- `--button-primary-bg`
- `--button-primary-fg`
- `--card-bg`
- `--input-bg`
- `--input-border`
- `--chart-income`
- `--chart-expense`
- `--chart-transfer`

Cơ chế đổi theme:

- Set trên root: `data-theme="minimal-mint"` hoặc theme khác.
- Component không hardcode màu trực tiếp, chỉ dùng semantic tokens hoặc component tokens.

---

## 6. Theme mặc định đề xuất

Ban đầu có thể ship 3 theme:

1. `minimal-mint`: theme mặc định, sạch, dễ nhìn, hợp app tài chính.
2. `minimal-graphite`: trung tính và nghiêm túc hơn.
3. `minimal-sunrise`: ấm và thân thiện hơn.

Lưu ý:

- tránh palette bị dominant tím/purple;
- ưu tiên contrast cao cho bảng dữ liệu và form.

---

## 7. Quy tắc UI cho MVP

- Radius tối đa: `8px`.
- Shadow: rất nhẹ, ưu tiên border/divider.
- Typography:
  - 1 font cho UI text;
  - 1 font cho số tiền/metric.
- Chart:
  - ít màu;
  - nhấn mạnh insight chính;
  - không dùng kiểu rainbow chart.

Các state bắt buộc trên màn hình, form và list:

- loading;
- empty;
- error;
- success;
- disabled.

---

## 8. Core components (MVP)

- `AppShell`
- `ThemeProvider` / `themeStore`
- `Button`
- `Input`
- `Select`
- `SegmentedControl` cho Income / Expense / Transfer
- `StatCard`
- `MoneyText`
- `WalletCard`
- `TransactionTable`
- `TransactionFormModal` hoặc `TransactionFormDrawer`
- `EmptyState`
- `InlineAlert`
- `LoadingSkeleton`

---

## 9. State và persistence cho theme

Khuyến nghị:

- Theme state dùng Zustand.
- Persist bằng `localStorage`.
- Lưu các trường:
  - `themeMode`: `auto` hoặc `manual`;
  - `primaryColor`;
  - `generatedPalette` nếu dùng auto mode;
  - `customPalette` nếu dùng manual mode.

Fallback:

- Nếu parse lỗi hoặc palette không hợp lệ, quay về `minimal-mint`.

---

## 10. Thứ tự triển khai đề xuất

1. Tạo token system + default themes.
2. Tạo AppShell + routing + protected routes.
3. Tạo auth pages.
4. Tạo dashboard page.
5. Tạo wallets page.
6. Tạo transactions page + form flows.
7. Hoàn thiện theme picker theo hướng auto/manual.
8. Polish states + đảm bảo `npm run build` pass.

---

## 11. Ngoài phạm vi hiện tại

- Dark mode đầy đủ.
- Đồng bộ theme theo account trên cloud.
- Advanced palette editor như gradient editor hoặc shade matrix.

---

## 12. Tóm tắt quyết định

- Chọn giao diện minimalist, tập trung vào dữ liệu.
- Chọn kiến trúc có sẵn đường cho người dùng đổi màu chủ đạo.
- Hỗ trợ cả `Auto palette` và `Manual palette`.
- Ưu tiên code theo semantic tokens để mở rộng dễ hơn.
