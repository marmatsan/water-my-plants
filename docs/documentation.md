---
title: Documentation standard
type: standard
scope: repository
owner: engineering
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - AGENTS.md
  - .teamcity/documentation-coverage.json
---

# Documentation Standard

This document is the canonical documentation policy for Water My Plants. It
defines where knowledge belongs, which documents are normative, and what CI
must validate. Documentation must describe the current repository or clearly
label a future decision as proposed.

## Source Of Truth

Use this precedence when two artifacts disagree:

1. executable code, tests, schemas, and versioned build configuration define
   current behavior;
2. standards define mandatory engineering policy;
3. accepted ADRs explain why architectural decisions were made;
4. references describe exact contracts derived from source;
5. guides explain supported development workflows;
6. runbooks execute or recover operational procedures;
7. Figma and rendered UML are derived publication surfaces.

Correct the lower-precedence document when it diverges. Do not weaken an
executable contract only to preserve stale prose.

## Document Types

| Type | Question answered | Canonical location |
|------|-------------------|--------------------|
| `README.md` | What does this area own and where should I continue? | Repository, documentation root, or module `docs/` root. |
| `standard` | What must or should implementation follow? | `docs/standards/` or `<module>/docs/standards/`. |
| `guide` | How do I implement a supported development change? | `docs/guides/` or `<module>/docs/guides/`. |
| `runbook` | How do I execute, verify, or recover an operation? | `docs/runbooks/` or `<module>/docs/runbooks/`. |
| `reference` | What is the exact current contract or inventory? | `docs/reference/` or `<module>/docs/reference/`. |
| `adr` | Why was a durable architectural decision taken? | `docs/decisions/`. |

Specialized executable or generated documentation may remain in `docs/ci/`,
`docs/bdd/`, `docs/dokka/`, or `docs/uml/`. These directories do not replace
the document types above.

## Normative Language

Standards use these terms deliberately:

- `MUST` or `MUST NOT`: required and review-blocking;
- `SHOULD` or `SHOULD NOT`: expected unless the exception is documented;
- `MAY`: optional and context-dependent.

Rules that can be checked mechanically SHOULD be enforced by tests, lint, or
CI. Prose remains necessary for design constraints that cannot be automated.

## Placement And Ownership

Project-wide rules and concepts belong under `docs/`. Documentation specific
to one module belongs under that module's single top-level `docs/` directory.
Do not create nested documentation roots.

A rule used by multiple modules MUST have one canonical project-wide document.
Module documentation links to that rule and records only ownership, contracts,
or explicit exceptions. Do not duplicate shared rules between modules.

README files are indexes, not handbooks. They SHOULD state purpose, ownership,
boundaries, verification entry points, and links to canonical detail.

## Required Metadata

Typed Markdown documents MUST start with YAML frontmatter containing:

```yaml
---
title: Human-readable title
type: standard | guide | runbook | reference | adr
scope: repository or module path
owner: stable area name
status: draft | active | accepted | deprecated | superseded
last-reviewed: YYYY-MM-DD
review-cycle-days: positive integer
sources:
  - canonical/source/path
---
```

Use stable ownership areas such as `android`, `repository-tooling`, or
`figma-design-sync`, not an individual's name. Never put credentials or local
machine paths in metadata.

Runbooks and external integration references SHOULD use a 90-day review cycle.
Standards and guides SHOULD use 180 days. ADRs record review metadata but are
changed by superseding decisions, not rewritten to hide history.

## Runbook Contract

A runbook MUST make the following information easy to find:

1. when to use it;
2. prerequisites, permissions, tools, and authorized inputs;
3. ordered commands or manual actions;
4. success verification;
5. recovery or rollback;
6. prohibited actions and safety boundaries;
7. canonical sources and related contracts.

Commands MUST be directly executable in the stated shell. Secrets must be
referenced by storage location or environment variable name, never included as
literal values.

## Guide And Reference Contract

A guide MUST describe one supported development outcome and link to the
standards it applies. It SHOULD explain choices and expected verification, but
must not become an incident-recovery procedure.

A reference MUST describe current names, fields, paths, or contracts precisely.
It should optimize for lookup rather than teach a workflow. Generated data may
support a reference but must not replace its canonical source.

## ADR Contract

ADRs use `adr-NNNN-kebab-case-title.md`. An ADR records context, decision,
consequences, alternatives, and links to superseded decisions. Accepted ADRs
are immutable except for status and supersession links; a changed decision gets
a new ADR.

## Production Documentation

Before production behavior is added, the applicable project standards MUST be
identified. New product work begins with the standards under
`docs/standards/` and the relevant implementation guide under `docs/guides/`.

Product modules keep `<module>/docs/README.md` as their local entry point. The
module README MUST link to shared standards and describe only module purpose,
public boundaries, dependencies, and focused verification.

Production standards evolve incrementally with the implementation. When
product work introduces a new recurring concern, such as coroutine usage,
database access, `data`/`domain`/`ui` layer responsibilities, ViewModels, or
dependency injection, the same change MUST add or refine the applicable
standard and link it from the relevant module documentation. Do not create
speculative rules before a concrete need exists; derive them from reviewed
code, tests, and architectural decisions, then use the documented contract for
subsequent work.

## Derived Visual Documentation

PlantUML source remains the reviewed UML source of truth. Figma contains the
published visual result and must link back to canonical repository sources.
Manual Figma edits cannot override a versioned standard, reference, YAML model,
test, or runbook.

## CI Enforcement

CI validates typed document placement, frontmatter, review dates, ADR names,
runbook sections, and local Markdown links. Documentation coverage rules in
`.teamcity/documentation-coverage.json` map implementation areas to canonical
documents that must change with them.

Review-expiry findings are warnings. Invalid structure, missing metadata,
broken links, and uncovered mapped implementation changes are failures.

## Templates

Start new documents from `docs/templates/`. Remove all placeholder text before
review and add the new document to the closest README index.
