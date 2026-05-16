# API Response Study

## 1) Mục tiêu

Chuẩn hóa response thành công/lỗi để frontend xử lý nhất quán.

## 2) Thành phần chính

- `ApiResponse<T>`: vỏ response chung.
- `PaginationResponse`: metadata cho API list.
- `ListQueryParams`: parse và validate query phân trang/sort/search.

## 3) Cách dùng

- API get-one/create/update/delete: `ApiResponse.success(message, data)`.
- API get-list: `ApiResponse.success(message, data, pagination)`.
- Field null được ẩn trong JSON (nhờ `@JsonInclude(NON_NULL)`).

## 4) Happy vs unhappy case

Happy:
- controller/service trả đúng factory method `success`.
- list endpoint trả kèm `pagination`.

Unhappy:
- lỗi nghiệp vụ/validation vẫn trả qua format `ApiResponse.error(...)` bởi exception handler.

## 5) Dấu hiệu hoàn chỉnh

- Toàn bộ endpoint dùng cùng response contract.
- Pagination metadata nhất quán page/size/total.
