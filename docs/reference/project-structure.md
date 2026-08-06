---
title: Project structure
type: reference
scope: repository
owner: architecture
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - settings.gradle.kts
  - repo/dependency-catalog/settings.gradle.kts
  - repo/gradle-plugins/settings.gradle.kts
  - repo/unit-testing/settings.gradle.kts
  - repo/verification-platform/settings.gradle.kts
  - repo/verification-platform/domain/build.gradle.kts
  - repo/verification-platform/data/build.gradle.kts
  - repo/verification-platform/plugin/build.gradle.kts
  - repo/figma-documentation-sync/settings.gradle.kts
  - repo/figma-documentation-sync/data/build.gradle.kts
  - repo/figma-documentation-sync/data/src/main/kotlin/com/marmatsan/figmaDocumentationSync/data/mcp/KtorFigmaPngAssetUploader.kt
  - repo/figma-documentation-sync/teamcity-operations/build.gradle.kts
  - repo/figma-documentation-sync/teamcity-operations/src/main/kotlin/com/marmatsan/figmaDocumentationSync/teamcity/operations/task/UploadCanonicalFigmaPayloadTask.kt
  - repo/project-config/settings.gradle.kts
  - repo/project-config/plugin/build.gradle.kts
  - repo/project-config/figma-adapter/build.gradle.kts
  - .agents/README.md
  - specs/README.md
---

# Project Structure

This repository is split between product modules, repository infrastructure,
documentation, CI configuration, and generated build output.

## Root

