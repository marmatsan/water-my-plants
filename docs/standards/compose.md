---
title: Jetpack Compose standard
type: standard
scope: product-ui
owner: android-ui
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - core/ui
  - onboarding/ui
  - repo/gradle-plugins/compose
---

# Jetpack Compose Standard

## Component APIs

- Screen composables SHOULD expose immutable UI state and event callbacks.
- Reusable visual composables MUST be stateless where practical. State owners
  pass data down and events up.
- `Modifier` SHOULD be the first optional parameter and MUST be applied to the
  component's outermost relevant layout.
- Composables MUST NOT perform network, database, or dependency-container
  lookups during composition.

## Design System

- Product UI MUST use tokens and theme values from `:core:ui` for shared color,
  typography, shape, spacing, density, and elevation decisions.
- Raw geometry is allowed for generated vector artwork and illustration
  internals, but not as a substitute for layout or design-system tokens.
- Feature-specific tokens may remain local until at least two consumers justify
  promotion to `:core:ui`.

## State And Effects

- State used by composition MUST be stable and have one clear owner.
- Side effects MUST use the appropriate Compose effect API and stable keys.
- Derived values SHOULD use `derivedStateOf` only when recomputation has a
  measurable cost or changes observation behavior.
- Collections rendered with lazy layouts MUST use stable keys when item identity
  survives reordering.

## Preview And Inspection

Reusable UI and screens SHOULD include focused previews for meaningful states.
Preview-only data stays outside production behavior. Figma-generated asset
bindings may live in dedicated `figma` packages and must not own screen state.

## Accessibility

Every composable MUST follow [the accessibility standard](accessibility.md).
Semantics belong with the component that owns the interaction.

## Verification

Run focused unit or UI tests, inspect previews for changed states, and run
`./gradlew check` before merge.

## Sources

- `core/ui/`
- `onboarding/ui/`
- `repo/gradle-plugins/compose/`
