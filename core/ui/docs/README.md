# Core UI Module

## Purpose

`:core:ui` owns the shared Water My Plants design system: theme, typography,
colors, shapes, density, spacing, and elevation used across product UI.

## Boundaries

This module must remain feature-agnostic. Feature screens, navigation flows,
networking, persistence, and product-specific state do not belong here.

## Dependencies

`:core:ui` uses the shared Android and Compose convention plugins and has no
project-module dependency.

## Shared Standards

- [Architecture](../../../docs/standards/architecture.md)
- [Compose](../../../docs/standards/compose.md)
- [Accessibility](../../../docs/standards/accessibility.md)

## Verification

```powershell
.\gradlew.bat :core:ui:check
```

## Module Documentation

Add module-specific references or UML below this single `docs/` directory. Keep
shared UI policy in the project standards linked above.
