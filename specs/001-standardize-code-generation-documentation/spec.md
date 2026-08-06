---
title: Standardize code-generation documentation
type: specification
scope: repository
owner: engineering
status: active
last-reviewed: 2026-08-06
review-cycle-days: 30
sources:
  - AGENTS.md
  - docs/documentation.md
  - docs/standards/architecture.md
  - docs/guides/add-feature.md
---

# Standardize Code-Generation Documentation

## Outcome

Provide one versioned decision system that lets an AI or human contributor
determine which patterns to apply, which alternatives are outside the
repository contract, and which verification proves a generated change.

## Context

The repository already has typed standards, guides, references, ADRs, and
documentation validation. Agent instructions currently mix routing with
detailed policy, while there is no validated package for active change intent
or repository-scoped reusable skills.

The change must strengthen the existing documentation hierarchy rather than
create a parallel source of truth for one AI provider.

## Required Behavior

- Keep durable engineering policy in typed repository documentation.
- Make `AGENTS.md` route work to canonical standards, guides, references, and
  scoped instructions.
- Express required generation behavior positively before documenting invalid
  alternatives.
- Pair every prohibited alternative with the supported replacement or a clear
  safety boundary.
- Keep active specifications distinct from current executable behavior.
- Keep reviewer profiles and skills concise and free from copied normative
  rules.
- Validate specification placement, metadata, required sections, and links in
  `checkDocumentation`.

## Acceptance Criteria

- A contributor can map a proposed source change to its applicable standards,
  implementation guide, exact references, and verification commands.
- Root and module `AGENTS.md` files contain scoped routing and exceptions rather
  than duplicated project-wide policy.
- The documentation standard defines active specifications and positive-first
  instruction design.
- A repository specification package is validated by focused unit tests and
  the Gradle documentation check.
- Repository-scoped skills reference canonical guides and standards without
  embedding a second copy of their rules.
- Documentation indexes expose the new standard, reference, ADR, templates,
  specification system, and agent adapters.
- Focused verification and the root `check` lifecycle pass.

## Non-Goals

- Selecting new Android, persistence, navigation, network, or dependency
  injection technologies without a real production consumer.
- Generating a broad library of speculative skills.
- Treating AI-generated code as a separate quality tier.
- Encoding transient machine paths, credentials, incident identifiers, or
  provider-specific UI behavior as repository policy.

## Decision Log

| Date | Decision | Durable destination |
|------|----------|---------------------|
| 2026-08-06 | Keep typed repository documentation as the normative layer. | ADR and documentation standard. |
| 2026-08-06 | Model desired change intent as a temporary active specification. | Documentation standard and `specs/README.md`. |
| 2026-08-06 | Put supported behavior before prohibited alternatives and always name the replacement. | Code-generation standard and standard template. |
| 2026-08-06 | Keep agents and skills as thin procedural adapters. | ADR, code-generation standard, and `.agents/README.md`. |
| 2026-08-06 | Create only the four implementation skills and three reviewer profiles demonstrated by current repository workflows. | `.agents/skills/` and `.agents/reviewers/`. |
| 2026-08-06 | Preserve dot-prefixed repository roots when normalizing documentation paths. | Documentation coverage reference and path-resolver regression test. |
| 2026-08-06 | Use the official skill compatibility validator and document its PyYAML environment prerequisite. | `.agents/README.md`. |

## Sources

- `AGENTS.md`
- `docs/documentation.md`
- `docs/standards/architecture.md`
- `docs/guides/add-feature.md`
- `.teamcity/documentation-coverage.json`
