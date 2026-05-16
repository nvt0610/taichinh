# Refresh Token Flow Study

## 1. Refresh Token Dùng Để Làm Gì?

Access token sống ngắn và dùng để gọi API. Khi access token hết hạn, frontend không nên bắt user đăng nhập lại ngay. Thay vào đó, frontend gửi refresh token lên backend để xin access token mới.

Trong project này:

- Login trả cả `accessToken` và `refreshToken`.
- Access token là JWT.
- Refresh token là opaque token ngẫu nhiên, không phải JWT.
- Refresh token được lưu trong bảng `refresh_tokens`.
- Refresh token có thể bị revoke khi logout.

## 2. Vì Sao Refresh Token Không Phải JWT?

Refresh token hiện là chuỗi random được tạo bằng `SecureRandom`:

```java
byte[] randomBytes = new byte[32];
secureRandom.nextBytes(randomBytes);
return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
```

Backend không cần nhét claim vào refresh token. Backend chỉ cần tra token trong database để biết:

- token này có tồn tại không,
- token thuộc user nào,
- token đã hết hạn chưa,
- token đã bị revoke chưa.

Cách này dễ revoke hơn JWT refresh token vì trạng thái nằm trong database.

## 3. Login Tạo Refresh Token Như Thế Nào?

Sau khi login verify password thành công, `AuthService.login(...)` gọi:

```java
RefreshToken refreshToken = createRefreshToken(savedUser);
```

`createRefreshToken(...)` sẽ:

1. Tạo chuỗi token ngẫu nhiên.
2. Tính `expiredAt` dựa trên `jwt.refresh-expiration`.
3. Lưu bản ghi `RefreshToken` vào database.
4. Trả token về trong `LoginResponse`.

Response login có thêm:

```json
{
  "accessToken": "jwt...",
  "refreshToken": "opaque-random-token",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "refreshExpiresIn": 604800
}
```

## 4. Refresh Access Token Hoạt Động Thế Nào?

Endpoint:

```http
POST /api/auth/refresh
```

Body:

```json
{
  "refreshToken": "opaque-random-token"
}
```

Service sẽ:

1. Tìm token trong bảng `refresh_tokens`.
2. Nếu không tồn tại, trả `INVALID_REFRESH_TOKEN`.
3. Nếu `revoked = true`, trả `INVALID_REFRESH_TOKEN`.
4. Nếu `expired_at` đã qua, revoke token rồi trả `INVALID_REFRESH_TOKEN`.
5. Nếu token hợp lệ, tìm user còn active.
6. Lấy roles của user.
7. Cấp access token JWT mới.

Refresh endpoint hiện chỉ cấp access token mới, chưa rotate refresh token.

## 5. Logout/Revoke Hoạt Động Thế Nào?

Endpoint:

```http
POST /api/auth/logout
```

Body:

```json
{
  "refreshToken": "opaque-random-token"
}
```

Service tìm refresh token theo chuỗi token. Nếu thấy thì set:

```java
refreshToken.setRevoked(true);
```

Sau khi logout, token đó không thể dùng ở `/api/auth/refresh` nữa.

Logout đang được thiết kế idempotent: nếu token không tồn tại thì vẫn trả success. Điều này giúp frontend gọi logout an toàn, kể cả khi token đã bị xóa hoặc không còn hợp lệ.

## 6. Những Gì Chưa Làm Ở Lát Này?

Lát này chưa làm:

- JWT filter đọc header `Authorization`.
- Protected routes.
- Refresh token rotation.
- Revoke toàn bộ refresh token của một user.
- Lưu hash của refresh token thay vì raw token.

Các phần đó có thể làm sau khi bước `Security filter + protected routes` bắt đầu.
