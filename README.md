# TaiChinh MVP - Personal Finance Tracker

Personal finance tracker for managing wallets, categories, and transactions (income, expense, transfer) with a dashboard summary.

## MVP Status

MVP backend + frontend is completed and integrated.
Current focus is UI refactor and UX polish.

## Tech Stack

- Backend: Java 21, Spring Boot 3, Spring Security, Spring Data JPA
- Database: PostgreSQL 16, Flyway
- Auth: JWT access token + refresh token
- Frontend: React 18, TypeScript, Vite, Zustand
- DevOps: Docker, Docker Compose

## Repository Structure

```text
taichinh/
|- backend/                        # Spring Boot API
|  |- src/main/java/com/taichinh/app/
|  |  |- config/
|  |  |- controller/
|  |  |- service/
|  |  |- repository/
|  |  |- entity/
|  |  |- dto/
|  |  |- security/
|  |  |- exception/
|  |  `- enums/
|  |- src/main/resources/
|  |  |- application.yml
|  |  |- application-prod.yml
|  |  `- db/migration/
|  `- pom.xml
|- frontend/                       # React app
|  |- src/
|  |  |- components/
|  |  |- pages/
|  |  |- services/
|  |  |- store/
|  |  |- types/
|  |  `- styles/
|  `- package.json
|- docs/
|  |- IMPLEMENTATION_CHECKLIST.md
|  |- study/
|  `- test-results/
|- docker-compose.dev.yml
|- docker-compose.yml
`- README.md
```

## Implemented Features

- Authentication
- Register
- Login
- Refresh access token
- Logout (refresh token revoke)

- Wallet management
- Create, list, detail, update, soft delete
- Pagination + search

- Category management
- Create, list, detail, update, soft delete
- Filter by type + pagination + search

- Transaction management
- Create income / expense / transfer
- List, detail, update by transaction type, soft delete
- Wallet balance update rules and ownership checks

- Dashboard
- Summary endpoint
- Recent transactions
- Top spending categories
- Monthly statistics

- Frontend pages
- Register/Login
- Dashboard
- Wallets
- Transactions
- Protected routing + auth store

## API Overview

Base URL (local): `http://localhost:8080`

Public endpoints:
- `GET /api/health`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

Protected endpoints:
- Wallets: `/api/wallets`
- Categories: `/api/categories`
- Transactions: `/api/transactions`
- Dashboard: `/api/dashboard/*`

See controllers for exact contracts:
- [AuthController.java](/f:/Project/taichinh/backend/src/main/java/com/taichinh/app/controller/AuthController.java)
- [WalletController.java](/f:/Project/taichinh/backend/src/main/java/com/taichinh/app/controller/WalletController.java)
- [CategoryController.java](/f:/Project/taichinh/backend/src/main/java/com/taichinh/app/controller/CategoryController.java)
- [TransactionController.java](/f:/Project/taichinh/backend/src/main/java/com/taichinh/app/controller/TransactionController.java)
- [DashboardController.java](/f:/Project/taichinh/backend/src/main/java/com/taichinh/app/controller/DashboardController.java)

## Run Locally (Development)

1. Start PostgreSQL (dev compose)

```bash
docker compose -f docker-compose.dev.yml up -d
```

2. Start backend

```bash
cd backend
cp .env.example .env
./mvnw spring-boot:run
```

3. Start frontend

```bash
cd frontend
npm install
npm run dev
```

Local URLs:
- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`

## Run Full Stack (Docker)

```bash
cp .env.example .env
docker compose up --build
```

## Testing and Validation

Backend tests and integration test reports are tracked in:
- [docs/test-results](/f:/Project/taichinh/docs/test-results)

MVP implementation checklist:
- [docs/IMPLEMENTATION_CHECKLIST.md](/f:/Project/taichinh/docs/IMPLEMENTATION_CHECKLIST.md)

Study notes by backend layer and workflow:
- [docs/study/README.md](/f:/Project/taichinh/docs/study/README.md)

## Security and Config Notes

- Do not commit real `.env` files.
- Use `.env.example` templates.
- JWT and DB settings are loaded from environment-backed config.
- API returns standardized response and error format.
