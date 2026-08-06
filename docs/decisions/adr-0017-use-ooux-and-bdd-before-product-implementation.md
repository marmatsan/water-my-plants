---
title: Use OOUX and BDD before product implementation
type: adr
scope: product-design
owner: product-design
status: accepted
last-reviewed: 2026-08-06
review-cycle-days: 365
sources:
  - docs/standards/product-design.md
  - docs/standards/testing.md
  - docs/reference/product-design-workspace.md
  - docs/guides/design-product-feature.md
---

# ADR-0017: Use OOUX And BDD Before Product Implementation

## Context

Water My Plants needs product behavior to remain traceable from design intent
to executable code. Starting with a screen layout can hide the objects a user
is manipulating, the actions available on those objects, and the state changes,
failures, permissions, or external effects caused by each action.

The product already uses a shared Figma file for wireframes, visual designs,
components, and Code Connect bindings. The design process needs a durable rule
that makes this workspace part of feature definition without allowing an
unimplemented design to be mistaken for current application behavior.

## Decision

- A user-visible product change starts with Object-Oriented UX (OOUX), not with
  production code or a finished screen. The design identifies the user-visible
  objects, their core content, metadata, relationships, and available actions.
- Every user action is documented before implementation with its trigger,
  target object, preconditions, inputs, successful state transition, expected
  failures and recovery, user feedback, accessibility behavior, and relevant
  persistence, network, permission, or other external effects.
- Representative outcomes are expressed as BDD examples beside the OOUX
  contract. When the behavior is approved for implementation, the stable
  examples are mirrored into repository-owned `.feature` files before or with
  the first production change.
- The required delivery sequence is OOUX action contract, wireframe, visual
  design, reusable component and Code Connect decision, executable BDD
  contract, and implementation. A stage may iterate with an earlier stage, but
  implementation does not begin while its action outcomes remain undefined.
- Figma owns approved pre-implementation product-design intent. Executable
  code, tests, schemas, and versioned configuration continue to own current
  implemented behavior. A conflict is reconciled explicitly; neither artifact
  silently overrides the other.
- Screens are projections of the objects and actions in the approved contract.
  MVI, ports, persistence, and transport structures are selected later to
  implement that contract and do not redefine it.

## Consequences

- Product decisions are reviewable before implementation and can be traced
  through wireframes, designs, Code Connect components, Gherkin, and tests.
- Failure, recovery, side-effect, and accessibility behavior becomes part of
  design instead of being discovered incidentally during coding.
- Designers and engineers incur more up-front modeling and must keep Figma and
  executable living documentation aligned while a feature changes.
- Figma can describe desired behavior that is not yet shipped, so every such
  contract must clearly distinguish design status from current behavior.
- Code Connect remains a component binding mechanism; it does not replace the
  OOUX action contract, BDD examples, or implementation tests.

## Alternatives

- UI-first implementation was rejected because a visually complete screen can
  still leave action semantics, state transitions, and failure behavior
  undefined.
- Writing Gherkin only after implementation was rejected because it records
  the implemented answer without helping design the behavior.
- Treating Figma as the source of current runtime behavior was rejected because
  designs can intentionally lead implementation and are not executable.
- Adopting a generic MVI framework as the product model was rejected because a
  presentation mechanism cannot replace the user-facing object and action
  model.

## Supersession

None.
