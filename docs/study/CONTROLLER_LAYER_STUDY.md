# Controller Layer Study

## 1) Controller là gì?

`controller` là tầng biên HTTP của backend: nhận request, map route, validate input, gọi service, và trả response chuẩn.

## 2) Trách nhiệm chính trong project

- Khai báo endpoint bằng `@RestController`, `@RequestMapping`, `@GetMapping/@PostMapping/...`.
- Nhận `@PathVariable`, `@RequestParam`, `@RequestBody`.
- Kích hoạt validation bằng `@Valid`.
- Không chứa business logic phức tạp; chỉ orchestration giữa web layer và service.
- Trả `ApiResponse` thống nhất.

## 3) Vị trí trong flow

`Client -> Security filter -> Controller -> Service -> Repository -> DB`

Chiều về:
`DB -> Repository -> Service -> Controller -> JSON`

## 4) Happy vs unhappy case

Happy:
- request hợp lệ
- controller gọi service
- service trả DTO
- controller bọc `ApiResponse.success(...)`

Unhappy:
- body/query sai -> validation exception
- token lỗi/quyền lỗi -> 401/403 từ security
- service throw `BusinessException` -> `GlobalExceptionHandler` map mã lỗi

## 5) Dấu hiệu controller hoàn chỉnh

- Endpoint rõ contract request/response.
- Validate input đủ.
- Không nhúng SQL/business rule.
- Trả status/message đúng use case.
- Error đi theo format chung.
