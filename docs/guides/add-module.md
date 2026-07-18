---
title: Add a product module
type: guide
scope: product-modules
owner: architecture
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - settings.gradle.kts
  - docs/standards/architecture.md
  - repo/gradle-plugins
---

# Add A Product Module

## Outcome

Create a focused Android module registered in the root build with an explicit
owner, dependency direction, and verification command.

## Applicable Standards

Read the [architecture](../standards/architecture.md),
[Android](../standards/android.md), and [testing](../standards/testing.md)
standards before changing the module graph.

## Steps

1. Choose a capability-based path such as `core/<capability>` or
   `<feature>/<layer>`. Do not use a generic dumping-ground name.
2. Add the module to the appropriate group in `settings.gradle.kts`.
3. Apply the existing Android, Compose, and test convention plugins needed by
   the module. Do not duplicate their shared configuration.
4. Declare a namespace and only the dependencies permitted by the architecture
   standard.
5. Add `<module>/docs/README.md` from the module README template. Record the
   module purpose, boundaries, consumers, dependencies, and focused check.
6. Add production and test source sets only when they contain real behavior.
7. Update `docs/reference/project-structure.md`.

## Verification

Run the new module's `check` task followed by `./gradlew check`. Confirm the
generated Figma model reports the intended module dependency only through the
official post-merge workflow.

## Related Documentation

- `docs/templates/module-readme.md`
- `docs/reference/project-structure.md`
- `docs/standards/architecture.md`
