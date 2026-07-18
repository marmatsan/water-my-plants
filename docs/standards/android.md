---
title: Android standard
type: standard
scope: product-modules
owner: android
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - repo/gradle-plugins/android
  - repo/gradle-plugins/dependencies
  - settings.gradle.kts
---

# Android Standard

## Build Configuration

- Android modules MUST apply the repository convention plugins instead of
  duplicating SDK, Java, Kotlin, coroutine, or test configuration.
- Dependency versions MUST come from the generated catalogs. Module build
  files MUST NOT hardcode dependency versions.
- Dependencies required by every consumer of a convention plugin belong in
  that plugin. Feature-only dependencies remain in the feature module.
- Android library modules MUST declare a namespace matching their stable
  package boundary.

## Lifecycle And State

- Android framework objects MUST remain at Android boundaries and MUST NOT be
  stored in domain state.
- Long-running work MUST be lifecycle-aware or owned by an application-scoped
  component with an explicit reason.
- Saved state and process restoration must be considered for user-visible
  state that cannot be reconstructed cheaply.

## Dependency Injection

Constructor injection is the default. A module composition root may bind
implementations. Activities, composables, repositories, and use cases MUST NOT
look up dependencies through global mutable state.

## Resources

- User-visible text MUST use Android resources unless it is server-provided
  content.
- Reusable colors, typography, spacing, and shapes belong to the design system.
- Secrets and environment-specific endpoints MUST NOT be committed as Android
  resources or build constants.

## Verification

Run the affected module's unit tests and `./gradlew check`. Changes to shared
Android convention plugins require their focused included-build checks.

## Sources

- `repo/gradle-plugins/android/`
- `repo/gradle-plugins/dependencies/`
- `settings.gradle.kts`
