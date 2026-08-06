---
title: Add a product feature
type: guide
scope: product-features
owner: architecture
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - docs/standards/product-design.md
  - docs/standards/architecture.md
  - docs/standards/compose.md
  - docs/standards/testing.md
  - settings.gradle.kts
---

# Add A Product Feature

## Outcome

Introduce observable product behavior with clear state ownership and without a
dependency on another feature.

## Applicable Standards

Start from the approved contract produced by
[design a product feature](design-product-feature.md). Apply the
[product design](../standards/product-design.md),
[architecture](../standards/architecture.md),
[Kotlin](../standards/kotlin.md), [testing](../standards/testing.md), and any
UI or API standards relevant to the feature.

## Steps

1. Confirm that the approved Figma contract defines the feature's OOUX objects,
   every user action and consequence, wireframe, visual design, and
   representative BDD examples. Return to design when implementation would
   invent or change user-visible behavior.
2. Mirror stable business examples into the owning `.feature` file before or
   with the first production source. Keep edge cases in focused tests.
3. Organize the change as one vertical capability. Decide whether it fits an
   existing module and introduce `ui`, `domain`, or `data` modules only for
   demonstrated durable boundaries.
4. Model valid domain behavior without Android, Compose, transport, or
   persistence types. Use a separate draft or input model when the UI can hold
   incomplete data.
5. Define a narrow, consumer-owned port for each required external capability.
   Add a focused use case when it names a business rule or coordinates ports;
   do not add a generic use-case hierarchy.
6. Implement adapters outside the domain boundary, map external models
   explicitly, and translate expected failures into capability-owned errors.
7. Bind concrete adapters in the `:app` composition root. Product behavior and
   adapter selection must not move into the application module together.
8. For UI behavior, expose immutable state and action callbacks. Translate the
   OOUX action vocabulary into presentation actions without exposing technical
   callbacks as product concepts. Use an
   explicit MVI `UiState`/`UiAction`/`UiEffect` contract only when the screen's
   transitions or one-off effects justify it.
9. Add deterministic domain tests, adapter contract tests, state-transition
   tests, and focused UI tests according to risk.
10. Reconcile implementation discoveries that affect objects, actions, states,
    failures, or side effects into the Figma and BDD contracts.
11. Update the feature or module README when its public boundary changes, and
   promote any newly demonstrated recurring rule to its owning standard.

## Verification

Run focused tests while iterating and `./gradlew check` before review. Confirm
the module graph still follows the documented dependency direction. Review
ports for consumer ownership, mappings for malformed external data, and every
new production type against all five SOLID principles.

## Related Documentation

- `docs/guides/design-product-feature.md`
- `docs/guides/add-module.md`
- `docs/guides/add-api-endpoint.md`
- `docs/guides/add-compose-screen.md`
