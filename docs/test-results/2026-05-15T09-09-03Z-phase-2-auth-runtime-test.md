# Test Batch: Phase 2 Auth Runtime

- Timestamp: `2026-05-15T09-09-03Z`
- Scope: Phase 2 authentication end-to-end verification, including register, login, JWT access token, refresh token, logout/revoke, JWT filter, and protected route behavior.
- Environment:
  - PostgreSQL container `taichinh_db` running and healthy on `localhost:5433`
  - Spring Boot app against local dev config
  - Test stack: Maven Surefire + JUnit 5 + Spring Boot integration test

## Commands Executed

```bash
./mvnw test
./mvnw -DskipTests package
```

Additional runtime validation was exercised by integration tests against the real PostgreSQL container.

## Batch Checks

- `GET /api/health`
  - Result: PASS
  - Notes: health endpoint returns success response.

- Protected route without token
  - Result: PASS
  - Notes: request to `/api/wallets` without JWT returns `401 Unauthorized`.

- `POST /api/auth/register`
  - Result: PASS
  - Notes: user registration succeeds, password is stored hashed, default `USER` role is assigned.

- `POST /api/auth/login`
  - Result: PASS
  - Notes: valid credentials return `accessToken`, `refreshToken`, role list, and login metadata.

- Protected route with valid access token
  - Result: PASS
  - Notes: request reaches protected area successfully. Because wallet controller does not exist yet, authenticated request ends in `404`, which is the expected current behavior.

- `POST /api/auth/refresh`
  - Result: PASS
  - Notes: valid refresh token returns a new access token.

- `POST /api/auth/logout`
  - Result: PASS
  - Notes: refresh token is revoked successfully.

- `POST /api/auth/refresh` after logout
  - Result: PASS
  - Notes: revoked refresh token is rejected with `401` and error code `INVALID_REFRESH_TOKEN`.

- JWT unit behavior
  - Result: PASS
  - Notes: claims generation/parsing and invalid-token filter behavior are covered by focused unit tests.

## Defects Found During Batch

### 1. Register flow failed at runtime with `409 CONFLICT`

- Where: `AuthService.register(...)`
- Cause: user row was not flushed before inserting into `user_roles`, so runtime DB behavior could violate the FK flow.
- Fix: changed `userRepository.save(...)` to `userRepository.saveAndFlush(...)`.
- Status: Fixed and verified in the same batch.

### 2. Authenticated request to missing protected route returned `500` instead of `404`

- Where: global exception handling for `NoResourceFoundException`
- Cause: missing route was falling through to the generic exception handler.
- Fix: added a specific handler for `NoResourceFoundException` in `GlobalExceptionHandler`.
- Status: Fixed and verified in the same batch.

### 3. JWT filter unit test initially failed on `Instant` serialization

- Where: `JwtAuthenticationFilterTest`
- Cause: test-local `ObjectMapper` did not register Java time modules.
- Fix: switched test `ObjectMapper` to `findAndRegisterModules()`.
- Status: Fixed and verified in the same batch.

### 4. Integration test client hit retry/auth behavior on `401` POST handling

- Where: integration test helper for auth POST requests
- Cause: `TestRestTemplate` + underlying `HttpURLConnection` behaved poorly for the `401` refresh-after-logout case.
- Fix: switched POST helper in integration test to JDK `HttpClient`.
- Status: Fixed and verified in the same batch.

## Final Batch Status

- Overall result: PASS
- Final evidence:
  - `./mvnw test` passed
  - `./mvnw -DskipTests package` passed
  - Auth flow is verified against a real PostgreSQL container for the currently implemented Phase 2 scope
