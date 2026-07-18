---
title: Add a Compose screen
type: guide
scope: product-ui
owner: android-ui
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - docs/standards/compose.md
  - docs/standards/accessibility.md
  - core/ui
---

# Add A Compose Screen

## Outcome

Create a state-driven, accessible screen that uses the shared Water My Plants
design system.

## Applicable Standards

Apply the [Compose](../standards/compose.md),
[accessibility](../standards/accessibility.md),
[Android](../standards/android.md), and [testing](../standards/testing.md)
standards.

## Steps

1. Define immutable UI state and user events before composing the full screen.
2. Keep the route or state owner separate from the stateless screen content.
3. Build reusable components with state and callbacks supplied by the caller.
4. Use `:core:ui` theme tokens for typography, color, shape, spacing, density,
   and elevation.
5. Add semantics and localized descriptions at the component that owns each
   interaction.
6. Add previews for loading, content, empty, and error states that actually
   exist.
7. Test important state rendering, events, and accessibility semantics.
8. Verify large text, supported themes, focus order, and touch targets.

## Verification

Run focused tests and `./gradlew check`. Inspect previews and the running screen
for clipping, overlapping content, missing semantics, and raw design values.

## Related Documentation

- `core/ui/docs/README.md`
- `onboarding/ui/docs/README.md`
