---
title: Project structure
type: reference
scope: repository
owner: architecture
status: active
last-reviewed: 2026-07-19
review-cycle-days: 180
sources:
  - settings.gradle.kts
  - repo/dependency-catalog/settings.gradle.kts
  - repo/gradle-plugins/settings.gradle.kts
  - repo/figma-design-sync/settings.gradle.kts
  - repo/figma-design-sync/data/build.gradle.kts
  - repo/figma-design-sync/data/src/main/kotlin/com/marmatsan/figmaDesignSync/data/mcp/KtorFigmaPngAssetUploader.kt
  - repo/figma-design-sync/project-config/build.gradle.kts
  - repo/figma-design-sync/project-config/src/main/kotlin/com/marmatsan/figmaDesignSync/projectConfig/UploadOfficialFigmaPayloadTask.kt
---

# Project Structure

This repository is split between product modules, repository infrastructure,
documentation, CI configuration, and generated build output.

## Root

```text
water-my-plants/
├── app/
├── core/
├── onboarding/
├── repo/
├── docs/
├── .teamcity/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

- `settings.gradle.kts` defines the root Gradle build and the production
  modules included in the app build.
- `build.gradle.kts` applies root-level plugins and shared root configuration.
- `gradle/`, `gradlew`, and `gradlew.bat` are the Gradle wrapper used to run the
  build consistently.
- `.teamcity/` and `teamcity.toml` contain CI configuration.
- `local.properties`, `.env`, `build/`, `.gradle/`, `.kotlin/`, `tmp/`, and
  module `build/` directories are local or generated state, not source of truth.

## Product Modules

These modules are part of the application build declared by the root
`settings.gradle.kts`.

| Path | Gradle module | Purpose |
|------|---------------|---------|
| `app/` | `:app` | Android application module and app-level BDD test suite. |
| `core/ui/` | `:core:ui` | Shared UI library used by feature and app modules. |
| `onboarding/ui/` | `:onboarding:ui` | Onboarding UI feature library. |

Product modules should contain app/runtime code, tests for that code, and
module-local documentation.

## Repository Infrastructure

`repo/` groups Gradle included builds and repository-owned tooling. These
modules support the repository and CI; they are not production app modules.

| Path | Included build | Purpose |
|------|----------------|---------|
| `repo/dependency-catalog/` | `dependency-catalog` | Parent included build for the reusable catalog engine and the Water My Plants catalog definition. |
| `repo/gradle-plugins/` | `gradle-plugins` | Convention plugins used by app modules and other repository builds. |
| `repo/figma-design-sync/` | `figma-design-sync` | Kotlin infrastructure that generates and executes the Figma sync contract, plus the TypeScript boundary evaluated by the Figma Plugin API. |

The root build includes `repo/gradle-plugins` and `repo/figma-design-sync`
through `pluginManagement.includeBuild(...)`. Both included builds consume
`repo/dependency-catalog`.

`repo/dependency-catalog` contains two Gradle modules with a one-way dependency:

| Path | Gradle module | Purpose |
|------|---------------|---------|
| `repo/dependency-catalog/catalog-core/` | `:catalog-core` | Reusable catalog tree model, DSL, traversal, and mappers. It does not know the Water My Plants dependencies. |
| `repo/dependency-catalog/water-my-plants-catalog/` | `:water-my-plants-catalog` | Concrete library/plugin trees, version schema, and `WaterMyPlantsCatalog` facade. Depends on `:catalog-core`. |

Repository tooling consumes the stable coordinates
`com.marmatsan.repo:catalog-core` and
`com.marmatsan.repo:water-my-plants-catalog`. The included-build root does not
publish a compatibility artifact.

`repo/figma-design-sync` separates its portable engine from this repository's
configuration:

| Path | Gradle module | Purpose |
|------|---------------|---------|
| `repo/figma-design-sync/domain/` | `:domain` | Portable design-model types and ports. |
| `repo/figma-design-sync/data/` | `:data` | Portable filesystem, Gradle, catalog-provider, CI, official MCP SDK, allow-listed PNG upload, runner-generation, and checkpoint adapters. It does not depend on `water-my-plants-catalog` in production. |
| `repo/figma-design-sync/plugin/` | `:plugin` | Reusable Gradle tasks, model generation, checks, and composition. |
| `repo/figma-design-sync/teamcity-adapter/` | `:teamcity-adapter` | Optional Kotlin translation from generated TeamCity YAML/XML to the portable CI model, plus typed TeamCity CLI access for artifacts and runs. |
| `repo/figma-design-sync/project-config/` | `:project-config` | Water My Plants paths, concrete catalog and CI providers, Figma identities, visual targets, credential adapters, verified official payload upload, repository-specific TeamCity orchestration, and adapter contract tests. |
| `repo/figma-design-sync/tools/` | not a Gradle module | TypeScript writer evaluated inside the Figma Plugin API runtime, plus preview tooling selected through the active project configuration. |

The root build applies the Water My Plants project adapter. That adapter applies
the portable plugin; another repository replaces `project-config` without
changing `domain`, `data`, `plugin`, or the writer implementation. It reuses
`teamcity-adapter` only if its CI provider is TeamCity.

The `project-config` test suite receives the language-neutral writer runtime
fixture from `tools/fixtures/contracts/` as a test-only system property. This
keeps the published contract executable against the typed Water My Plants
configuration without adding the tools package to production dependencies.

## Documentation

| Path | Purpose |
|------|---------|
| `README.md` | Repository entry point and links to deeper documentation. |
| `docs/documentation.md` | Canonical documentation taxonomy and validation contract. |
| `docs/standards/` | Project-wide engineering rules for production and tooling. |
| `docs/guides/` | Supported development workflows. |
| `docs/reference/` | Exact project contracts and inventories. |
| `docs/decisions/` | Architecture Decision Records. |
| `docs/runbooks/` | Project-wide operational execution and recovery procedures. |
| `docs/templates/` | Starting points for typed documentation. |
| `docs/ci/` | CI and branch protection documentation. |
| `docs/uml/` | Project-wide PlantUML diagrams and shared UML includes. |
| `<module>/docs/README.md` | Module documentation index and orientation. |
| `<module>/docs/standards/` | Rules owned only by that module. |
| `<module>/docs/guides/` | Module-specific development workflows. |
| `<module>/docs/reference/` | Module-specific contracts and inventories. |
| `<module>/docs/runbooks/` | Module-owned operational runbooks. |
| `<module>/docs/uml/` | Module-owned PlantUML diagrams, UML publication notes, and UML helper scripts. |

Project-wide architecture or workflow documentation belongs under `docs/`.
Module-specific documentation belongs under that module's top-level `docs/`
directory.

## Generated Output

Generated files and local caches should not be treated as source:

- `build/`
- `.gradle/`
- `.kotlin/`
- `tmp/`
- any module-local `build/` directory
- generated Figma sync JavaScript under `repo/figma-design-sync/tools/`

If a generated artifact is required for review, document how to regenerate it
instead of treating the generated file as the source of truth.
