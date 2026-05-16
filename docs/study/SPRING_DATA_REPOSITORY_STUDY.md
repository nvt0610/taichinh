# Spring Data Repository Study

## Repository Là Gì?

Repository là tầng code chuyên đọc và ghi dữ liệu. Trong project này, repository là các interface trong package `com.taichinh.app.repository` và kế thừa `JpaRepository<Entity, UUID>`.

Ví dụ:

```java
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    List<Wallet> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId);
}
```

Spring Data JPA tự tạo implementation lúc app chạy, nên mình không cần viết class `WalletRepositoryImpl` cho các query cơ bản.

## Repository Hoạt Động Trong Project Này

Mỗi repository quản lý một entity chính:

- `UserRepository` quản lý bảng `users`.
- `RoleRepository` quản lý bảng `roles`.
- `WalletRepository` quản lý bảng `wallets`.
- `CategoryRepository` quản lý bảng `categories`.
- `TransactionRepository` quản lý bảng `transactions`.
- `RefreshTokenRepository` quản lý bảng `refresh_tokens`.

Các method như `findByEmailAndDeletedAtIsNull` là derived query method. Spring đọc tên method, map sang field của entity, rồi sinh SQL tương ứng.

## Vị Trí Trong Luồng Request Đến Database

Luồng backend thường đi như sau:

```text
Controller
  -> Service
  -> Repository
  -> Entity
  -> Database table
```

Controller nhận HTTP request. Service xử lý nghiệp vụ. Repository thực hiện truy vấn. Entity mô tả dữ liệu Java được map với table trong PostgreSQL.

## Khi Nào Repository Được Xem Là Đủ?

Một repository cơ bản được xem là đủ khi:

- Kế thừa đúng `JpaRepository<Entity, UUID>`.
- Có query method cho các luồng service gần nhất cần dùng.
- Query soft-delete có điều kiện `DeletedAtIsNull` nếu entity có `deletedAt`.
- Method ownership có cả `id` và `userId` khi dữ liệu thuộc về một user.
- App build được, chứng tỏ Spring Data parse được tên method và field entity tồn tại.

Không nên nhồi toàn bộ query tương lai vào repository ngay từ đầu. Thêm query khi service hoặc API thật sự cần.
