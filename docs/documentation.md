# Documentation Guide

This repository uses a small set of documentation types. Keep each document in
one role so readers know whether they are orienting themselves or executing an
operation.

## Document Types

| Type | Purpose | Typical location |
|------|---------|------------------|
| `README.md` | Orient readers: what this area is, what it owns, and where to go next. | Repository root, `docs/`, and module `docs/` directories. |
| Guide | Explain a stable concept, structure, policy, or decision. | `docs/*.md` or module `docs/*.md`. |
| Runbook | Execute an operation with prerequisites, steps, verification, and recovery notes. | `docs/runbooks/` or module `docs/runbooks/`. |
| Reference | Capture detailed API, generated-model, BDD, or tool-specific facts. | Close to the owning module or tool. |

## README Rules

A README should answer:

- What is this repository, module, or documentation area?
- What belongs here?
- Which contracts or boundaries matter?
- Where should the reader go for details?

Avoid putting long operational procedures in README files. Link to a runbook
instead.

## Runbook Rules

A runbook should answer:

- When to use it.
- Prerequisites and required credentials/tools.
- Exact commands or manual steps.
- How to verify success.
- Common failures and recovery steps.
- What not to do.

Runbooks should be named by operation, for example `trunk-sync.md` or
`release.md`.

## Placement

Project-wide documentation belongs under `docs/`.

Module-specific documentation belongs under that module's top-level `docs/`
directory. Use `docs/runbooks/` for module operations.

UML publication runbooks are the exception: keep them under the module's
`docs/uml/` directory because the PlantUML source, import notes, and helper
scripts should stay together.

## Current Entry Points

- Repository overview: `README.md`
- Project structure: `docs/project-structure.md`
- CI policy: `docs/ci/main-branch-protection.md`
- Figma design sync docs: `repo/figma-design-sync/docs/README.md`
