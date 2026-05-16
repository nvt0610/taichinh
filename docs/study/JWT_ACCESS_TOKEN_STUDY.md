# JWT Access Token Study

## 1. JWT Là Gì Trong Project Này?

JWT là access token backend cấp sau khi user đăng nhập thành công. Frontend sẽ giữ token này và gửi lại trong header cho các API cần đăng nhập:

```http
Authorization: Bearer <access_token>
```

Trong project này, JWT access token dùng để chứng minh:

- user là ai,
- token được backend ký hợp lệ,
- token còn hạn,
- token có các claim cơ bản để filter dữ liệu theo user sau này.

JWT access token không thay thế refresh token. Access token sống ngắn hơn và dùng để gọi API. Refresh token sẽ làm ở lát sau để xin access token mới khi access token hết hạn.

---

## 2. Dependency JWT Đang Dùng

Project đang dùng thư viện `jjwt` trong `backend/pom.xml`:

```xml
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.12.6</version>
</dependency>
```

Ngoài `jjwt-api`, project còn có:

- `jjwt-impl`: implementation runtime.
- `jjwt-jackson`: hỗ trợ serialize/deserialize JSON claims.

Code không tự mã hóa JSON bằng tay. `jjwt` chịu trách nhiệm build token, ký token, parse token, và validate chữ ký.

---

## 3. Config JWT Nằm Ở Đâu?

Config nằm trong `application.yml`:

```yaml
jwt:
  secret: ${JWT_SECRET:taichinh-super-secret-key-change-in-production-min-256-bits}
  expiration: 86400000
  refresh-expiration: 604800000
```

Ý nghĩa:

- `jwt.secret`: secret key dùng để ký access token.
- `jwt.expiration`: thời gian sống của access token, đơn vị millisecond.
- `jwt.refresh-expiration`: dành cho refresh token, hiện chưa dùng ở lát JWT access token này.

Ở production, `application-prod.yml` bắt buộc đọc `JWT_SECRET` từ biến môi trường. Không nên hard-code secret thật trong source code.

---

## 4. `JwtService` Đang Làm Gì?

File chính là `backend/src/main/java/com/taichinh/app/security/JwtService.java`.

Class này có 3 trách nhiệm chính:

- tạo access token,
- parse claims từ token,
- kiểm tra token còn hạn hay không.

Constructor đọc config:

```java
public JwtService(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") long accessTokenExpirationMillis)
```

Sau đó secret được đổi thành signing key:

```java
Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))
```

`jjwt` dùng key này để ký token và kiểm tra chữ ký khi parse token.

---

## 5. Access Token Được Tạo Khi Nào?

Access token được tạo trong `AuthService.login(...)` sau khi:

1. Tìm thấy user theo username hoặc email.
2. User còn active.
3. Password đúng với BCrypt hash trong database.
4. Backend cập nhật `last_login_at`.
5. Backend lấy danh sách role của user.

Sau đó code gọi:

```java
String accessToken = jwtService.generateAccessToken(savedUser, roles);
```

Nghĩa là login thành công mới được cấp token. Register hiện chưa tự cấp token, vì MVP có thể chọn flow đăng ký xong rồi đăng nhập sau.

---

## 6. Token Có Những Claim Nào?

Trong `generateAccessToken(...)`, token có các claim chính:

```java
.subject(user.getId().toString())
.claim("username", user.getUsername())
.claim("email", user.getEmail())
.claim("roles", roles)
.issuedAt(Date.from(issuedAt))
.expiration(Date.from(expiresAt))
```

Ý nghĩa:

- `sub`: user id, đây là định danh chính của token.
- `username`: username hiện tại của user.
- `email`: email hiện tại của user.
- `roles`: danh sách role, ví dụ `["USER"]`.
- `iat`: thời điểm token được cấp.
- `exp`: thời điểm token hết hạn.

Sau này khi làm JWT filter, backend sẽ đọc `sub` để biết request thuộc user nào.

---

## 7. Login Response Trả Token Ra Sao?

`LoginResponse` hiện có:

```java
public record LoginResponse(
        UUID id,
        String username,
        String email,
        List<String> roles,
        String accessToken,
        String tokenType,
        long expiresIn,
        LocalDateTime lastLoginAt)
```

Ví dụ response:

```json
{
  "success": true,
  "message": "Login completed successfully.",
  "data": {
    "id": "user-id",
    "username": "mai",
    "email": "mai@example.com",
    "roles": ["USER"],
    "accessToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "lastLoginAt": "2026-05-15T08:00:00"
  },
  "timestamp": "2026-05-15T08:00:00Z"
}
```

`expiresIn` đang trả bằng giây, vì frontend thường dễ dùng giây hơn millisecond khi tính thời gian còn lại.

---

## 8. Parse Và Validate Token Là Gì?

`parseClaims(...)` dùng để đọc token:

```java
return Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
```

Khi parse, `jjwt` sẽ kiểm tra chữ ký. Nếu token bị sửa, sai secret, hoặc format không hợp lệ thì thư viện sẽ ném exception.

`isTokenValid(...)` hiện làm bước cơ bản:

```java
Claims claims = parseClaims(token);
return claims.getExpiration().after(new Date());
```

Nghĩa là token hợp lệ khi:

- parse được,
- chữ ký đúng,
- `exp` còn sau thời điểm hiện tại.

JWT filter dùng method parse token này trong request pipeline để kiểm tra token và set authentication cho Spring Security.

---

## 9. JWT Filter Và Protected Routes

File `JwtAuthenticationFilter` đọc header:

```http
Authorization: Bearer <access_token>
```

Nếu không có bearer token, filter cho request đi tiếp. Sau đó `SecurityConfig` sẽ quyết định endpoint đó public hay cần login.

Nếu có bearer token, filter sẽ:

1. Parse token bằng `JwtService`.
2. Kiểm tra chữ ký và hạn token thông qua `jjwt`.
3. Lấy `sub` làm principal.
4. Lấy claim `roles` và đổi sang Spring authorities dạng `ROLE_USER`.
5. Set authentication vào `SecurityContextHolder`.

Nếu token sai hoặc hết hạn, filter trả response `401 Unauthorized` theo `ApiResponse` chuẩn.

`SecurityConfig` hiện public các route:

- `GET /api/health`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

Các route domain sau cần token:

- `/api/wallets/**`
- `/api/categories/**`
- `/api/transactions/**`
- `/api/dashboard/**`

Các route khác cũng mặc định cần authentication vì config dùng:

```java
.anyRequest().authenticated()
```

---

## 10. Access Token Khác Refresh Token Như Thế Nào?

Access token:

- dùng để gọi API,
- sống ngắn hơn,
- không lưu DB trong thiết kế hiện tại,
- backend chỉ cần verify chữ ký và hạn token.

Refresh token:

- dùng để xin access token mới,
- sống lâu hơn,
- sẽ lưu DB trong bảng `refresh_tokens`,
- có trạng thái `revoked` và `expired_at`.

Vì vậy checklist vẫn chưa tick refresh token. Lát này chỉ hoàn thành JWT access token.

---

## 11. Khi Nào Xem JWT Access Token Là Đủ?

Lát JWT access token được xem là đủ khi:

- Có `JwtService`.
- Login thành công trả `accessToken`.
- Token có claims cơ bản: user id, username, email, roles.
- Token dùng expiration từ config.
- Build backend thành công.

Những phần chưa nằm trong lát này:

- Refresh token.
- Logout/revoke refresh token.
