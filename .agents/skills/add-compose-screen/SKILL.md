---
name: add-compose-screen
description: Add or materially change one Water My Plants Jetpack Compose screen with explicit state, events, design-system tokens, accessibility, previews, tests, and documentation. Use for product UI screen work under a Compose feature module; do not use for generated vector internals alone.
---

# Add Compose Screen

1. Read the root and scoped `AGENTS.md` files and the active specification.
2. Read the
   [Compose screen guide](../../../docs/guides/add-compose-screen.md), the
   [code-generation standard](../../../docs/standards/code-generation.md), and
   the Compose, accessibility, Android, and testing standards selected by the
   [context map](../../../docs/reference/code-generation-context.md).
3. Inspect the owning module README, current theme tokens, comparable
   components, Figma bindings, previews, and UI tests.
4. Define the immutable UI state and user events before composing the complete
   screen. Keep the state owner or route separate from stateless screen content.
5. Build reusable components from caller-supplied state and callbacks. Reuse
   design-system tokens and attach semantics at the interaction owner.
6. Add previews only for meaningful supported states and tests for important
   rendering, events, and accessibility behavior.
7. Inspect font scaling, supported themes, focus order, touch targets, clipping,
   and raw design values.
8. Run the owning module checks, `checkDocumentation` when contracts change,
   and root `check` before completion.
9. Review SOLID and promote reusable UI or accessibility learning without
   turning one-off geometry into a shared rule.

Return the screen outcome, state and event contract, accessibility evidence,
preview/test coverage, and verification results.
