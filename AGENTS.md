# Project Rules For Codex

These rules apply to this repository.

GitHub repository:

- `https://github.com/nvt0610/taichinh`

## MVP Progress Tracking

- Treat `docs/IMPLEMENTATION_CHECKLIST.md` as the main MVP roadmap and progress tracker.
- Before doing implementation work, read only the relevant section of the checklist, not the whole file unless needed.
- After completing code work that changes MVP progress, update the checklist in the same turn when appropriate.
- Use checklist status consistently:
  - `[x]` means done and minimally verified.
  - `[~]` means partially done or in progress.
  - `[ ]` means not started.
- Keep checklist updates concise. Do not turn the checklist into a long study note.

## Study Documentation

- Use `docs/study/` for learning notes about Spring Boot, clean architecture layers, and project-specific patterns.
- When adding a new important Spring Boot layer, class type, or architectural pattern, check whether a study note already exists.
- If no relevant study note exists, create one in `docs/study/`.
- If a relevant study note already exists, do not duplicate it; update it only when the new work materially adds a concept that is missing.
- Study notes should explain:
  - what the file/class/layer is,
  - how it works in this project,
  - where it sits in the request-to-database flow,
  - how to recognize when it is complete.

## Testing And Result Logging

- When the user asks to test, test thoroughly enough to meaningfully validate the requested slice instead of stopping at compile-only checks when deeper verification is feasible.
- Record every explicit testing pass in a timestamped batch report under `docs/test-results/`.
- Use one file per batch with naming format `YYYY-MM-DDTHH-MM-SSZ-<short-slug>.md`.
- Each batch report should include:
  - scope being tested,
  - environment/setup used,
  - commands executed,
  - pass/fail result for each major check,
  - defects found,
  - fixes applied during the batch if any,
  - final batch status.
- When reporting back to the user after testing, summarize the batch result clearly: pass hết hay fail ở đâu, không bỏ qua các lỗi đã gặp giữa chừng.

## Skill Usage

- Use the six role skills as the default routing layer:
  - `architect-skill` for architecture, roadmap, sequencing, and cross-module decisions.
  - `po-skill` for requirements, API contracts, payloads, acceptance criteria, and scope decisions.
  - `db-skill` for schema, migrations, constraints, indexes, query correctness, and data invariants.
  - `be-skill` for backend APIs, services, auth, domain logic, jobs, integrations, and backend tests.
  - `fe-skill` for frontend UI, state, forms, API consumption, accessibility, visual QA, and client tests.
  - `qa-skill` for test plans, regression checks, bug reproduction, test reports, and release confidence.
- Use specialist workspace skills only when their extra guidance clearly matches the task risk. Examples:
  - API contract/pagination/error/versioning work: `api-patterns` or `api-design-principles`.
  - Auth/JWT/permission/IDOR/security-sensitive work: `auth-implementation-patterns` or `security-auditor`.
  - Frontend API race-safety/client state work: `frontend-api-integration-patterns`.
  - React/TypeScript/Zustand/Tailwind-specific work: `react-patterns`, `typescript-expert`, `zustand-store-ts`, or `tailwind-patterns`.
  - UI polish/review/accessibility/browser checks: `frontend-design`, `design-taste-frontend`, `ui-review`, `ui-a11y`, `accessibility-compliance-accessibility-audit`, `playwright-skill`, or `e2e-testing`.
  - Bugs/test failures: `systematic-debugging`, then the relevant role skill.
  - Validation after code changes: `lint-and-validate` when it adds value beyond the project-specific testing rule.
- Do not stack many skills by default. Prefer one role skill plus at most one or two specialist skills for the current slice.
- If the user explicitly names a skill, use it unless it is unavailable or clearly unsafe for the request.
- For tiny self-contained tasks, skip extra specialist skills and follow the repository rules directly.
- Remember that installed skill files and skills loaded in the current session are different states. After skill installs or updates, a new chat or VS Code/Codex reload may be needed before the active skill list reflects the change.

## Implementation Style

- Prefer the existing Spring Boot package structure:
  - `controller`
  - `service`
  - `repository`
  - `entity`
  - `dto`
  - `exception`
  - `security`
  - `enums`
- Keep backend changes aligned with Flyway schema and `docs/FINANCE_TRACKER_DB_DESIGN.md`.
- For MVP work, finish the smallest checklist slice, verify it, then update tracking docs.

## Git And Ignore Hygiene

- Keep `.gitignore` strict before publishing or committing.
- Never commit secret files such as `.env`, `backend/.env`, or `frontend/.env`.
- Commit env templates such as `.env.example`, `backend/.env.example`, and `frontend/.env.example`.
- Do not ignore Maven Wrapper config under `backend/.mvn/`; it is needed so others can build with `./mvnw`.
- Do not commit build outputs such as `backend/target/`, `frontend/dist/`, `frontend/node_modules/`, logs, caches, or coverage folders.
