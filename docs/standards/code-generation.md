---
title: Code-generation standard
type: standard
scope: repository
owner: engineering
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - AGENTS.md
  - docs/documentation.md
  - docs/decisions/adr-0014-use-canonical-code-generation-decisions.md
  - docs/reference/code-generation-context.md
  - specs/README.md
---

# Code-Generation Standard

## Purpose

Define how humans, AI agents, scaffolds, and generators select and apply the
repository's engineering decisions. Generated code has the same architecture,
quality, documentation, and verification obligations as manually written code.

## Required Outcomes

- Identify the affected capability and consumer before selecting a pattern.
- Load the applicable standards, guide, references, active specification, and
  scoped agent instructions before generating source.
- Inspect current executable code and tests for verified examples. Treat
  scaffold code as non-authoritative when module documentation says it is not a
  production precedent.
- Produce the smallest coherent change that satisfies the approved outcome.
- Preserve dependency direction, package cohesion, typed failure semantics,
  test ownership, and public API boundaries.
- Run focused verification during implementation and aggregate verification
  before completion.
- Promote reusable decisions and verification learning to canonical
  documentation in the same change.

## Preferred Patterns

1. Start from the consumer-visible outcome and concrete examples.
2. Select the context row in
   [`../reference/code-generation-context.md`](../reference/code-generation-context.md).
3. Read every required source named by that row; follow scoped `AGENTS.md`
   additions for files below their directory.
4. Model domain behavior and consumer-owned ports before concrete adapters.
5. Bind concrete implementations in the applicable composition root.
6. Drive behavior through the smallest deterministic test and add BDD only
   when the behavior merits executable living documentation.
7. Generate checked-in Kotlin in a capability package that matches its path.
8. Review every materially changed type against SRP, OCP, LSP, ISP, and DIP.
9. Update the exact standard, reference, guide, runbook, ADR, or executable
   contract that owns a new decision.

## Decision Rules

- When code and prose disagree about current behavior, correct the lower
  precedence artifact; do not weaken the executable contract to preserve stale
  prose.
- When an active specification conflicts with a standard or accepted ADR,
  record and approve an exception or a new decision before implementation.
- When several modules repeat one capability, place the shared abstraction in
  the narrowest consumer-appropriate shared module after its boundary is
  demonstrated.
- When only one consumer exists and no variation point is demonstrated, keep
  the implementation concrete and focused.
- When a new recurring concern appears, evolve the relevant standard from
  reviewed implementation and tests in the same change.
- When an operation is destructive, credentialed, external, or recovery
  oriented, follow or create a runbook rather than hiding the procedure in a
  skill.

## Disallowed Alternatives

| Do not generate | Generate or use instead | Reason |
|-----------------|-------------------------|--------|
| Product behavior in `:app` or repository tooling | A focused feature/core capability assembled by the product composition root | Keeps product and tooling boundaries explicit. |
| A service locator or global mutable container | Constructor-injected collaborators selected by a composition root | Preserves ownership and testability. |
| Transport DTOs, persistence entities, or Compose state as domain models | Explicit boundary models and tested mapping | Prevents infrastructure leakage. |
| `null` or catch-all exceptions for expected failure | Consumer-owned typed errors in the repository `Result` contract | Makes recoverable behavior explicit. |
| Generic packages such as `model`, `service`, `manager`, `helper`, or `utils` | A capability package with one cohesive reason to change | Makes ownership and change boundaries visible. |
| Hard-coded dependency versions or repeated Gradle defaults | Type-safe catalogs and repository convention plugins | Keeps build policy centralized and verifiable. |
| Sleeps, machine state, or real external services in deterministic tests | Controlled clocks, dispatchers, fixtures, and local boundary doubles | Keeps tests reliable. |
| Copied standards inside an agent or skill | Links to the canonical standard, guide, reference, and focused checks | Prevents instruction drift. |
| A speculative abstraction, module, standard, or skill | The simplest current implementation plus a documented promotion trigger | Avoids unsupported architecture. |
| Generated or temporary artifacts as source of truth | Reviewed source plus a documented regeneration path | Preserves reviewable ownership. |

## Exceptions

An exception to a `MUST` or `MUST NOT` rule requires the review mechanism named
by its owning standard. Architectural exceptions require an accepted ADR.
Record a temporary, change-local exception in the active specification and
promote it before completion if it remains reusable.

An agent must stop and request a decision when the available sources permit
materially different architectures or require a technology selection not yet
approved by the repository.

## Verification

- Run the smallest affected test or verification task while iterating.
- Run `./gradlew checkDocumentation` after documentation or specification
  changes.
- Run the focused boundary checks selected by the context reference.
- Run `./gradlew check` before completion.
- Inspect the final diff for copied policy, stale paths, generated artifacts,
  and undocumented decisions.

## Sources

- [ADR-0014](../decisions/adr-0014-use-canonical-code-generation-decisions.md)
- [Documentation standard](../documentation.md)
- [Code-generation context](../reference/code-generation-context.md)
- [Product architecture standard](architecture.md)
- [Testing standard](testing.md)
- [`AGENTS.md`](../../AGENTS.md)
- [`specs/README.md`](../../specs/README.md)
