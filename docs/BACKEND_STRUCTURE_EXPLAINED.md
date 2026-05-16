# Backend Structure Explained (Spring Boot + Maven)

File này giải thích backend hiện tại được setup như nào, mỗi phần làm gì, và chúng nối nhau ra sao.

## 1) Big Picture

Backend của project là một app **Spring Boot** chạy bằng **Maven**.

Luồng tổng quát:

1. Bạn chạy lệnh `./mvnw spring-boot:run` trong WSL/Linux hoặc `.\mvnw.cmd spring-boot:run` trên Windows
2. Maven đọc `pom.xml`, tải dependency cần thiết, compile code
3. Spring Boot tìm class entry point `TaichinhApplication`
4. Flyway kiểm tra/chạy migration database
5. Spring khởi tạo config (datasource, JPA, security, server port...)
6. App mở API tại `http://localhost:8080`
7. FE gọi API (proxy qua `/api`) -> BE xử lý -> truy vấn DB PostgreSQL

---

## 2) Cấu trúc thư mục backend hiện tại

```text
backend/
├── .mvn/wrapper/
│   └── maven-wrapper.properties
├── src/main/java/com/taichinh/app/
│   ├── TaichinhApplication.java
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   └── HealthController.java
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── exception/
│   ├── security/
│   └── enums/
├── src/main/resources/
│   ├── application.yml
│   ├── application-prod.yml
│   └── db/
├── .env
├── .env.example
├── mvnw
├── mvnw.cmd
├── pom.xml
└── Dockerfile
```

Trong `src/main/resources/db/` hiện có `init_schema.sql` và thư mục `migration/` cho Flyway.

---

## 3) Maven setup đang làm gì

## `pom.xml` (trung tâm của Maven)

`pom.xml` định nghĩa:

- Project metadata: `groupId`, `artifactId`, version
- Java version: `21`
- Parent: `spring-boot-starter-parent:3.3.0`
- Dependencies: web, security, jpa, validation, postgresql, jwt, flyway...
- Plugin: `spring-boot-maven-plugin` để chạy/đóng gói Spring Boot app

Nói ngắn: `pom.xml` là bản đồ để Maven biết phải build app như nào.

## `mvnw` / `mvnw.cmd`

- Đây là **Maven Wrapper**, cho phép chạy Maven ngay cả khi máy chưa cài Maven global.
- Wrapper tự tải đúng version Maven theo `.mvn/wrapper/maven-wrapper.properties`.

Lệnh chính trên Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

---

## 4) Spring Boot setup đang làm gì

## `TaichinhApplication.java`

- Là entry point của app.
- Có `@SpringBootApplication` + `main()` để bật Spring context.
- Không có file này thì `spring-boot:run` sẽ báo không tìm được main class.

## `application.yml` (dev/local)

Đang cấu hình:

- `spring.config.import: optional:file:.env[.properties]` để Spring Boot đọc trực tiếp file `backend/.env`
- DB URL: dùng env `DB_HOST/DB_PORT/DB_NAME`, mặc định `localhost:5433`
- Username/password DB từ env
- JPA `ddl-auto: validate`
- Flyway bật mặc định, đọc migration từ `classpath:db/migration`
- `baseline-on-migrate: true` để quản lý được DB dev đã có schema sẵn
- Server port từ env, mặc định `8080`
- JWT expiration và refresh expiration đọc từ env, có default an toàn cho local
- CORS origin mặc định `http://localhost:5173`

## `application-prod.yml` (production profile)

- Cũng import `backend/.env` bằng `spring.config.import`
- DB host mặc định `db` (phù hợp docker network)
- JPA `ddl-auto: validate` (an toàn hơn prod)
- Flyway bật mặc định để migrate schema khi app start
- `JWT_SECRET` bắt buộc lấy từ env
- Logging và CORS theo hướng production

---

## 5) Các package Java trong `src/main/java` dùng để làm gì

- `controller/`: nhận HTTP request từ FE
- `service/`: business logic
- `repository/`: truy cập DB (Spring Data JPA)
- `entity/`: mapping bảng DB <-> object Java
- `dto/`: object request/response cho API
- `config/`: cấu hình hệ thống (security, cors, bean...)
- `security/`: JWT filter, auth logic (sẽ mở rộng)
- `exception/`: xử lý lỗi tập trung
- `enums/`: các enum domain

Hiện project đang ở phase đầu, nhiều folder có `.gitkeep` để giữ structure.

---

## 6) Code đang có thật sự chạy thế nào (FE -> BE -> DB)

### `HealthController`

`GET /api/health`:

- BE dùng `JdbcTemplate` chạy `SELECT 1`
- Nếu query OK -> DB reachable
- Trả JSON kiểu:
  - `backend: ok`
  - `database: ok`
  - `check: fe->be->db`

### `SecurityConfig`

Hiện tại backend đã chạy theo hướng stateless + JWT:

- Public routes: `/api/health`, `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout`
- Protected routes: `/api/wallets/**`, `/api/categories/**`, `/api/transactions/**`, `/api/dashboard/**`
- `JwtAuthenticationFilter` đọc token `Bearer ...` và set authentication vào Spring Security context

---

## 7) Vai trò của các file env

- `backend/.env`: biến môi trường local cho BE (DB host/port, JWT secret, CORS...)
- `backend/.env.example`: template chia sẻ cho team
- `.env` ở root: env cho `docker-compose.yml` full stack
- `.env.example` ở root: template production/full stack

Điểm quan trọng:

- Spring Boot không tự đọc file `.env` kiểu Node.js.
- Trong project này, Spring đọc `backend/.env` vì `application.yml` và `application-prod.yml` có `spring.config.import`.
- Sau khi import file `.env`, các giá trị mới được map vào `${ENV_NAME:defaultValue}`.
- Nếu biến không có trong `backend/.env`, Spring mới dùng giá trị default trong `application.yml`.

---

## 8) Docker liên quan backend thế nào

- DB chạy container PostgreSQL (map ra local `5433`)
- BE local kết nối DB qua `localhost:5433`
- Khi deploy bằng docker-compose full stack, BE có thể dùng host nội bộ `db:5432`
- Local dev đang khớp sẵn với:
  - `docker-compose.dev.yml`: Postgres chạy ở `localhost:5433`
  - `backend/.env`: `DB_HOST=localhost`, `DB_PORT=5433`
  - `backend/.env`: `CORS_ORIGINS=http://localhost:5173` cho Vite FE

---

## 9) Database migration hiện tại

Project dùng Flyway để quản lý schema:

- `V1__baseline_schema.sql`: schema MVP ban đầu
- `V2__updated_at_triggers.sql`: function + trigger tự cập nhật `updated_at`

DB dev hiện tại đã được baseline ở version `1` và migrate lên version `2`.

---

## 10) Run sequence khuyến nghị (local)

1. Start DB:

```bash
docker compose -f docker-compose.dev.yml up -d
```

2. Start BE:

```bash
cd backend
./mvnw spring-boot:run
```

3. Start FE:

```bash
cd frontend
npm run dev
```

4. Mở FE, xem console check `FE -> BE -> DB`.
