# Exception Handling Study

## 1) Mục tiêu

Chuẩn hóa error code, status code, và body lỗi cho mọi endpoint.

## 2) Thành phần chính

- `ErrorCode`: enum map domain error -> HTTP status.
- `BusinessException`: exception nghiệp vụ do service throw.
- `ErrorResponse` + `FieldErrorResponse`: payload lỗi chi tiết.
- `GlobalExceptionHandler`: `@RestControllerAdvice` gom và map exception.

## 3) Luồng xử lý lỗi

1. Controller/service ném exception.
2. `GlobalExceptionHandler` bắt exception phù hợp.
3. Build `ApiResponse.error(...)` với `ErrorCode` + details.
4. Trả HTTP status tương ứng.

## 4) Happy vs unhappy case

Happy:
- không exception, đi luồng success bình thường.

Unhappy:
- validation lỗi -> `VALIDATION_FAILED` (400)
- unauthorized -> `UNAUTHORIZED` (401)
- forbidden -> `FORBIDDEN` (403)
- not found -> `NOT_FOUND` (404)
- conflict -> `CONFLICT` (409)
- lỗi không dự kiến -> `INTERNAL_ERROR` (500)

## 5) Dấu hiệu hoàn chỉnh

- Error code ổn định để frontend xử lý theo machine-readable code.
- Message đủ rõ cho debug.
- Không lộ stack trace nhạy cảm ra client.
