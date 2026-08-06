---
title: Documentation standard
type: standard
scope: repository
owner: engineering
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - AGENTS.md
  - docs/decisions/adr-0014-use-canonical-code-generation-decisions.md
  - docs/decisions/adr-0017-use-ooux-and-bdd-before-product-implementation.md
  - .teamcity/documentation-coverage.json
---

# Documentation Standard

This document is the canonical documentation policy for Water My Plants. It
defines where knowledge belongs, which documents are normative, and what CI
must validate. Documentation must describe the current repository or clearly
label future intent as an active specification or proposed decision.

## Source Of Truth

Use this precedence when two artifacts disagree:

1. executable code, tests, schemas, and versioned build configuration define
   current behavior;
2. standards define mandatory engineering policy;
3. accepted ADRs explain why architectural decisions were made;
4. references describe exact contracts derived from source;
5. guides explain supported development workflows;
6. runbooks execute or recover operational procedures;
7. Figma product designs define approved pre-implementation intent, while
   rendered UML and published architecture views are derived surfaces.

Correct the lower-precedence document when it diverges. Do not weaken an
executable contract only to preserve stale prose.

An active specification describes approved change intent, not current
behavior. Apply it together with standards and accepted ADRs. When the desired
outcome conflicts with either, record and approve the new decision or exception
before implementation. Current code and tests remain the source for what the
repository does until the change is implemented.

For user-visible product work, the approved OOUX, wireframe, action, and BDD
contract in the canonical Figma workspace defines the design target before
production implementation. It MUST clearly remain future intent until its
behavior is executable. Implementation discoveries that change an object,
action, outcome, failure, or side effect return to the design contract; Figma
does not silently override shipped behavior, and shipped behavior does not
silently rewrite approved intent.

## Document Types

| Type | Question answered | Canonical location |
|------|-------------------|--------------------|
| `README.md` | What does this area own and where should I continue? | Repository, documentation root, or module `docs/` root. |
| `standard` | What must or should implementation follow? | `docs/standards/` or `<module>/docs/standards/`. |
| `guide` | How do I implement a supported development change? | `docs/guides/` or `<module>/docs/guides/`. |
| `runbook` | How do I execute, verify, or recover an operation? | `docs/runbooks/` or `<module>/docs/runbooks/`. |
| `reference` | What is the exact current contract or inventory? | `docs/reference/` or `<module>/docs/reference/`. |
| `adr` | Why was a durable architectural decision taken? | `docs/decisions/`. |
| `specification` | What approved change is active and how will it be delivered and verified? | `specs/<id>-<name>/`. |

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

State the supported outcome and preferred pattern before its constraints. A
`MUST NOT` or `SHOULD NOT` rule MUST name the supported replacement in the same
rule or identify the safety boundary that leaves no valid replacement. Prefer
"use X when Y" over a list of rejected implementations.

## Placement And Ownership

Project-wide rules and concepts belong under `docs/`. Documentation specific
to one module belongs under that module's single top-level `docs/` directory.
Do not create nested documentation roots.

Active change intent belongs under the root `specs/` directory. Do not place
standards, general guides, current reference material, or operational runbooks
inside a specification package.

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
type: standard | guide | runbook | reference | adr | specification
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
`figma-documentation-sync`, not an individual's name. Never put credentials or local
machine paths in metadata.

Runbooks and external integration references SHOULD use a 90-day review cycle.
Standards and guides SHOULD use 180 days. ADRs record review metadata but are
changed by superseding decisions, not rewritten to hide history.
Active specifications SHOULD use a 30-day review cycle because stale change
intent is especially likely to misdirect implementation.

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

## Specification Contract

Create `specs/<three-digit-id>-<kebab-case-name>/` when approved work needs
durable context across several implementation steps, contracts, or sessions.
The package MUST contain:

1. `spec.md` with outcome, context, required behavior, acceptance criteria,
   non-goals, decision log, and sources;
2. `plan.md` with outcome, ordered steps, verification, and decision
   documentation;
3. `checklist.md` with scope, implementation, architecture and SOLID, testing
   and verification, documentation, and completion evidence.

Add `contracts/` or another supporting artifact only when it carries real
information required by the change. Do not generate placeholder research,
quick-start, data-model, contract, or checklist files.

