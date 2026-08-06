---
title: Implement the code-generation documentation system
type: specification
scope: repository
owner: engineering
status: active
last-reviewed: 2026-08-06
review-cycle-days: 30
sources:
  - specs/001-standardize-code-generation-documentation/spec.md
---

# Implement The Code-Generation Documentation System

## Outcome

Deliver the approved specification in reviewable slices while keeping every
new decision in its canonical durable document.

## Steps

1. Record the documentation architecture in an ADR.
2. Add the code-generation standard and context-routing reference.
3. Update the documentation taxonomy, templates, indexes, and project
   structure reference.
4. Move global UML, Gradle, and repository-hygiene policy out of broad agent or
   architecture instructions when each concern has its own reason to change.
5. Reduce root and module agent instructions to routing, local boundaries,
   exceptions, and focused verification.
6. Add specification classification and section validation with focused tests.
7. Add the minimal reviewer profiles and repository skills as thin adapters.
8. Run focused and aggregate verification.
9. Perform the documentation-learning review and promote any reusable finding
   before removing this active specification package.

## Verification

- Run `:verification-platform:domain:test` for typed-document validation.
- Run `:verification-platform:data:test` for filesystem discovery.
- Validate every repository skill with the skill validator.
- Run `checkDocumentation` after documentation and coverage changes.
- Run the root `check` lifecycle before review.
- Inspect the final diff for duplicated rules and stale paths.

## Decision Documentation

Record a durable architectural choice in an ADR, a recurring implementation
rule in a standard, an exact name or mapping in a reference, a supported
development workflow in a guide, and an operational or recovery procedure in
a runbook. Record observable behavior in tests, schemas, or BDD features.

Update the decision log in `spec.md` when a choice is made, then promote that
choice before this active specification is removed.
