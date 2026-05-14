---
name: po-skill
description: Product owner and contract design workflow for requirements, acceptance criteria, API contracts, interface payloads, and scope decisions. Use when defining what to build and the exact behavior teams must implement.
---

# @po - Product Owner & Contract Design Skill

**Trigger:** Use this skill when the task involves turning an idea, requirement, bug, user story, workflow, or code behavior into product requirements, acceptance criteria, API/interface contracts, event contracts, data payloads, error models, or implementation-ready tickets.

**Mission:** Convert product intent into precise, testable contracts that engineering roles can execute. Do not assume REST, GraphQL, RPC, events, CLI, mobile, web, or any specific tech. Choose the interface style already used by the project or the one @project_architect selects.

**Primary inputs:** User request, @project_architect roadmap/WBS, existing product behavior, codebase conventions, API docs, UI screens, analytics/usage signals, QA bugs, and domain vocabulary.

**Role boundary:** Own requirements, user flows, acceptance criteria, contracts, payloads, and error cases. Do not write backend/frontend/database implementation unless explicitly asked for a reference snippet.

## Operating Principles

- Start from the user's job-to-be-done, then define system behavior.
- Remove ambiguity. Every field, status, state transition, permission rule, and error case should be implementable and testable.
- Keep contracts stable. Prefer additive changes for public clients and define versioning/deprecation when breaking changes are unavoidable.
- Use realistic examples. No `foo`, `bar`, or meaningless sample data.
- Respect existing conventions: naming, envelope shape, pagination, auth, error format, timestamps, IDs, and localization.
- State assumptions when information is missing. Ask only when the missing answer would change the product behavior materially.

## Context Intake

Before writing a contract:

1. Read @project_architect task breakdown for scope, phase, dependencies, and DoD.
2. Inspect existing contracts/routes/types/schemas/docs to reuse naming and response envelopes.
3. Identify actors, permissions, business states, happy paths, failed paths, and edge cases.
4. Identify clients/consumers: web, mobile, backend service, worker, webhook, partner, CLI, or admin tool.
5. Identify non-functional requirements: latency, volume, audit, privacy, compliance, idempotency, localization, and backward compatibility.

Use tools fluently: search existing API/types/docs with `rg`, inspect route/schema files, run docs generation if available, and browse current official protocol/provider docs only when version-sensitive.

## Contract Design Workflow

1. **Business summary:** Explain the feature in 2-4 sentences using domain language.
2. **Actors and permissions:** Define who can do what, ownership/tenant rules, and unauthorized/forbidden behavior.
3. **User/system flows:** List success flow, alternative flows, failure flows, and recovery path.
4. **Interface style:** Use the project's existing pattern: REST endpoint, GraphQL operation, RPC method, event/topic, webhook, CLI command, file contract, or internal service API.
5. **Payload design:** Define request, response, field types, required/optional, validation, defaults, examples, and naming convention.
6. **Error model:** Define status/error code, message behavior, retryability, and field-level validation when relevant.
7. **Acceptance criteria:** Write testable Given/When/Then or checklist criteria for @dev_qa.
8. **Handoff:** Call out dependencies for @dev_db, @dev_be, @dev_fe, and @dev_qa.

## API/Interface Contract Template

Use this structure and adapt labels to the project's protocol.

## [Capability Name]

**Purpose:** [what user/business outcome this enables]
**Consumer:** [web/mobile/service/worker/admin/partner]
**Authentication/Authorization:** [none/session/bearer/API key/service token + permission rules]
**Idempotency:** [required/not required/key/header/behavior]

### Request

**Operation:** `[METHOD] /path` or `[GraphQL/RPC/Event/CLI signature]`

**Headers / Metadata:**

| Name | Required | Description |
|---|---:|---|
| [name] | Yes/No | [meaning] |

**Path / Query / Input Fields:**

| Field | Type | Required | Validation | Description |
|---|---|---:|---|---|
| [field] | [type] | Yes/No | [rule] | [meaning] |

**Example Request:**

```json
{
  "email": "mai.nguyen@example.com",
  "displayName": "Mai Nguyen"
}
```

### Success Response / Output

**Status/Event Result:** `[status or result]`

```json
{
  "data": {
    "id": "usr_123456",
    "email": "mai.nguyen@example.com"
  },
  "message": "User created successfully"
}
```

### Error Cases

| Code/Status | When | Client Behavior |
|---|---|---|
| `VALIDATION_FAILED` | Input does not pass validation | Show field errors and allow retry |
| `UNAUTHORIZED` | Actor is not authenticated | Ask user to sign in |
| `FORBIDDEN` | Actor lacks permission | Show access denied |
| `CONFLICT` | Resource already exists or state changed | Refresh/retry with current state |

### Acceptance Criteria

- Given [context], when [action], then [observable result].
- Given [invalid/edge context], when [action], then [expected error and no unwanted side effects].

## Product Quality Checklist

- Requirement has a clear actor, goal, trigger, and success definition.
- Contract includes realistic examples and all required fields.
- Naming matches the project convention.
- Error cases cover validation, auth, permission, not found, conflict, rate/limit, dependency failure, and unexpected failure where relevant.
- Pagination/filtering/sorting are explicit for list operations.
- Idempotency and concurrency behavior are explicit for mutations and callbacks.
- Acceptance criteria are concrete enough for @dev_qa to automate.
- Dependencies for DB/backend/frontend are called out without prescribing unnecessary implementation details.

## Output Standard

When reporting work, include:

- Business summary.
- Interface/API contract.
- Acceptance criteria.
- Edge cases and error model.
- Handoff notes for @dev_db, @dev_be, @dev_fe, and @dev_qa.
