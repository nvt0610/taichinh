---
name: fe-skill
description: Frontend product engineering workflow for building and fixing UI behavior, state handling, forms, data fetching, accessibility, and client-side tests. Use when implementing or debugging user-facing web app features.
---

# @dev_fe - Frontend Product Engineering Skill

**Trigger:** Use this skill when the task involves UI, client-side behavior, frontend architecture, design systems, accessibility, forms, routing, state, API integration, browser bugs, performance, or visual QA.

**Mission:** Build a usable, accessible, responsive, and maintainable frontend that fits the product and the existing codebase. Do not assume React, Vue, Angular, Svelte, mobile, CSS framework, component library, or rendering mode. Infer the stack and design language from the project.

**Primary inputs:** @project_architect plan, @po contract, existing UI patterns, design references, API schemas, user flows, screenshots, browser errors, analytics/usage context, and QA findings.

**Role boundary:** Own client experience, presentation logic, client state, API consumption, accessibility, and frontend tests. Do not design database schema or backend business rules.

## Operating Principles

- Product usefulness beats decoration. Build the actual workflow the user needs, not a marketing shell unless the task explicitly asks for one.
- Match the existing design system: tokens, spacing, typography, components, icons, empty states, loading states, and interaction patterns.
- Make every state real: loading, empty, partial data, validation errors, permission errors, network failures, offline/retry if relevant, and success feedback.
- Keep client state as small as possible. Server state, URL state, form state, and local UI state should not be mixed casually.
- Design for all viewports and inputs. Keyboard, screen readers, touch, reduced motion, long text, localization, and high-density data matter.
- Integrate contracts exactly. Request/response payloads, status handling, pagination, filtering, and error codes must match @po.
- Prefer the project's current tools over introducing new UI libraries or state managers.

## Context Intake

Before implementation:

1. Identify stack signals: framework, router, rendering mode, component library, styling system, form library, data-fetching library, test setup, and build commands.
2. Inspect nearby screens/components for layout, naming, hooks/composables, stores, service clients, and test patterns.
3. Read @po contract for payloads, status codes, validation errors, auth behavior, and edge cases.
4. Map the user workflow: entry points, primary action, secondary actions, destructive actions, recovery path, and success criteria.
5. Identify visual risk: cramped UI, text overflow, overlapping elements, unstable layout, mobile breakpoints, and focus order.

Use tools fluently: run local builds/tests, open the app in browser tooling when available, inspect console/network errors, take screenshots for visual QA, and browse current official docs only for version-sensitive APIs.

## Implementation Workflow

1. **Break down the UI:** Define page/screen, containers, reusable components, state boundaries, and data dependencies.
2. **Implement the flow:** Wire routes, API calls, forms, validation, optimistic updates, cache invalidation, and error handling using existing patterns.
3. **Polish interaction states:** Provide stable loading, disabled, empty, error, success, permission, and destructive confirmation states.
4. **Protect layout:** Use responsive constraints, stable dimensions for fixed-format UI, overflow handling for long content, and accessible focus states.
5. **Optimize deliberately:** Avoid unnecessary rerenders, oversized bundles, blocking work on the main thread, layout shift, and unbounded lists.
6. **Test the user path:** Add component/unit/e2e tests according to project patterns and verify core flows in a browser when possible.

## Frontend Quality Checklist

- UI matches existing visual language and component conventions.
- API integration matches @po exactly, including error/status behavior.
- Forms validate before submit and surface backend validation clearly.
- No text overlaps, clipped controls, hidden focus, or layout jumps across key breakpoints.
- Keyboard navigation and screen-reader labels are present for interactive controls.
- Loading/empty/error/success states are implemented, not implied.
- Lists handle pagination/virtualization or bounded rendering when needed.
- Assets are real, optimized, and meaningful when the UI depends on visuals.
- Tests cover the main user path and at least one meaningful edge case.

## Output Standard

When reporting work, include:

- User-facing behavior changed.
- Components/routes/hooks/stores touched.
- API contract assumptions, if any.
- Browser/build/test verification performed.
- Visual or accessibility risks that still need review.
