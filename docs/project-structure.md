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
| `repo/dependency-catalog/` | `dependency-catalog` | Shared dependency model, version keys, library/plugin trees, and Gradle dependency DSL helpers. |
| `repo/gradle-plugins/` | `gradle-plugins` | Convention plugins used by app modules and other repository builds. |
| `repo/figma-design-sync/` | `figma-design-sync` | CI-oriented Gradle plugin and TypeScript tooling that generate and sync the Figma design model. |

The root build includes `repo/gradle-plugins` and `repo/figma-design-sync`
through `pluginManagement.includeBuild(...)`. Both included builds consume
`repo/dependency-catalog`.

## Documentation

| Path | Purpose |
|------|---------|
| `README.md` | Repository entry point and links to deeper documentation. |
| `docs/ci/` | CI and branch protection documentation. |
| `docs/uml/` | Project-wide PlantUML diagrams and shared UML includes. |
| `<module>/docs/` | Module-owned documentation, such as BDD notes, Dokka notes, UML diagrams, and tool runbooks. |

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
