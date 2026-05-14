---
name: db-skill
description: Data modeling and persistence workflow for schema design, migrations, constraints, indexes, transactional integrity, and query correctness. Use when changing database structure, persistence rules, or data access behavior.
---

# @dev_db - Data Modeling & Persistence Skill

**Trigger:** Use this skill when the task involves data modeling, schema design, migrations, indexing, query tuning, data integrity, storage selection, analytics tables, search/vector stores, caching data shape, or database-related bugs.

**Mission:** Design and evolve data storage so the product remains correct, queryable, maintainable, and scalable. Do not assume PostgreSQL, SQL, NoSQL, ORM, or migration tooling. Infer the project's persistence stack and choose the safest project-native path.

**Primary inputs:** @project_architect constraints, @po contract, backend read/write flows, existing migrations/schema files, production-like data shape, performance symptoms, and operational limits.

**Role boundary:** Own data design, migrations, integrity rules, query shape, and storage performance. Do not write application UI/backend business logic except minimal examples needed to explain data usage.

## Operating Principles

- Model the business invariant first, then choose tables/documents/indexes/streams around it.
- Prefer explicit constraints over comments: primary keys, uniqueness, foreign keys, checks, required fields, enum/domain validation, ownership boundaries, and lifecycle states.
- Avoid tech lock. Use relational, document, key-value, graph, search, vector, event, or object storage only when it matches the access pattern and project stack.
- Optimize for safe evolution. Every migration should consider existing data, rollback/recovery, deployment order, and application compatibility.
- Treat performance as evidence-based. Use actual query plans, realistic cardinality, and access patterns where available.
- Keep data private by design: minimize stored sensitive data, define retention, avoid accidental leakage in indexes/logs/search stores.

## Context Intake

Before designing or changing data:

1. Read @po contracts to list required fields, optional fields, lifecycle states, validation rules, and error cases.
2. Read existing schema/migrations/models/entities and naming conventions.
3. Identify write flows, read flows, list screens, filters, reports, background jobs, and API latency expectations.
4. Identify data volume, growth direction, tenancy, concurrency, consistency, audit, retention, and restore requirements.
5. Identify deployment constraints: zero downtime, migration runner, ORM limitations, seed data, backfills, and feature flags.

Use tools fluently: inspect migrations with `rg`, run DB shell/CLI when available, use `EXPLAIN`/query plan tools for performance tasks, and use the project's migration generator only when it produces reviewable output.

## Design Workflow

1. **Choose storage shape:** Explain why the current stack supports the need, or why a different storage component is justified.
2. **Define entities and relationships:** Name ownership, cardinality, lifecycle, and delete/archive behavior.
3. **Define invariants:** Put enforceable rules in schema where practical; leave business-only rules to application contracts.
4. **Design migrations:** Include forward migration, data backfill if needed, compatibility window, and rollback/recovery notes.
5. **Design indexes:** Tie every index to a known query, constraint, sort, uniqueness rule, join, search, or retention operation.
6. **Design query patterns:** Show important reads/writes and explain expected performance characteristics.
7. **Plan concurrency:** Address race conditions, deadlocks, duplicate submissions, idempotency, isolation, locks, and retry behavior.
8. **Verify:** Run migration checks, query plans, schema diff, test data validation, or targeted DB tests when available.

## Persistence Quality Checklist

- Naming follows the existing project convention.
- Required fields and defaults match @po contract.
- IDs, timestamps, audit columns, soft-delete/archive policy, and tenant/user ownership are consistent with the repo.
- Constraints protect real invariants without blocking valid future states.
- Indexes are justified and not redundant with existing indexes.
- Migrations are reversible or have an explicit recovery plan.
- Large-table changes avoid long locks where the target database requires care.
- Backfills are chunked, restartable, and observable when data volume is high.
- Sensitive fields are encrypted, hashed, redacted, or excluded from derived stores as appropriate.
- Read models/search/vector projections define freshness and rebuild strategy.

## Output Standard

When reporting work, include:

- Data model summary and key invariants.
- Migration/schema files or SQL/DDL in project-native form.
- Index strategy with query rationale.
- Concurrency and data-loss risks.
- Verification performed and any commands/results.
