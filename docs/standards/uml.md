---
title: UML documentation standard
type: standard
scope: repository
owner: engineering
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - docs/uml/includes/theme.puml
  - docs/uml/includes/stereotypes.puml
  - docs/uml/diagrams/module-implementation-dependencies.puml
  - repo/figma-documentation-sync/docs/uml/figma-import.md
  - repo/figma-documentation-sync/docs/reference/visual-sync-contract.md
---

# UML Documentation Standard

## Purpose

Define the reviewed PlantUML source layout and the Figma publication contract
for project-wide and module-owned UML documentation.

## Source And Layout

- Use PlantUML for reviewed UML source.
- Keep module diagrams in `<module>/docs/uml/diagrams/` and project-wide
  diagrams in `docs/uml/diagrams/`.
- Keep shared themes and stereotypes in `docs/uml/includes/`; every module
  diagram includes them unless it documents a reason not to.
- Keep one top-level module `docs/` directory and group BDD, UML diagrams,
  helpers, guides, references, and runbooks beneath it without nested
  documentation roots.
- Name diagrams in kebab-case by purpose. Use stable scopes: `architecture` for
  high-level boundaries, `domain-model` for domain data, `ports-and-adapters`
  for Clean Architecture boundaries, and `*-flow` for sequence or activity
  behavior.
- Prefer one focused diagram per file and use source package and class names
  when documenting implementation structure.
- Default architecture diagrams to a left-to-right layout unless the modeled
  relationship communicates more clearly in another direction.

## Derived Documentation

- Keep `.puml` as the reviewed source of truth and render SVG as a temporary
  Figma publication artifact.
- Render temporary SVG and sanitized import files under
  `tmp/uml/<module-or-scope>/`; do not render generated SVG beside its source.
- A UML diagram derived from BDD keeps the `.feature` file as executable source
  of truth and changes with that feature.
- Create a feature diagram only when it adds communication value beyond the
  executable scenario.

## Figma Publication

- Publish generated SVG directly with Figma SVG import. Use PNG only for
  diagnosis, never as the final UML documentation node.
- Group related diagrams in a parent Figma section named for the module or
  project concern.
- The parent section contains one relevant `.Header`, binds its fill to
  `md/sys/color/surface`, uses corner radius `28`, and contains only its header
  and related diagram sections.
- Populate `.Header` properties with the scope name, a concise definition, and
  links to the documented source on the GitHub `main` branch when possible.
- Name each child diagram section after the complete `.puml` filename. It
  contains only the imported SVG `Group` and uses
  `md/sys/color/outline`, inside stroke alignment, and stroke weight `2`.
- Lock every parent or child section created or modified during publication
  after its SVG and metadata have been verified.
- Delete temporary rendered SVGs after successful publication when no later
  consumer requires them.

The UML documentation page is
`https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63308-2386`.

## Disallowed Alternatives

| Do not use                                 | Use instead                                         | Reason                                                    |
|--------------------------------------------|-----------------------------------------------------|-----------------------------------------------------------|
| Manually recreated native Figma UML shapes | The generated PlantUML SVG                          | Preserves the reviewed source and repeatable publication. |
| PNG or image-fill publication              | SVG import producing vector and text descendants    | Keeps UML inspectable and scalable.                       |
| Loose top-level diagram sections           | A named parent documentation section with `.Header` | Preserves scope and navigation.                           |
| Checked-in rendered SVG by default         | Temporary `tmp/uml/` output                         | Prevents derived artifacts from becoming source.          |
| One catch-all diagram                      | Focused diagrams with stable purpose names          | Keeps change reasons and review scope cohesive.           |

## Exceptions

An alternative layout, missing include, checked-in render, or exploratory
native Figma mockup requires a documented reason next to the owning diagram or
module documentation. Exploration never replaces the final PlantUML source and
SVG publication contract.

## Verification

- Render each changed `.puml` with PlantUML and Graphviz.
- Follow the owning module's SVG sanitization and Figma import runbook.
- Inspect imported text alignment, dashed lifelines and returns, vector/group
  structure, image fills, section ownership, source links, and locks.
- Every added or changed `.puml` reaching `main` must be published in its Figma
  UML section before the documentation state is considered complete.

## Sources

- `docs/uml/includes/theme.puml`
- `docs/uml/includes/stereotypes.puml`
- [Figma import runbook](../../repo/figma-documentation-sync/docs/uml/figma-import.md)
- [Visual sync contract](../../repo/figma-documentation-sync/docs/reference/visual-sync-contract.md)
