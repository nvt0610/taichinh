# MVP BE to FE Flow Check

- Timestamp: 2026-05-15T18:34:21Z
- Scope: Kiểm tra lại luồng MVP từ backend đến frontend: build, backend integration tests, FE service contract, FE lint/build, và runtime smoke khả dụng trong môi trường hiện tại.

## Environment

- Workspace: `/mnt/f/Project/taichinh`
- Database: `taichinh_db` Docker container healthy, exposed at `localhost:5433`
- Backend: Spring Boot 3.3.0, Java 21
- Frontend: React 18 + TypeScript + Vite

## Commands executed

1. `docker ps --format '{{.Names}} {{.Status}} {{.Ports}}'`
2. `./mvnw test` in `backend`
3. `./mvnw -DskipTests package` in `backend`
4. `npm run lint` in `frontend`
5. `npm run build` in `frontend`
6. `curl -i --max-time 5 http://localhost:8080/api/health`
7. `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=18080`
8. `curl -i --max-time 5 http://127.0.0.1:18080/api/health`

## Results

1. Database container: **PASS**
   - `taichinh_db` is healthy and mapped to `localhost:5433`.
2. Backend tests: **PASS**
   - `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0`.
   - Coverage includes auth flow, data ownership, dashboard summary, transaction flow, wallet/category CRUD, DB trigger, JWT service/filter.
3. Backend package: **PASS**
   - Spring Boot jar built successfully at `backend/target/taichinh-backend-0.0.1-SNAPSHOT.jar`.
4. Frontend lint: **PASS**
   - ESLint completed with `--max-warnings 0`.
5. Frontend build: **PASS**
   - TypeScript + Vite production build completed successfully.
6. FE to BE contract scan: **PASS**
   - Frontend services call backend contracts through `apiClient`.
   - Base URL default `/api` aligns with backend `/api/*` routes.
   - Wallet/category/transaction pagination sends 1-based `page`.
   - Sort format uses `field,dir`, matching backend `ListQueryParams`.
   - Transaction create/update/delete endpoints match backend routes.
7. Runtime HTTP smoke from tool shell: **BLOCKED**
   - `localhost:8080` was not reachable from the tool shell.
   - Backend started successfully on temporary port `18080`, but `curl` from a separate command still could not connect to `127.0.0.1:18080`.
   - This looks like a local tool/runtime networking boundary rather than a compile or contract failure, because the Spring app reached `Tomcat started on port 18080` and shut down cleanly.

## Defects found

- No application code defect found in this batch.
- Environment note: runtime HTTP smoke could not be completed from this tool shell due to localhost reachability between command sessions.

## Fixes applied during this batch

- Updated `docs/IMPLEMENTATION_CHECKLIST.md` to mark backend package check as done.

## Final batch status

- **PASS with runtime smoke caveat**
- MVP flow is connected at code/contract/build/test level.
- The only unresolved item is direct HTTP runtime verification from this tool environment, not a confirmed product bug.
