---
title: Typed documentation system
type: adr
scope: repository
owner: engineering
status: accepted
last-reviewed: 2026-07-18
review-cycle-days: 365
sources:
  - docs/documentation.md
  - .teamcity/documentation-coverage.json
---

# ADR-0001: Typed Documentation System

## Context

The repository already contains module indexes, CI references, Figma runbooks,
and UML publication rules. References and standards had started to live inside
`runbooks/`, making it unclear whether a reader was learning a contract or
executing an operation. Product development will add architecture, API, UI,
testing, and accessibility conventions, which would amplify that ambiguity.

## Decision

The repository will use typed documentation with canonical directories for
standards, guides, references, runbooks, and ADRs. Typed documents carry common
frontmatter and are validated by CI. README files remain navigation indexes,
and Figma remains a derived visual publication surface.

Shared production rules live under `docs/standards/`. Module documentation
links to shared rules and records only module-specific ownership or exceptions.

## Consequences

- Readers can infer a document's purpose from its path and metadata.
- CI can reject misplaced or structurally incomplete documentation.
- Existing links must be updated when documents are reclassified.
- Documentation changes become part of the same reviewed contract as code.

## Alternatives

- Keep all technical documentation in README files. Rejected because indexes
  would become large, duplicated, and operationally ambiguous.
- Keep topic-only directories such as `ci/` and `figma/` without document
  types. Retained only for specialized executable sources; general prose still
  follows the typed system.
- Store the canonical process in Figma. Rejected because visual publication is
  derived and cannot provide reviewable executable contracts by itself.

## Supersession

None.
