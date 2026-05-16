# Wallet Và Category CRUD Study

File này giải thích layer CRUD đầu tiên của Phase 3 trong backend: `wallet` và `category`.

## 1) Mục tiêu của slice này

Cho user đã đăng nhập:

- tạo wallet
- xem list wallet có pagination + search
- xem chi tiết wallet
- cập nhật thông tin wallet
- soft delete wallet
- tạo category
- xem list category theo user, có filter `type`, pagination, search
- xem chi tiết category
- cập nhật category
- soft delete category

Điểm quan trọng: mọi dữ liệu đều bị khóa theo `userId`, không được lấy chéo dữ liệu user khác.

---

## 2) Các file chính

### Wallet

- `controller/WalletController.java`
- `service/WalletService.java`
- `repository/WalletRepository.java`
- `dto/wallet/CreateWalletRequest.java`
- `dto/wallet/UpdateWalletRequest.java`
- `dto/wallet/WalletResponse.java`

### Category

- `controller/CategoryController.java`
- `service/CategoryService.java`
- `repository/CategoryRepository.java`
- `dto/category/CreateCategoryRequest.java`
- `dto/category/UpdateCategoryRequest.java`
- `dto/category/CategoryResponse.java`

### Hỗ trợ dùng chung

- `security/AuthenticatedUserProvider.java`
- `dto/common/ListQueryParams.java`
- `dto/common/ApiResponse.java`
- `dto/common/PaginationResponse.java`
- `exception/GlobalExceptionHandler.java`

---

## 3) Luồng request -> database

Ví dụ `GET /api/wallets?page=1&size=10&q=main&sort=createdAt,desc`

1. `JwtAuthenticationFilter` đọc access token.
2. Spring Security xác thực request.
3. `WalletController` nhận request.
4. `AuthenticatedUserProvider` đổi `Authentication.getName()` thành `UUID userId`.
5. `ListQueryParams` parse `page`, `size`, `q`, `sort` thành `Pageable`.
6. `WalletService` chọn query phù hợp:
   - không có `q` -> query list thường
   - có `q` -> query search theo `name`
7. `WalletRepository` chạy JPA query.
8. Service map `Wallet` -> `WalletResponse`.
9. Controller trả `ApiResponse.success(..., data, pagination)`.

Category cũng đi flow tương tự, chỉ thêm filter `type`.

---

## 4) Vì sao cần `AuthenticatedUserProvider`

JWT access token của project đang để:

- `subject = userId`
- `username`
- `email`
- `roles`

Nên ở protected route, controller không nên tin `userId` từ request.
Thay vào đó:

- lấy `Authentication`
- đọc `subject` từ security context
- convert ra `UUID`

Như vậy client không thể sửa request để đụng dữ liệu user khác.

---

## 5) Vì sao wallet update không cho sửa balance

Trong Phase 3 này:

- `balance` được set lúc tạo wallet
- update wallet chỉ sửa `name`, `type`, `description`

Lý do:

- `balance` về sau phải được điều khiển chính bởi transaction flow
- nếu cho sửa balance tự do ngay ở update API thì rất dễ lệch số dư khi Phase income/expense/transfer được thêm vào

Nói ngắn: wallet update hiện tại là “update info”, chưa phải “adjust balance”.

---

## 6) Pagination, sort, search đang chạy như nào

`ListQueryParams` hỗ trợ:

- `page`
- `size`
- `q`
- `sort`

### Wallet

- default sort: `createdAt desc`
- sort whitelist: `name`, `type`, `balance`, `createdAt`, `updatedAt`

### Category

- default sort: `name asc`
- sort whitelist: `name`, `type`, `createdAt`, `updatedAt`

Controller trả:

- `data`: list item
- `pagination`: metadata cho frontend render trang

---

## 7) Soft delete đang làm gì

Delete API không xóa row thật khỏi DB.

Nó chỉ set:

```txt
deleted_at = now()
```

Sau đó các query list/detail chỉ lấy:

```txt
deleted_at IS NULL
```

Điều này giữ được lịch sử dữ liệu và khớp với thiết kế DB của project.

---

## 8) Error handling liên quan CRUD này

Các case chính:

- body sai validation -> `VALIDATION_FAILED`
- sort không hợp lệ -> `INVALID_SORT`
- enum/body parse lỗi -> `BAD_REQUEST`
- resource không thuộc user hoặc đã soft delete -> `NOT_FOUND`
- chưa đăng nhập / token sai -> `UNAUTHORIZED`

Tất cả đi qua `GlobalExceptionHandler` hoặc Spring Security entry point và trả theo format `ApiResponse`.

---

## 9) Khi nào biết slice này hoàn thành

Slice wallet/category CRUD được xem là hoàn thành khi:

- endpoint create/list/detail/update/delete đều chạy được
- list có pagination
- list có search
- category list filter được theo `type`
- dữ liệu bị khóa theo `userId`
- delete là soft delete
- auth flow cũ vẫn pass
- integration test CRUD với PostgreSQL thật pass

---

## 10) Giới hạn hiện tại

Chưa làm trong slice này:

- default category seed logic
- income/expense/transfer transaction
- tự cộng/trừ balance theo transaction
- rule không cho chi vượt số dư
- restore dữ liệu soft delete

Các phần đó sẽ nối tiếp trong các lát Phase 3 sau.
