---
title: Product design standard
type: standard
scope: product-design
owner: product-design
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - docs/decisions/adr-0017-use-ooux-and-bdd-before-product-implementation.md
  - docs/reference/product-design-workspace.md
  - docs/standards/testing.md
  - onboarding/ui/src/main/kotlin/com/marmatsan/onboarding/ui/figma
---

# Product Design Standard

## Purpose

Govern the design contract for every user-visible Water My Plants capability
from OOUX discovery through wireframe, visual design, BDD, Code Connect, and
implementation handoff.

## Required Outcomes

- A product change MUST identify the user-visible objects before defining its
  screen structure.
- Each object MUST document its purpose, identity, core content, metadata,
  relationships, and available calls to action.
- Every user action MUST define its trigger, target, preconditions, input,
  successful state transition, expected failures and recovery, user feedback,
  accessibility behavior, and relevant external effects.
- Representative action outcomes MUST be written as BDD examples during
  wireframing. Stable approved examples MUST become repository-owned
  executable `.feature` scenarios when implementation begins.
- The wireframe and visual design MUST use the shared Figma workspace and link
  the object and action contract they realize.
- Reusable visual elements MUST use an existing component or introduce a
  deliberately owned component. A Compose-bound component MUST have a stable
  top-level Figma node and a checked-in `.figma.kt` Code Connect mapping.
- Design intent and shipped behavior MUST be distinguishable. Figma owns the
  approved design target; code, tests, schemas, and versioned configuration own
  current implemented behavior.

## Preferred Patterns

Model one object card per user-recognizable object. Keep nouns in core content,
metadata, and relationships; keep verbs in calls to action. Model a screen as a
projection of the objects and actions needed for one user outcome.

Use a compact action contract with these fields:

| Field | Required meaning |
|-------|------------------|
| Action | Stable domain-oriented verb or CTA name. |
| Trigger and target | What the user does and which object receives it. |
| Preconditions | State, permission, or prerequisite required before the action. |
| Input | User-provided or contextual values consumed by the action. |
| Success | Observable state transition and next available actions. |
| Failure and recovery | Expected failure, feedback, retained state, and retry or alternative path. |
| External effects | Persistence, network, notification, navigation, or permission effects. |
| Accessibility | Semantics, focus, announcement, and non-visual feedback required. |
| BDD example | Representative Given/When/Then outcome using the same vocabulary. |

Keep Gherkin representative. Put exhaustive validation and adapter edge cases
in the narrowest deterministic tests. Keep Figma component properties and
checked-in Code Connect properties named consistently so design variants map
to source without interpretation.

## Decision Rules

- When an action changes persistent or remote state, document the optimistic
  or blocking behavior, failure feedback, retry policy, and resulting object
  state before visual design is approved.
- When an action requires permission or leaves the application, document the
  denied, cancelled, resumed, and unavailable outcomes.
- When an object or action is shared by multiple screens, define it once and
  reuse the same vocabulary and component contract.
- When a Figma design and implemented behavior diverge, stop the affected
  change, identify which state is intended, and update the lower-precedence or
  future-intent artifact in the same coherent change.
- When several presentation architectures could implement the design, apply
  the Compose and architecture standards after the OOUX and BDD contract is
  stable; do not encode implementation mechanics in Figma scenarios.

## Disallowed Alternatives

| Do not use | Use instead | Reason |
|------------|-------------|--------|
| A finished screen as the first behavior specification | An OOUX object and action contract followed by a wireframe | Layout alone does not define behavior. |
| A CTA with only a label and destination | The complete action contract including failure, recovery, and side effects | Prevents hidden product decisions during implementation. |
| Figma-only BDD after implementation starts | Approved examples mirrored to repository `.feature` files | Makes stable business behavior executable. |
| A detached local visual or duplicated component | The shared Figma component and stable Code Connect mapping | Preserves one design-to-code contract. |
| MVI events or technical callbacks as OOUX actions | User-domain verbs translated later into UI actions | Keeps product language independent of implementation. |

## Exceptions

Pure repository tooling and non-user-visible maintenance do not require the
product-design flow. A user-visible emergency correction may use an abbreviated
wireframe only when the active specification or pull request records why; its
actions, outcomes, BDD contract, and final Figma design remain required before
the change is complete.

## Verification

- Review the OOUX object cards and action table before approving the wireframe.
- Inspect the wireframe and visual design at supported screen sizes and states.
- Verify reused and new components against their stable Figma nodes and
  checked-in `.figma.kt` mappings.
- Run the owning BDD task after `.feature` changes and the focused state,
  adapter, UI, and accessibility tests required by the implementation.
- Run `./gradlew checkDocumentation` for contract documentation changes and
  `./gradlew check` before completion.

## Sources

- [ADR-0017](../decisions/adr-0017-use-ooux-and-bdd-before-product-implementation.md)
- [Product design workspace](../reference/product-design-workspace.md)
- [Design a product feature](../guides/design-product-feature.md)
- [Testing standard](testing.md)
- [Compose standard](compose.md)
