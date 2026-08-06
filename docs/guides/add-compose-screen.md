---
title: Add a Compose screen
type: guide
scope: product-ui
owner: android-ui
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - docs/standards/product-design.md
  - docs/standards/compose.md
  - docs/standards/accessibility.md
  - core/ui
---

# Add A Compose Screen

## Outcome

Create a state-driven, accessible screen that uses the shared Water My Plants
design system.

## Applicable Standards

Start from the approved contract produced by
[design a product feature](design-product-feature.md). Apply the
[product design](../standards/product-design.md),
[Compose](../standards/compose.md),
[accessibility](../standards/accessibility.md),
[Android](../standards/android.md), and [testing](../standards/testing.md)
standards.

## Steps

1. Verify that the OOUX objects, complete user-action consequences, wireframe,
   visual design, and representative BDD examples are approved in Figma. Do
   not invent a user-visible state or effect during Compose implementation.
2. Define immutable UI state and action callbacks from that contract before
   composing the full screen. Keep editable form values separate from valid
   domain entities.
3. Choose the smallest presentation contract. Use state plus callbacks for a
   simple screen; introduce `UiState`, `UiAction`, and `UiEffect` when multiple
   transitions or one-off effects make MVI useful.
4. Keep the route or state owner separate from stateless screen content. The
   route collects state with lifecycle awareness, executes effects, and returns
   external results as actions.
5. Keep effect emission private to the state owner. Store restorable user state
   in `UiState` or an explicit saved-state contract instead of relying on a
   transient effect stream.
6. Add a pure reducer only when transition complexity makes it independently
   valuable and testable. Keep straightforward transitions concrete rather
   than inheriting from a shared MVI base class.
7. Build reusable components with state and callbacks supplied by the caller.
   Reuse the approved Figma component and verify its stable Code Connect
   mapping; add or change a component binding in the same change when needed.
8. Use `:core:ui` theme tokens for typography, color, shape, spacing, density,
   and elevation.
9. Add semantics and localized descriptions at the component that owns each
   interaction.
10. Add previews for loading, content, empty, and error states that actually
   exist.
11. Test state transitions, effect delivery, important rendering, user actions,
    restoration behavior, and accessibility semantics.
12. Reconcile any changed object, action, outcome, or side effect back into the
    Figma OOUX, wireframe, visual design, and repository BDD contract.
13. Verify large text, supported themes, focus order, and touch targets.

## Verification

Run focused state-owner and UI tests and `./gradlew check`. Inspect previews and
the running screen for clipping, overlapping content, missing semantics, raw
design values, duplicated effects, and state lost after recreation.

## Related Documentation

- `docs/guides/design-product-feature.md`
- `docs/reference/product-design-workspace.md`
- `core/ui/docs/README.md`
- `onboarding/ui/docs/README.md`
