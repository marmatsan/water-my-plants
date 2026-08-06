---
title: Design a product feature
type: guide
scope: product-design
owner: product-design
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - docs/standards/product-design.md
  - docs/standards/testing.md
  - docs/reference/product-design-workspace.md
  - specs/README.md
---

# Design A Product Feature

## Outcome

Produce an approved, traceable product contract that moves from OOUX through
wireframe and visual design to executable BDD and an implementation-ready
handoff.

## Applicable Standards

Apply the [product design](../standards/product-design.md),
[testing](../standards/testing.md), [accessibility](../standards/accessibility.md),
and [documentation](../documentation.md) standards. Apply the Compose,
architecture, API, and persistence decisions only when the resulting contract
requires those implementation boundaries.

## Steps

1. Name one observable user outcome, the affected users and objects, and the
   behaviors that are explicitly outside the change. Create or update an active
   specification when the work spans several decisions or sessions.
2. In the Figma Wireframe page, create an OOUX card for each user-recognizable
   object. Record its purpose, identity, core content, metadata, relationships,
   and available calls to action.
3. For every action, complete this contract before laying out the final
   wireframe:

   | Action      | Trigger and target      | Preconditions and input   | Success state         | Failure and recovery                           | External effects                                              | Feedback and accessibility      | BDD example             |
   |-------------|-------------------------|---------------------------|-----------------------|------------------------------------------------|---------------------------------------------------------------|---------------------------------|-------------------------|
   | Domain verb | User gesture and object | Required state and values | Observable transition | Expected failure, retained state, and recovery | Persistence, network, permission, navigation, or notification | Visible and non-visual response | Given/When/Then outcome |

4. Write representative Given/When/Then examples beside the object and action
   model. Use the same object and action vocabulary; describe behavior rather
   than composables, ViewModels, HTTP details, or database operations.
5. Build the wireframe as a projection of the approved objects, states, and
   actions. Include content, loading, empty, failure, permission, and recovery
   states that the action contracts require.
6. Review the wireframe contract with product, design, and engineering. Return
   to the OOUX model when the review discovers a missing object, action,
   consequence, or state.
7. Create the visual design on the Design page. Reuse shared assets and
   components; add a component only when it has clear ownership, variants, and
   accessibility semantics.
8. For a Compose-bound component, verify the stable top-level Figma node and
   add or update its checked-in `.figma.kt` Code Connect mapping. Treat the
   source mapping, not a page-root query, as the executable binding.
9. Mirror the stable business examples into the owning repository `.feature`
   file before or with the first production change. Add focused tests for edge
   cases that should not expand the Gherkin contract.
10. Hand off the approved contract through the relevant implementation guide:
    [add a feature](add-feature.md), [add a Compose screen](add-compose-screen.md),
    or [add an API endpoint](add-api-endpoint.md). Reconcile any implementation
    discovery that changes user-visible behavior back into OOUX, Figma, and BDD.

## Verification

- Every visible CTA maps to one documented action, and every documented action
  appears in at least one reachable state or is explicitly deferred.
- Success, expected failure, recovery, permission, cancellation, and external
  effects are present where applicable.
- Wireframe and visual design use the canonical Figma surfaces in the
  [workspace reference](../reference/product-design-workspace.md).
- Component variants and Code Connect properties agree with checked-in
  `.figma.kt` mappings.
- Approved `.feature` scenarios use the same product language and pass through
  the owning BDD task once implementation exists.
- Run `./gradlew checkDocumentation` after documentation changes and
  `./gradlew check` before completing the implementation change.

## Related Documentation

- [ADR-0017](../decisions/adr-0017-use-ooux-and-bdd-before-product-implementation.md)
- [Product design standard](../standards/product-design.md)
- [Product design workspace](../reference/product-design-workspace.md)
- [Code-generation context](../reference/code-generation-context.md)
