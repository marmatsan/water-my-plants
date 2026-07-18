# Onboarding UI Module

## Purpose

`:onboarding:ui` owns onboarding screen composition, illustration components,
and Figma-linked onboarding assets.

## Boundaries

The module must not depend on `:app` or another feature. Reusable design-system
behavior moves to `:core:ui`; onboarding-specific UI remains here.

## Dependencies

- `:core:ui` supplies shared theme and design tokens.

## Shared Standards

- [Architecture](../../../docs/standards/architecture.md)
- [Compose](../../../docs/standards/compose.md)
- [Accessibility](../../../docs/standards/accessibility.md)
- [Testing](../../../docs/standards/testing.md)

## Verification

```powershell
.\gradlew.bat :onboarding:ui:check
```

## Module Documentation

Figma binding sources live under `src/main/kotlin/com/marmatsan/onboarding/ui/figma/`.
Document future onboarding-specific guides or references under this directory.