Use `draft` while shaping intent and `active` after approval. Before completing
the implementation, promote every reusable decision to code, tests, a schema,
standard, ADR, reference, guide, or runbook. Remove the active package after
that promotion; Git history and the pull request retain the execution record
without presenting an old plan as current policy.

## ADR Contract

ADRs use `adr-NNNN-kebab-case-title.md`. An ADR records context, decision,
consequences, alternatives, and links to superseded decisions. Accepted ADRs
are immutable except for status and supersession links; a changed decision gets
a new ADR. A superseded ADR keeps its original source paths as historical
evidence; validation does not require those retired paths to remain present.

## Production Documentation

Before production behavior is added, the applicable project standards MUST be
identified. New product work begins with the standards under
`docs/standards/`, the
[code-generation context](reference/code-generation-context.md), and the
relevant implementation guide under `docs/guides/`.

Product modules keep `<module>/docs/README.md` as their local entry point. The
module README MUST link to shared standards and describe only module purpose,
public boundaries, dependencies, and focused verification.

User-visible product work MUST first apply the
[product design standard](standards/product-design.md) and the
[design-product-feature guide](guides/design-product-feature.md). Its OOUX
objects, complete user-action consequences, wireframe, visual design, and
representative BDD examples form the implementation handoff.

Production standards evolve incrementally with the implementation. When
product work introduces a new recurring concern, such as coroutine usage,
database access, `data`/`domain`/`ui` layer responsibilities, ViewModels, or
dependency injection, the same change MUST add or refine the applicable
standard and link it from the relevant module documentation. Do not create
speculative rules before a concrete need exists; derive them from reviewed
code, tests, and architectural decisions, then use the documented contract for
subsequent work.

## Implementation Learning Review

Before completing an implementation, migration, bootstrap, or recovery, compare
the behavior observed during verification with the current documentation. New
reusable knowledge, failure modes, permission boundaries, recovery steps, and
safety constraints MUST be documented in the same change when they are not
already covered.

Choose the document type from the operational value of the learning: update a
standard for a recurring rule, a guide for a supported development workflow, a
runbook for execution or recovery, and a reference for an exact contract. Add
the document to its nearest README index and extend documentation coverage when
the implementation area needs a durable code-to-document mapping.

The review MUST distinguish a verified reusable lesson from incident-only
detail. Do not preserve transient identifiers, credentials, machine-specific
paths, or unsafe emergency workarounds as normal procedure. Record a safe
recovery boundary when the exceptional behavior is important for future
diagnosis.

## Figma And Derived Visual Documentation

PlantUML source remains the reviewed UML source of truth. Figma contains the
published visual result and must link back to canonical repository sources.
Manual Figma edits cannot override a versioned standard, reference, YAML model,
test, or runbook.

Product design is a separate Figma responsibility. The canonical product file
owns approved pre-implementation OOUX, wireframe, visual, and component intent
under [ADR-0017](decisions/adr-0017-use-ooux-and-bdd-before-product-implementation.md).
Stable business examples move into repository `.feature` files when
implementation starts, and checked-in `.figma.kt` mappings own Code Connect
bindings. Product design pages are not a substitute for executable current
behavior or PlantUML architecture source.

## Agent And Skill Adapters

`AGENTS.md` routes a change to canonical repository documents and states
completion obligations. A scoped `AGENTS.md` adds only local boundaries,
exceptions, and focused verification.

Repository reviewer profiles and skills live under `.agents/`. They MAY select
documents and execute a repeatable workflow, but MUST link standards, guides,
references, and runbooks instead of copying their rules. Provider-specific
configuration is an adapter and cannot redefine the repository contract.

## CI Enforcement

CI validates typed document placement, frontmatter, review dates, ADR names,
runbook and specification sections, and local Markdown links. Documentation coverage rules in
`.teamcity/documentation-coverage.json` map implementation areas to canonical
documents that must change with them.

Review-expiry findings are warnings. Invalid structure, missing metadata,
broken links, and uncovered mapped implementation changes are failures.

## Templates

Start new documents and active specification artifacts from `docs/templates/`.
Remove all placeholder text before review and add the new durable document or
active specification to the closest README index.
