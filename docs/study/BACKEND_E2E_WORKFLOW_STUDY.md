# Backend End-to-End Workflow Study

## 1) Mục tiêu tài liệu

Tài liệu này mô tả một luồng backend điển hình trong project từ lúc request vào `controller`, đi qua các layer, xuống database, rồi quay ngược lên để trả response.

Bao gồm cả:
- Happy case (thành công),
- Unhappy case (lỗi/ngoại lệ).

## 2) Happy case: request xử lý thành công

Ví dụ tổng quát cho endpoint cần xác thực JWT và thao tác dữ liệu cá nhân.

### Bước 1: HTTP request đi vào Spring Security filter chain

- Client gửi request kèm access token (Bearer JWT).
- Security filter kiểm tra token:
- token hợp lệ -> tạo `Authentication` và set vào `SecurityContext`.
- token không bắt buộc với endpoint public -> có thể đi tiếp theo rule config.

### Bước 2: Request tới `Controller`

- `Controller` match theo route (`@RequestMapping`, `@GetMapping`, `@PostMapping`...).
- Spring parse payload vào DTO và chạy validation annotation (`@Valid`) nếu có.
- `Controller` gọi method trong `Service` tương ứng.

### Bước 3: `Service` xử lý nghiệp vụ

- Lấy user hiện tại từ security context (nếu use case cần).
- Kiểm tra business rule:
- dữ liệu có thuộc user không,
- input có hợp lệ theo domain không,
- trạng thái hiện tại có cho phép thao tác không.
- Gọi một hoặc nhiều `Repository` để đọc/ghi dữ liệu.
- Nếu là ghi, có thể chạy trong transaction để đảm bảo toàn vẹn.

### Bước 4: `Repository` làm việc với DB

- Spring Data JPA/Hibernate tạo và chạy SQL.
- DB trả dữ liệu về entity hoặc xác nhận write thành công.

### Bước 5: Quay lại `Service` để map response

- `Service` nhận entity/result.
- Map sang DTO response/API response model.
- Trả kết quả lên `Controller`.

### Bước 6: `Controller` trả HTTP response

- `Controller` bọc payload theo chuẩn response của project (nếu có wrapper).
- Spring serialize object thành JSON.
- HTTP status thành công (`200`, `201`, ... ) được trả về client.

## 3) Unhappy case: các nhánh lỗi thường gặp

### A. Lỗi trước khi vào service

1. `401 Unauthorized`
- JWT thiếu/hết hạn/sai chữ ký.
- Security filter chặn request trước khi vào controller.

2. `403 Forbidden`
- Đã authenticated nhưng không có quyền theo rule bảo mật endpoint.

3. `400 Bad Request` (validation)
- Payload sai format hoặc vi phạm annotation validate.
- Spring ném exception bind/validation ngay ở tầng web.

### B. Lỗi trong service/repository

1. `404 Not Found`
- Service query không thấy resource (wallet/category/transaction...).
- Service ném `NotFound` exception domain.

2. `403 Forbidden` (ownership/business permission)
- Resource tồn tại nhưng không thuộc user hiện tại.
- Service chặn và ném exception quyền truy cập.

3. `400 Bad Request` (business rule)
- Ví dụ: số tiền âm khi rule không cho phép, hoặc input hợp lệ về mặt syntax nhưng sai nghiệp vụ.

4. `409 Conflict` (nếu có áp dụng)
- Trùng dữ liệu unique hoặc trạng thái xung đột.

5. `500 Internal Server Error`
- Lỗi không dự kiến (null, lỗi hạ tầng, query lỗi ngoài expected cases).

### C. Global exception handler chuẩn hóa response

- Exception từ controller/service được bắt bởi `@ControllerAdvice` (nếu project đã cấu hình).
- Handler map exception -> HTTP status + error body thống nhất (message, code, timestamp, path...).
- Client nhận lỗi dạng nhất quán thay vì stack trace thô.

## 4) Flow tóm tắt nhanh (cả hai chiều)

Happy path:
`Request -> Security -> Controller -> Service -> Repository -> DB -> Service -> Controller -> JSON Response`

Unhappy path (ví dụ NotFound):
`Request -> Security -> Controller -> Service (throw NotFound) -> Global Exception Handler -> Error JSON`

## 5) Dấu hiệu một luồng endpoint đã hoàn chỉnh

- Có route rõ ràng, request/response DTO rõ ràng.
- Có kiểm tra auth + ownership đúng chỗ.
- Happy case trả status/payload đúng contract.
- Unhappy case chính trả đúng status code và format lỗi chuẩn.
- Có test đủ để xác nhận cả happy/unhappy path quan trọng.
