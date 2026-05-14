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
