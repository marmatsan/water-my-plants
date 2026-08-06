---
title: Code-generation context map
type: reference
scope: repository
owner: engineering
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - AGENTS.md
  - docs/standards/code-generation.md
  - docs/standards/README.md
  - docs/guides/README.md
  - docs/reference/project-structure.md
---

# Code-Generation Context Map

## Purpose

Map a proposed source change to the minimum canonical context and verification
that must be loaded before generating code.

## Contract

Always apply the [code-generation standard](../standards/code-generation.md),
the active specification when one governs the change, and every scoped
`AGENTS.md` between the repository root and the target file.

| Change context | Standards | Supported guide or reference | Executable evidence | Focused verification |
|----------------|-----------|------------------------------|---------------------|----------------------|
| Product design intent, wireframe, or visual screen design | [Product design](../standards/product-design.md), [testing](../standards/testing.md), [accessibility](../standards/accessibility.md), [documentation](../documentation.md) | [Design a product feature](../guides/design-product-feature.md), [product design workspace](product-design-workspace.md) | Figma OOUX/action contract, wireframe, visual design, repository `.feature`, and `.figma.kt` mappings | OOUX/action review, design-state inspection, Code Connect mapping, `checkDocumentation` |
| Product capability or domain behavior | [Architecture](../standards/architecture.md), [Kotlin](../standards/kotlin.md), [testing](../standards/testing.md), [errors](../standards/error-handling.md) | [Add a feature](../guides/add-feature.md) | Owning module tests and public boundary | Owning module `test`/`check`, module boundaries |
| Compose screen or component | [Compose](../standards/compose.md), [accessibility](../standards/accessibility.md), [Android](../standards/android.md), [testing](../standards/testing.md) | [Add a Compose screen](../guides/add-compose-screen.md) | Existing design-system tokens, previews, and UI tests | Owning module `check`, preview and accessibility inspection |
| Product or core module | [Architecture](../standards/architecture.md), [Android](../standards/android.md), [Gradle](../standards/gradle.md), [testing](../standards/testing.md) | [Add a module](../guides/add-module.md), [project structure](project-structure.md) | `settings.gradle.kts` and equivalent modules | New module `check`, `checkModuleBoundaries`, root `check` |
| API or infrastructure adapter | [Architecture](../standards/architecture.md), [API client](../standards/api-client.md), [errors](../standards/error-handling.md), [testing](../standards/testing.md) | [Add an API endpoint](../guides/add-api-endpoint.md) | Consumer-owned port and adapter contract tests | Focused unit/contract tests, typed-result check |
| Kotlin API or coroutine behavior | [Kotlin](../standards/kotlin.md), [architecture](../standards/architecture.md), [testing](../standards/testing.md) | Owning module README and Dokka reference | Current declarations, KDoc, deterministic tests | Kotlin style, Dokka, owning module `check` |
| Gradle plugin, task, catalog, or included build | [Gradle](../standards/gradle.md), [architecture](../standards/architecture.md), [testing](../standards/testing.md), [repository hygiene](../standards/repository-hygiene.md) | [Project structure](project-structure.md) and owning included-build docs | Plugin functional tests and standalone consumer fixtures | Owning included-build `check`, staged distribution, boundary checks |
| Typed documentation or specification validation | [Documentation](../documentation.md), [code generation](../standards/code-generation.md), [testing](../standards/testing.md) | [Documentation coverage](../ci/documentation-coverage.md) | Documentation validator and coverage tests | Verification-platform focused tests, `checkDocumentation` |
| UML or Figma-published architecture documentation | [UML](../standards/uml.md), [documentation](../documentation.md) | Owning module UML import runbook and visual-sync contract | `.puml` source and canonical Figma publication state | PlantUML render, structural/visual inspection, post-merge publication |

## Invariants

- A context row selects additional documents; it never reduces a stronger
  scoped instruction or executable contract.
- Current code and tests must be inspected before reusing an implementation
  pattern.
- Scaffold code explicitly identified as temporary is not executable precedent
  for new production architecture.
- A missing standard does not authorize improvisation. Use the simplest
  compatible implementation and document a recurring concern only after the
  change demonstrates it.
- Verification commands may be narrowed during iteration but must include the
  owning aggregate checks before completion.

## Sources

- [`AGENTS.md`](../../AGENTS.md)
- [Code-generation standard](../standards/code-generation.md)
- [Engineering standards index](../standards/README.md)
- [Development guides index](../guides/README.md)
- [Project structure](project-structure.md)
