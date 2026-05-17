# Service Layer Study

## 1) Service là gì trong project này?

`service` là tầng xử lý nghiệp vụ nằm giữa `controller` và `repository`.

Trong backend này, `service` chịu trách nhiệm:
- nhận dữ liệu đã qua validate cơ bản từ `controller` (request DTO/path param),
- kiểm tra rule nghiệp vụ (quyền sở hữu dữ liệu, trạng thái hợp lệ, ràng buộc domain),
- phối hợp nhiều repository hoặc nhiều bước xử lý trong một use case,
- map entity <-> DTO response,
- ném exception domain (ví dụ `NotFound`, `Forbidden`, `BadRequest`) để tầng global exception handler trả response lỗi thống nhất.

Service **không** nên làm việc của controller (HTTP concern) hoặc repository (query chi tiết).

## 2) Service hoạt động như thế nào trong project này?

Luồng điển hình:
1. `Controller` gọi hàm service tương ứng với endpoint.
2. `Service` đọc context user hiện tại (nếu cần) từ security context/JWT principal.
3. `Service` tải dữ liệu qua `repository`.
4. `Service` kiểm tra nghiệp vụ:
- dữ liệu có tồn tại không,
- dữ liệu có thuộc user hiện tại không,
- action có hợp lệ với trạng thái hiện tại không.
5. Nếu hợp lệ:
- tạo/cập nhật entity,
- lưu qua repository,
- build DTO response.
6. Nếu không hợp lệ: throw exception phù hợp để trả mã lỗi đúng.

## 3) Service nằm ở đâu trong request-to-database flow?

Vị trí:
`Client -> Controller -> Service -> Repository -> DB`

Chiều về:
`DB -> Repository -> Service (map + rule hậu xử lý) -> Controller -> Response JSON`

Service là điểm trung tâm giữ cho logic nghiệp vụ không bị rải ở controller.

## 4) Dấu hiệu nhận biết một service "đã hoàn chỉnh"

Một service/use case được xem là hoàn chỉnh khi:
- Có xử lý đủ happy path theo yêu cầu nghiệp vụ.
- Có xử lý unhappy path chính (không tìm thấy, không có quyền, dữ liệu không hợp lệ).
- Không để lộ entity trực tiếp ra API (trả DTO/response model rõ ràng).
- Không phụ thuộc trực tiếp vào HTTP object (`HttpServletRequest`, status code hardcode trong service) trừ khi thật sự cần.
- Có test phù hợp ở mức service và/hoặc integration cho endpoint liên quan.
- Lỗi được ném theo exception chuẩn của project để format response thống nhất.

## 5) Checklist nhanh khi thêm service mới

- Tên service method diễn tả use case, không chỉ CRUD mơ hồ.
- Đặt transaction boundary hợp lý (`@Transactional`) cho các thao tác ghi nhiều bước.
- Validate ownership trước khi update/delete dữ liệu user-specific.
- Tránh query N+1 hoặc gọi DB dư thừa trong cùng use case.
- Trả về DTO sạch, không chứa field nhạy cảm.
