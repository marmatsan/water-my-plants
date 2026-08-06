---
title: Canonical code-generation decision system
type: adr
scope: repository
owner: engineering
status: accepted
last-reviewed: 2026-08-06
review-cycle-days: 365
sources:
  - AGENTS.md
  - docs/documentation.md
  - .teamcity/documentation-coverage.json
---

# ADR-0014: Use A Canonical Code-Generation Decision System

## Context

The repository uses multiple AI-capable development surfaces and already owns
typed standards, guides, references, ADRs, executable behavior, and CI-backed
documentation validation. Detailed policy in agent-specific files would create
parallel sources of truth and drift as tools or instruction formats change.

Long-running changes also need approved intent that can survive multiple work
sessions without being confused with current implemented behavior.

## Decision

Use the existing typed documentation system as the normative knowledge layer
for code generation. Code, tests, schemas, and versioned configuration describe
current behavior; standards and accepted ADRs constrain changes; references
describe current contracts; guides describe supported implementation paths.

Use `AGENTS.md` as a routing and completion contract. Scoped `AGENTS.md` files
may add local boundaries, exceptions, and verification, but must link shared
rules instead of copying them.

Use `specs/<id>-<name>/` for approved active change intent. A specification may
define the desired outcome but cannot silently override a standard or accepted
ADR. Promote durable knowledge and remove the active package when the change is
complete.

Write standards positive-first: state the supported outcome and preferred
pattern before constraints. A prohibited alternative must identify the
supported replacement or the safety boundary that leaves no valid replacement.

Keep reviewer profiles and skills as thin adapters. They select canonical
documents, execute a focused workflow, and report verification; they do not own
repository policy.

## Consequences

- Humans and AI tools use the same reviewed engineering contract.
- Provider-specific integrations can change without rewriting architecture or
  implementation standards.
- Active specifications preserve intent without claiming that planned behavior
  is already implemented.
- Documentation validation must recognize specification packages and protect
  their structure.
- Contributors must promote reusable learning before removing an active
  specification.

## Alternatives

- Store all rules in `AGENTS.md`. Rejected because global and scoped files would
  duplicate typed standards and become difficult to validate by concern.
- Keep a separate rule library for each AI provider. Rejected because provider
  syntax is an adapter concern, not an engineering-policy boundary.
- Treat implementation plans as permanent current documentation. Rejected
  because completed plans become stale and can contradict executable behavior.
- Create a skill for every possible Android or Kotlin topic. Rejected because
  skills are justified by repeated workflows, not speculative taxonomy.

## Supersession

None.
