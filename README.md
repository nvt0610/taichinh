# 💰 Tài Chính - Personal Finance Tracker

Ứng dụng quản lý tài chính cá nhân giúp theo dõi thu nhập, chi tiêu, ví tiền và thống kê tài chính.

## 🛠 Tech Stack

| Layer    | Technology                                          |
|----------|-----------------------------------------------------|
| Backend  | Java 21, Spring Boot 3, Spring Security, Spring Data JPA |
| Database | PostgreSQL 16, Flyway migrations                    |
| Auth     | JWT (JSON Web Token)                                |
| Frontend | React 18, TypeScript, Vite                          |
| DevOps   | Docker, Docker Compose                              |

---

## 📁 Cấu trúc thư mục

```
taichinh/
├── backend/                          # Spring Boot API
│   ├── src/main/java/com/taichinh/app/
│   │   ├── config/                   # SecurityConfig, CorsConfig, JwtConfig
│   │   ├── controller/               # REST Controllers
│   │   ├── service/                  # Business Logic
│   │   ├── repository/               # JPA Repositories
│   │   ├── entity/                   # JPA Entities
│   │   ├── dto/                      # Request/Response DTOs
│   │   ├── security/                 # JWT Filter, UserDetailsService
│   │   ├── exception/                # Global Exception Handler
│   │   └── enums/                    # TransactionType, WalletType...
│   ├── src/main/resources/
│   │   ├── application.yml           # Dev config
│   │   ├── application-prod.yml      # Prod config
│   │   └── db/migration/             # Flyway migrations
│   ├── Dockerfile                    # Multi-stage Docker build
│   └── pom.xml                       # Maven dependencies
│
├── frontend/                         # React + TypeScript
│   ├── src/
│   │   ├── pages/                    # LoginPage, DashboardPage...
│   │   ├── components/               # Reusable UI components
│   │   ├── hooks/                    # Custom hooks
│   │   ├── services/                 # API calls (axios)
│   │   ├── store/                    # State management (Zustand)
│   │   ├── types/                    # TypeScript interfaces
│   │   ├── utils/                    # Helper functions
│   │   └── constants/                # API_URL, routes...
│   ├── Dockerfile                    # Multi-stage: Node build + Nginx
│   ├── nginx.conf                    # Nginx config + API proxy
│   ├── vite.config.ts
│   └── package.json
│
├── docs/                             # Project documentation
├── docker-compose.dev.yml            # DEV: chỉ chạy DB
├── docker-compose.yml                # PROD: DB + BE + FE
├── .env.example                      # Template biến môi trường
└── .gitignore
```

---

## 🚀 Hướng dẫn chạy

### Giai đoạn Development

**Bước 1: Khởi động Database (Docker)**
```bash
docker compose -f docker-compose.dev.yml up -d
```
> PostgreSQL sẽ chạy tại `localhost:5433`

**Bước 2: Chạy Backend**
```bash
cd backend
./mvnw spring-boot:run
# hoặc mở trong IntelliJ IDEA và Run
```
> Spring Boot API sẽ chạy tại `http://localhost:8080`

**Bước 3: Chạy Frontend**
```bash
cd frontend
npm install
npm run dev
```
> React App sẽ chạy tại `http://localhost:5173`

---

### Giai đoạn Production (sau khi code xong)

**Setup biến môi trường:**
```bash
cp .env.example .env
# Chỉnh sửa .env với giá trị thật
```

**Build và chạy tất cả:**
```bash
docker compose up --build
```
> App sẽ chạy tại `http://localhost`

---

## 🗃 Database

- **Host (dev):** `localhost:5433`
- **Database:** `taichinh_db`
- **User:** `taichinh_user`
- **Password:** `taichinh_pass`

---

## 📋 API Endpoints (dự kiến)

| Method | Endpoint                    | Mô tả                    |
|--------|-----------------------------|--------------------------|
| POST   | /api/auth/register          | Đăng ký tài khoản        |
| POST   | /api/auth/login             | Đăng nhập, nhận JWT      |
| GET    | /api/wallets                | Danh sách ví             |
| POST   | /api/wallets                | Tạo ví mới               |
| GET    | /api/transactions           | Lịch sử giao dịch        |
| POST   | /api/transactions           | Thêm giao dịch           |
| GET    | /api/dashboard/summary      | Tổng quan tài chính      |
| GET    | /api/categories             | Danh mục chi tiêu        |
