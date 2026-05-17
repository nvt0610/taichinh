# DTO Layer Study

## 1) DTO là gì?

`dto` là object trao đổi dữ liệu giữa API và client, tách khỏi entity DB.

## 2) Nhóm DTO trong project

- Request DTO: nhận input (`Create...Request`, `Update...Request`).
- Response DTO: trả output (`...Response`).
- Common DTO: `ApiResponse`, `PaginationResponse`, `ListQueryParams`.

## 3) Vai trò trong flow

`Controller` nhận request DTO -> `Service` xử lý -> map entity sang response DTO -> trả client.

## 4) Quy tắc dùng DTO

- Không expose entity trực tiếp ra API.
- Request DTO có validation annotation.
- Response DTO chỉ chứa field client cần.
- Không để field nhạy cảm trong response.

## 5) Happy vs unhappy case

Happy:
- request DTO pass validation
- service map đúng response DTO

Unhappy:
- parse lỗi enum/date/json -> 400
- vi phạm annotation validate -> `VALIDATION_FAILED`

## 6) Dấu hiệu DTO layer hoàn chỉnh

- Mỗi endpoint có request/response rõ ràng.
- Validation message đủ dễ hiểu.
- Mapping nhất quán giữa các module.
