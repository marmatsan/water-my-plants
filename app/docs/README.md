# App Module

## Purpose

`:app` is the Android application and product composition boundary. It owns the
manifest, application entry points, top-level dependency assembly, and the app
BDD suite.

## Boundaries

Reusable feature or UI behavior does not belong in `:app`. New production
features should live behind feature or core module APIs and be assembled here.
The current greeting classes are scaffold behavior, not a template for future
layer dependencies.

## Dependencies

- `:core:ui` supplies the shared design system.

Feature modules may be added as composition dependencies when their product
flow is integrated.

## Shared Standards

- [Architecture](../../docs/standards/architecture.md)
- [Android](../../docs/standards/android.md)
- [Compose](../../docs/standards/compose.md)
- [Testing](../../docs/standards/testing.md)

## Verification

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug
```

## Module Documentation

BDD feature files live under `src/test/resources/features/`. Project-wide
development guides live under `docs/guides/`.
