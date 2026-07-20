# Verification Platform Documentation

## Purpose

This directory is the documentation entry point for the provider-neutral
verification platform under `repo/verification-platform`. It connects the
executable behavior, generated Kotlin API reference, shared engineering
standards, and the module's implementation overview without duplicating their
contracts.

## Boundaries

Documentation here covers CI planning, module-impact classification, execution
topology, TeamCity adapters, and the Gradle composition root owned by this
included build. Project-wide testing and architecture rules remain under the
repository `docs/standards/` directory, and TeamCity operations remain under
`.teamcity/` and the project runbooks.

The implementation overview and current task contract live in the
[module README](../README.md).

## Dependencies

| Module | Direct project dependencies | Responsibility |
|---|---|---|
| `domain` | None | Provider-neutral models, ports, and planning services. |
| `data` | `domain` | Git, Gradle, JSON, and TeamCity adapters. |
| `plugin` | `data`, `domain` | Gradle tasks and composition of the verification platform. |

The dependency direction is inward toward `domain`; provider and build-tool
details must not leak into domain contracts.

## Shared Standards

- [Architecture standard](../../../docs/standards/architecture.md)
- [Testing, BDD, TDD, and KDoc standard](../../../docs/standards/testing.md)
- [Canonical documentation standard](../../../docs/documentation.md)

## Verification

Run the standalone included-build check, including tests and strict Dokka
validation, with:

```powershell
.\gradlew.bat -p repo/verification-platform check
```

Run the same Kotlin documentation coverage task used by TeamCity after changing
this module's build structure or documentation:

```powershell
.\gradlew.bat checkDocumentation
```

## Module Documentation

- [Executable BDD contracts](bdd/README.md)
- [Domain API documentation guide](../domain/docs/dokka/README.md)
- [Data adapter API documentation guide](../data/docs/dokka/README.md)
- [Gradle plugin API documentation guide](../plugin/docs/dokka/README.md)
- [Generated CI plan reference](../../../docs/reference/ci-verification-plan.md)
