# Frontend Phase 1 App Shell & Routing Validation

## Scope

Kiểm tra FE-1 sau khi thêm React Router, public/protected routes, AppShell, desktop sidebar, mobile bottom navigation, topbar, fallback page và session demo tạm thời cho protected route.

## Environment / Setup

- Workspace: `/mnt/f/Project/taichinh`
- Frontend path: `frontend`
- Backend không cần chạy cho batch này vì FE-1 chỉ kiểm routing/layout shell
- Vite dev server chạy local ở `http://127.0.0.1:3001/` trong batch này

## Commands Executed

```bash
npm run lint
npm run build
npm run dev -- --host 127.0.0.1
curl -fsSL http://127.0.0.1:3001/login
curl -fsSL http://127.0.0.1:3001/dashboard
curl -fsSL http://127.0.0.1:3001/wallets
curl -fsSL http://127.0.0.1:3001/transactions
```

## Results

- `npm run lint`: Pass
- `npm run build`: Pass
- Vite dev server: Pass, started on port `3001` because port `3000` was already in use
- SPA route response for `/login`: Pass
- SPA route response for `/dashboard`: Pass
- SPA route response for `/wallets`: Pass
- SPA route response for `/transactions`: Pass

## Defects Found

- Initial curl checks failed while the dev server was not running.
- Port `3000` was already in use, so Vite selected `3001`.

## Fixes Applied During Batch

- Restarted Vite dev server and rechecked routes on `3001`.
- Fixed duplicated mobile bottom nav padding in CSS before validation.

## Final Batch Status

Pass hết. FE-1 đạt mức tối thiểu để đi tiếp sang FE-2 API client và auth foundation.
