---
title: Accessibility standard
type: standard
scope: product-ui
owner: android-ui
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - core/ui
  - onboarding/ui
  - docs/standards/compose.md
---

# Accessibility Standard

## Semantics

- Interactive elements MUST expose an accessible role, state, action, and name.
- Meaningful images MUST have a localized description. Decorative images MUST
  be excluded from accessibility semantics.
- A composed control SHOULD expose one coherent semantic node rather than
  duplicate labels from decorative children.
- Validation errors and asynchronous state changes MUST be perceivable without
  relying only on color.

## Interaction

- Touch targets MUST be large enough for reliable interaction and must not
  overlap neighboring actions.
- Every action MUST be usable without gesture-only knowledge when an equivalent
  standard action is possible.
- Focus order MUST follow reading and task order. Dialogs and temporary surfaces
  must manage focus predictably.

## Text And Visuals

- Layout MUST tolerate user font scaling without clipping, overlap, or loss of
  actions.
- Text and meaningful icons MUST maintain readable contrast against their
  actual background in supported themes.
- Information MUST NOT depend on color alone.
- Motion SHOULD respect reduced-motion expectations and MUST NOT block task
  completion.

## Verification

Review changed screens with semantics inspection, increased font size, light
and dark themes, and keyboard or accessibility focus where applicable. Add UI
tests for critical semantics and state descriptions.

## Sources

- `core/ui/`
- `onboarding/ui/`
- `docs/standards/compose.md`