```text
water-my-plants/
├── .agents/
├── app/
├── core/
├── onboarding/
├── repo/
├── docs/
├── specs/
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
- `.agents/` contains repository-scoped reviewer and skill adapters that route
  work to canonical documentation.
- `specs/` contains approved active change intent and is not a description of
  current implemented behavior.
- `local.properties`, `.env`, `build/`, `.gradle/`, `.kotlin/`, `tmp/`, and
  module `build/` directories are local or generated state, not source of truth.

## Product Modules

These modules are part of the application build declared by the root
`settings.gradle.kts`.

| Path             | Gradle module    | Purpose                                                  |
|------------------|------------------|----------------------------------------------------------|
| `app/`           | `:app`           | Android application module and app-level BDD test suite. |
| `core/ui/`       | `:core:ui`       | Shared UI library used by feature and app modules.       |
| `onboarding/ui/` | `:onboarding:ui` | Onboarding UI feature library.                           |

Product modules should contain app/runtime code, tests for that code, and
module-local documentation.

## Repository Infrastructure

`repo/` groups Gradle included builds and repository-owned tooling. These
modules support the repository and CI; they are not production app modules.

| Path                             | Included build             | Purpose                                                                                                                                    |
|----------------------------------|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `repo/dependency-catalog/`       | `dependency-catalog`       | Reusable catalog API, optional tree DSL, and Gradle settings adapter.                                                                      |
| `repo/gradle-plugins/`           | `gradle-plugins`           | Convention plugins used by app modules and other repository builds.                                                                        |
| `repo/unit-testing/`             | `unit-testing`             | Assertion-framework-agnostic typed test APIs that can be consumed independently.                                                           |
| `repo/verification-platform/`    | `verification-platform`    | Provider-neutral Kotlin platform that plans and executes repository verification through Gradle.                                           |
| `repo/figma-documentation-sync/` | `figma-documentation-sync` | Kotlin infrastructure that generates and executes the Figma sync contract, plus the TypeScript boundary evaluated by the Figma Plugin API. |
| `repo/project-config/`           | `project-config`           | Reusable Settings and Project composition entry points for consumer-owned dependency catalogs and repository capabilities.                 |

The root build includes its plugin-producing builds through
`pluginManagement.includeBuild(...)`. It includes `gradle-plugins` and
`unit-testing` as regular composite participants so repository-owned library
coordinates are substituted from source. Reusable builds do not include
sibling builds or import product implementations. They declare test-only API
coordinates in their own catalogs; the root composite substitutes those
coordinates during repository development. The root and `repo/project-config`
are the composition boundaries that bind versioned APIs to local
implementations. Every included build reads its own
root `versions.properties` and remains independent from another build's
compile/test registry.

Every catalog-consuming included build constructs its local `libs` and
`plugins` catalogs through `com.marmatsan.dependencyCatalog.tree`.
`repo/dependency-catalog` is the deliberate bootstrap exception: it declares
its own build catalog manually because it cannot resolve the plugin that it is
currently producing. These local tool catalogs are not Water My Plants product
catalogs and are not published as Figma dependency trees.

`repo/unit-testing` owns `:unit-test-dsl`, the Kotlin-only,
assertion-framework-agnostic behavior-phase API. It publishes
`com.marmatsan.repo:unit-test-dsl` independently from the convention plugins
that consume it. Water My Plants registers it in the consumer-owned `testLibs`
catalog, so it never enters the production `libs` tree or app runtime graph.

`repo/dependency-catalog` contains four reusable Gradle modules:

| Path                                                  | Gradle module                 | Purpose                                                                                                                  |
|-------------------------------------------------------|-------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| `repo/dependency-catalog/catalog-api/`                | `:catalog-api`                | Immutable catalog model and segregated resolved/aliased provider APIs.                                                   |
| `repo/dependency-catalog/catalog-core/`               | `:catalog-core`               | Optional tree DSL, version strategies, traversal, and canonical mapping to `:catalog-api` for provider implementations.  |
| `repo/dependency-catalog/catalog-gradle-plugin/`      | `:catalog-gradle-plugin`      | Reusable `com.marmatsan.dependencyCatalog` settings plugin. Depends only on `:catalog-api`.                              |
| `repo/dependency-catalog/catalog-tree-gradle-plugin/` | `:catalog-tree-gradle-plugin` | Reusable `com.marmatsan.dependencyCatalog.tree` settings plugin for consumer-owned compact trees and version registries. |

`repo/project-config` contains two reusable plugin modules:

| Path                                 | Gradle module    | Purpose                                                                                                                                                                                  |
|--------------------------------------|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `repo/project-config/plugin/`        | `:plugin`        | `com.marmatsan.projectConfig.settings` captures a consumer-owned inline tree and materializes Gradle catalogs; `com.marmatsan.projectConfig` anchors matching project-phase composition. |
| `repo/project-config/figma-adapter/` | `:figma-adapter` | Optional `com.marmatsan.projectConfig.figma` adapter that maps the consumer catalog to Figma-owned models and supplies a serialized task input.                                          |

The `samples/health-consumer` fixture resolves published plugin markers and
runtime artifacts from staged Maven repositories. It deliberately has no
source `includeBuild` or `health-project-config` build. It applies the optional
published Figma adapter; its tree and `versions.properties` belong to the
consumer.

Repository tooling consumes stable Maven/plugin coordinates. The included-build
root does not publish a compatibility artifact; its standalone consumer proves
that no source include is required.

`repo/verification-platform` separates provider-neutral verification policy from
infrastructure and Gradle composition:

| Path                                 | Gradle module | Purpose                                                                                                       |
|--------------------------------------|---------------|---------------------------------------------------------------------------------------------------------------|
| `repo/verification-platform/domain/` | `:domain`     | Provider-neutral plans, topology, module-impact rules, ports, and services.                                   |
| `repo/verification-platform/data/`   | `:data`       | Git, Gradle-model, JSON, TeamCity REST, parameter, and service-message adapters. Depends on `:domain`.        |
| `repo/verification-platform/plugin/` | `:plugin`     | Gradle tasks and the `com.marmatsan.verificationPlatform` composition root. Depends on `:domain` and `:data`. |

The included-build root keeps `:verification-platform:check` as an aggregate contract while the
implementation dependency direction remains `plugin -> data -> domain`.

`repo/figma-documentation-sync` separates its portable engine from this repository's
configuration:

| Path                                                 | Gradle module          | Purpose                                                                                                                                                                                                   |
|------------------------------------------------------|------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `repo/figma-documentation-sync/domain/`              | `:domain`              | Portable design-model types and ports.                                                                                                                                                                    |
| `repo/figma-documentation-sync/data/`                | `:data`                | Portable filesystem, Gradle, Figma-owned catalog port, CI, official MCP SDK, allow-listed PNG upload, runner-generation, and checkpoint adapters. It does not depend on Dependency Catalog in production. |
| `repo/figma-documentation-sync/plugin/`              | `:plugin`              | Reusable Gradle tasks, model generation, checks, and composition.                                                                                                                                         |
| `repo/figma-documentation-sync/teamcity-adapter/`    | `:teamcity-adapter`    | Optional Kotlin translation from generated TeamCity YAML/XML to the portable CI model, plus typed TeamCity CLI access for artifacts and runs.                                                             |
| `repo/figma-documentation-sync/teamcity-operations/` | `:teamcity-operations` | Optional Gradle plugin for canonical artifact handoff, verified Figma PNG upload, Cloudflare credentials, and idempotent TeamCity reruns.                                                                 |
| `repo/figma-documentation-sync/tools/`               | not a Gradle module    | TypeScript writer evaluated inside the Figma Plugin API runtime, plus preview tooling selected through the active project configuration.                                                                  |

The root build applies the reusable project-config Figma adapter and owns Water
My Plants identities in its root configuration. Another repository configures
the same adapter without changing `domain`, `data`, `plugin`, or the writer
implementation. It reuses
`teamcity-adapter` only if its CI provider is TeamCity and applies
`teamcity-operations` only when it exposes supervised TeamCity/Figma operations.

Root `settings.gradle.kts` owns the Water My Plants library and plugin trees;
root `versions.properties` owns their versions. Root `build.gradle.kts` owns
Figma identities, TeamCity selections, and writer configuration. Reusable
included builds contain no product name or concrete repository path.
`checkModuleBoundaries` enforces these scopes.

Documentation coverage treats changes to included-build settings and module
`build.gradle.kts` files as potential structure changes without interpreting
their diff. This includes formatting-only edits. Review this inventory whenever
those paths change; if the dependency graph is unchanged, refresh the review
date and keep the existing topology instead of inventing an architectural
change or weakening the coverage rule.

All product and repository-infrastructure Kotlin sources follow the shared
[Kotlin standard](../standards/kotlin.md). The repository-wide KtLint adoption
changed source layout but did not change the module inventory or dependency
directions recorded above.

## Documentation

| Path                       | Purpose                                                                              |
|----------------------------|--------------------------------------------------------------------------------------|
| `README.md`                | Repository entry point and links to deeper documentation.                            |
| `docs/documentation.md`    | Canonical documentation taxonomy and validation contract.                            |
| `docs/standards/`          | Project-wide engineering rules for production and tooling.                           |
| `docs/guides/`             | Supported development workflows.                                                     |
| `docs/reference/`          | Exact project contracts and inventories.                                             |
| `docs/decisions/`          | Architecture Decision Records.                                                       |
| `docs/runbooks/`           | Project-wide operational execution and recovery procedures.                          |
| `docs/templates/`          | Starting points for typed documentation.                                             |
| `specs/`                   | Active specification packages that are removed after durable knowledge is promoted.  |
| `.agents/`                 | Thin reviewer profiles and reusable skills that link canonical repository documents. |
| `docs/ci/`                 | CI and branch protection documentation.                                              |
| `docs/uml/`                | Project-wide PlantUML diagrams and shared UML includes.                              |
| `<module>/docs/README.md`  | Module documentation index and orientation.                                          |
| `<module>/docs/standards/` | Rules owned only by that module.                                                     |
| `<module>/docs/guides/`    | Module-specific development workflows.                                               |
| `<module>/docs/reference/` | Module-specific contracts and inventories.                                           |
| `<module>/docs/runbooks/`  | Module-owned operational runbooks.                                                   |
| `<module>/docs/uml/`       | Module-owned PlantUML diagrams, UML publication notes, and UML helper scripts.       |

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
- generated Figma sync JavaScript under `repo/figma-documentation-sync/tools/`

If a generated artifact is required for review, document how to regenerate it
instead of treating the generated file as the source of truth. Delete it after
its final consumer finishes; `./gradlew cleanTemporaryArtifacts` removes the
repository `tmp/` tree and generated Figma tooling distribution.
