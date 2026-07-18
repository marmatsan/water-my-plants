---
title: Add a product feature
type: guide
scope: product-features
owner: architecture
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - docs/standards/architecture.md
  - docs/standards/testing.md
  - settings.gradle.kts
---

# Add A Product Feature

## Outcome

Introduce observable product behavior with clear state ownership and without a
dependency on another feature.

## Applicable Standards

Apply the [architecture](../standards/architecture.md),
[Kotlin](../standards/kotlin.md), [testing](../standards/testing.md), and any
UI or API standards relevant to the feature.

## Steps

1. Describe the user-visible behavior and add a BDD scenario only when it adds
   executable business value.
2. Decide whether the feature fits an existing module. Create a module only
   when it introduces a durable capability or boundary.
3. Model domain behavior without Android, transport, or persistence types.
4. Define capability interfaces at the boundary that owns the need.
5. Implement adapters outside the domain boundary and bind them in a
   composition root.
6. Expose immutable UI state and events to the screen.
7. Add focused unit, adapter, and UI tests according to risk.
8. Update the feature or module README when its public boundary changes.

## Verification

Run focused tests while iterating and `./gradlew check` before review. Confirm
the module graph still follows the documented dependency direction.

## Related Documentation

- `docs/guides/add-module.md`
- `docs/guides/add-api-endpoint.md`
- `docs/guides/add-compose-screen.md`
