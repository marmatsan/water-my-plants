---
title: Product design workspace
type: reference
scope: product-design
owner: product-design
status: active
last-reviewed: 2026-08-06
review-cycle-days: 90
sources:
  - https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants
  - onboarding/ui/src/main/kotlin/com/marmatsan/onboarding/ui/figma
  - docs/standards/product-design.md
---

# Product Design Workspace

## Purpose

Record the exact Figma workspace, page nodes, OOUX vocabulary, and Code Connect
relationships used to design Water My Plants product behavior.

## Contract

The canonical product-design workspace is the Figma file **Water My Plants**
with file key `YBZXsd8oyGLbcI2KWxJvRK`.

| Surface | Stable node | Purpose |
|---------|-------------|---------|
| Wireframe | [`62796:601`](https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62796-601) | OOUX object cards, action tags, BDD examples, and screen wireframes. |
| Design | [`62868:156`](https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62868-156) | Approved visual screen designs derived from the wireframes. |
| Assets | [`63010:1484`](https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63010-1484) | Reusable asset component sets such as plant, shape, dots, and leaf variants. |
| Illustration | [`62937:7521`](https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62937-7521) | Reusable illustration component variants. |
| Gradle dependencies | [`62934:908`](https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62934-908) | Published Gradle dependency documentation inside the same file; it is not the product-screen root. |

The inspected onboarding wireframe currently demonstrates this OOUX shape:

- object: onboarding step;
- core content: `steps`, `currentStepIndex`, `totalSteps`,
  `currentStepTitle`, `currentStepBody`, `currentStepIllustration`, and
  `primaryCtaLabel`;
- calls to action: `OnNext` and `OnAddFirstPlant`;
- screen projections: `OnboardingScreen1` and `OnboardingScreen2`.

These names are evidence for the current onboarding contract, not a mandatory
schema for unrelated features.

Compose Code Connect mappings for onboarding assets live in
`onboarding/ui/src/main/kotlin/com/marmatsan/onboarding/ui/figma/`. Examples
include:

| Figma component | Stable node | Checked-in mapping |
|-----------------|-------------|--------------------|
| Illustration | `62815:331` | `Illustration.figma.kt` |
| Plant asset | `62873:2534` | `AssetsPlant.figma.kt` |

The same package owns the background, dots, leaf, and shape mappings. The
checked-in annotation URL and property mappings are the executable source for
the code binding.

## Invariants

- Product wireframes precede visual screen design, and visual design precedes
  production implementation.
- Every user action in a wireframe has a complete action contract and at least
  one representative BDD outcome.
- Visual designs preserve the approved OOUX objects, actions, states, and
  consequences. A change to them returns to the wireframe contract.
- Code Connect uses stable top-level component or component-set node IDs.
  Nested instance IDs and transient selection IDs are not durable contracts.
- An empty remote mapping result for a page or section root does not prove that
  child components are unbound. Verify the component-set node and the
  checked-in `.figma.kt` source.
- Figma records approved design intent. Repository code and executable tests
  record current shipped behavior; discrepancies are resolved before the
  affected feature is complete.

## Sources

- [Water My Plants Figma file](https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants)
- [Product design standard](../standards/product-design.md)
- [`onboarding.ui.figma`](../../onboarding/ui/src/main/kotlin/com/marmatsan/onboarding/ui/figma)
