# Study Notes

Folder này chứa tài liệu học tập cho các khái niệm Spring Boot và kiến trúc backend đang dùng trong project.

Nguyên tắc:
- Trước khi thêm layer/class quan trọng mới, kiểm tra tài liệu ở đây để tránh trùng.
- Nếu đã có note phù hợp thì cập nhật note cũ thay vì tạo file mới.
- Note cần bám vào code thật của project, hạn chế lý thuyết xa ngữ cảnh.

## Study Theo Layer

- `CONTROLLER_LAYER_STUDY.md`: Vai trò controller, HTTP boundary, validation, response mapping.
- `SERVICE_LAYER_STUDY.md`: Vai trò service, business rules, transaction boundary, ownership checks.
- `SPRING_DATA_REPOSITORY_STUDY.md`: Spring Data repository, query methods, và vị trí trong flow.
- `JPA_ENUM_AND_ENTITY_STUDY.md`: Entity/enums và mapping JPA.
- `DTO_LAYER_STUDY.md`: Request/response DTO, validation, mapping nguyên tắc.
- `API_RESPONSE_STUDY.md`: Contract response thành công, pagination metadata.
- `EXCEPTION_HANDLING_STUDY.md`: ErrorCode, BusinessException, GlobalExceptionHandler.
- `SECURITY_LAYER_STUDY.md`: JWT/security filter chain, authn/authz flow.
- `CONFIG_LAYER_STUDY.md`: Bean/config lifecycle, security/password/cors config.

## Study Theo Luồng Nghiệp Vụ

- `BACKEND_E2E_WORKFLOW_STUDY.md`: End-to-end backend flow (happy/unhappy path).
- `JWT_ACCESS_TOKEN_STUDY.md`: Access token claims/lifecycle.
- `REFRESH_TOKEN_FLOW_STUDY.md`: Refresh token flow và revoke/logout.
- `WALLET_CATEGORY_CRUD_STUDY.md`: CRUD owner-scoped cho wallet/category.
- `TRANSACTION_BALANCE_FLOW_STUDY.md`: Income/expense/transfer và cập nhật số dư.
- `DASHBOARD_SUMMARY_STUDY.md`: Dashboard summary/recent/top-category/monthly stats.

## Ghi chú lịch sử

- `API_RESPONSE_AND_EXCEPTION_STUDY.md` là note cũ dạng gộp; đã tách thành 2 file riêng (`API_RESPONSE_STUDY.md` và `EXCEPTION_HANDLING_STUDY.md`) để học theo layer rõ hơn.
