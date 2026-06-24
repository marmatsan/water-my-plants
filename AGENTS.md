# Project Agent Instructions

## Branching

- Use trunk-based development as the branching strategy.
- Treat `main` as the trunk and keep it stable, tested, and releasable.
- Create short-lived branches from `main` using `feature/<short-description>`, `fix/<short-description>`, or `chore/<short-description>`.
- Keep branches small and merge them back into `main` quickly through pull requests.
- Prefer feature flags or hidden entry points for incomplete work instead of long-running branches.
- Delete short-lived branches after they have been merged into `main`.
- Use version tags such as `v1.4.0` to mark releases.
- Create temporary `release/<version>` stabilization branches only when a release needs focused QA or last-mile fixes.
- Create `hotfix/<short-description>` branches from `main` only for urgent production fixes, then merge the fix back into `main` and tag the patch release.

## UML Documentation

- Use PlantUML for UML diagrams.
- Keep UML diagrams next to the code they document:
  - Module diagrams belong in `<module>/docs/uml/`.
  - Cross-module or project-wide diagrams belong in `docs/uml/`.
- Keep shared PlantUML includes in `docs/uml/includes/`.
- Every module diagram must include the shared theme and stereotypes unless the diagram has a documented reason not to.
- Name diagram files in kebab-case using the diagram purpose, for example `architecture.puml`, `ports-and-adapters.puml`, or `model-generation-flow.puml`.
- When a diagram is published to Figma, render the `.puml` to `.svg` and place the generated SVG in a Figma section named after the diagram file stem, for example `architecture` for `architecture.puml`.
- The generated SVG is an upload artifact for Figma sync, not the source of truth. Keep the `.puml` as the reviewed source in the repository.
- UML diagrams published to Figma must use the generated SVG directly. Do not recreate the diagram manually with native Figma UML components unless a task explicitly asks for an exploratory mockup.
- Lock every Figma section created or modified during UML publication after the SVG has been placed, so generated documentation cannot be edited accidentally in Figma.
- The UML documentation page is `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63308-2386`.
- Prefer one focused diagram per file. Do not create large catch-all diagrams that mix unrelated concerns.
- Use package names and class names from the source code when documenting implementation structure.
- Use stable architecture terms for diagram scopes:
  - `architecture` for high-level module boundaries and dependency direction.
  - `domain-model` for domain data structures.
  - `ports-and-adapters` for Clean Architecture boundaries.
  - `*-flow` for sequence or activity diagrams that describe runtime behavior.
- Generated image exports must be treated as build artifacts unless a task explicitly requires checked-in rendered diagrams.
