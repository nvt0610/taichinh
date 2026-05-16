# Frontend Phase 2-3 API Auth Foundation & Auth Pages Validation

## Scope

Kiểm tra sau khi triển khai FE-2 và FE-3:

- Axios API client
- Auth token interceptor
- API error normalizer
- Auth service: register, login, refresh, logout
- Zustand `authStore` có persist token/user state
- Login/Register pages bằng React Hook Form
- Logout action trong AppShell

## Environment / Setup

- Workspace: `/mnt/f/Project/taichinh`
- Frontend path: `frontend`
- Backend health check target: `http://localhost:8080/api/health`

## Commands Executed

```bash
npm run lint
npm run build
curl -fsSL http://localhost:8080/api/health
```

## Results

- `npm run lint`: Pass
- `npm run build`: Pass
- Backend health check: Fail, không kết nối được `localhost:8080`

## Defects Found

- Backend không chạy ở `localhost:8080` trong batch này, nên chưa thể test register/login end-to-end bằng backend thật.
- Build lần đầu báo thiếu type cho `import.meta.env` và helper map field errors viết quá phức tạp.

## Fixes Applied During Batch

- Thêm `frontend/src/vite-env.d.ts`.
- Đơn giản hóa `toFieldErrorMap` trong API client.
- Tách auth interceptor khỏi store import trực tiếp bằng `configureAuthHandlers` để tránh import vòng giữa Axios client và Zustand store.

## Final Batch Status

Pass phần frontend compile/lint. Runtime auth end-to-end còn pending vì backend chưa chạy, nên các mục API services/Auth pages được đánh dấu `[~]` trong checklist cho tới khi test được với backend thật.
