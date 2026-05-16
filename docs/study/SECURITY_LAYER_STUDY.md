# Security Layer Study

## 1) Security layer là gì?

`security` đảm bảo xác thực và phân quyền trước khi request vào business layer.

## 2) Thành phần chính

- JWT access token parser/validator.
- Security filter chain.
- Authenticated user resolver (ví dụ provider lấy `userId` từ principal).
- Rule bảo vệ endpoint public/protected.

## 3) Flow xác thực

1. Request vào filter chain.
2. Filter đọc Bearer token.
3. Token hợp lệ -> set `Authentication` vào `SecurityContext`.
4. Controller/Service đọc user hiện tại từ context.

## 4) Happy vs unhappy case

Happy:
- token hợp lệ, chưa hết hạn
- endpoint được phép truy cập
- request đi tiếp xuống controller

Unhappy:
- thiếu token/sai chữ ký/hết hạn -> 401
- authenticated nhưng không đủ quyền -> 403
- principal không parse được user id hợp lệ -> xử lý như unauthorized/bad request theo config

## 5) Dấu hiệu security hoàn chỉnh

- Endpoint public/protected khai báo rõ.
- Không tin `userId` từ body/query khi route cần ownership.
- Lỗi authn/authz trả format ổn định.
- Token lifecycle (login/refresh/logout) khớp với auth flow.
