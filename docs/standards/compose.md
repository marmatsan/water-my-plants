---
title: Jetpack Compose standard
type: standard
scope: product-ui
owner: android-ui
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - docs/standards/product-design.md
  - docs/reference/product-design-workspace.md
  - core/ui
  - onboarding/ui
  - repo/gradle-plugins/compose
---

# Jetpack Compose Standard

## Component APIs

- Screen APIs MUST implement the approved OOUX objects, actions, states, and
  consequences from the product-design contract. Technical callbacks and MVI
  events translate that vocabulary; they do not redefine it.
- Screen composables SHOULD expose immutable UI state and event callbacks.
- Reusable visual composables MUST be stateless where practical. State owners
  pass data down and events up.
- `Modifier` SHOULD be the first optional parameter and MUST be applied to the
  component's outermost relevant layout.
- Composables MUST NOT perform network, database, or dependency-container
  lookups during composition.

## Unidirectional Presentation Flow

Compose screens MUST use one-way data flow: immutable state moves from its
owner to the screen and user or system input returns through callbacks. Keep a
route or state-owning composable separate from stateless screen content when
lifecycle collection, dependency wiring, navigation, permissions, or other
effects are required.

MVI is a supported specialization of this flow when a screen has enough state
transitions or effects to benefit from an explicit contract. It is not a
mandatory framework for simple components or screens. An MVI screen uses:

- `*UiState` for the complete immutable and renderable screen state;
- `*UiAction` for user intentions and results returned by Android or another
  external boundary;
- `*UiEffect` for one-off requests that the route executes, such as navigation,
  permission launchers, or transient messages.

The state owner MUST accept actions, expose read-only observable state, and
keep effect emission private. A route consumes effects and translates external
results back into actions; screen content MUST NOT invoke a public effect
emitter or send an effect back to its producer. State that must survive
recreation belongs in `UiState` or an explicit saved-state contract, not only
in a transient effect stream.

Closed action and effect vocabularies SHOULD use sealed hierarchies. Add a pure
reducer when independently testable transition logic is complex enough to
justify it. Otherwise keep the transition concrete in the state owner. Do not
introduce repository-wide MVI base classes or marker interfaces until multiple
implementations demonstrate a substitutable contract.

Editable form state MUST remain a presentation model. Convert it to a valid
domain command or value only at the capability boundary; do not mutate or
partially populate a domain entity to represent incomplete user input.

## Design System

- Product UI MUST use tokens and theme values from `:core:ui` for shared color,
  typography, shape, spacing, density, and elevation decisions.
- Raw geometry is allowed for generated vector artwork and illustration
  internals, but not as a substitute for layout or design-system tokens.
- Feature-specific tokens may remain local until at least two consumers justify
  promotion to `:core:ui`.

## State And Effects

- State used by composition MUST be stable and have one clear owner.
- Route-level state collection MUST be lifecycle-aware and expose only a
  read-only `Flow` or `StateFlow` to consumers.
- Side effects MUST use the appropriate Compose effect API and stable keys.
- Every transient effect contract MUST define and test its delivery semantics.
- Derived values SHOULD use `derivedStateOf` only when recomputation has a
  measurable cost or changes observation behavior.
- Collections rendered with lazy layouts MUST use stable keys when item identity
  survives reordering.

## Preview And Inspection

Reusable UI and screens SHOULD include focused previews for meaningful states.
Preview-only data stays outside production behavior. Figma-generated asset
bindings may live in dedicated `figma` packages and must not own screen state.

Compose-bound Figma components MUST use a stable top-level component or
component-set node and a checked-in `.figma.kt` mapping. Component properties,
variant names, and source parameters SHOULD share a recognizable vocabulary.
An empty page-root mapping query is not evidence that child components are
unbound; verify the component node and checked-in mapping source.

## Accessibility

Every composable MUST follow [the accessibility standard](accessibility.md).
Semantics belong with the component that owns the interaction.

## Verification

Run focused unit or UI tests, inspect previews for changed states, and run
`./gradlew check` before merge.

## Sources

- `docs/standards/product-design.md`
- `docs/reference/product-design-workspace.md`
- `core/ui/`
- `onboarding/ui/`
- `repo/gradle-plugins/compose/`
