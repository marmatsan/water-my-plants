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

## Commits

- Use Conventional Commits for commit messages: `<type>(<scope>): <summary>`.
- Keep commits clean and focused on one coherent change.
- Write the summary as the concrete change made, not as a vague activity such as "update files".
- Add a commit body when the reason for the change is not obvious from the diff.
- Use the commit body to explain why the change was made, what decision or tradeoff it captures, and what future maintainers should avoid undoing accidentally.
- Use this commit message structure:

```text
<type>(<scope>): <what changed>

<why it changed>

<extra context / tradeoffs / verification if useful>
```

- Prefer commit messages shaped like:

```text
test(figma-design-sync): add executable BDD coverage for design model generation

Add Cucumber scenarios for the figma design model so the expected repository
snapshot is documented as executable behavior.

The feature covers the domain generation flow and the Gradle task integration
because both are part of the contract used by the Figma sync pipeline.
```

## UML Documentation

- Use PlantUML for UML diagrams.
- Keep UML diagrams next to the code they document:
  - Module diagrams belong in `<module>/docs/uml/diagrams/`.
  - Cross-module or project-wide diagrams belong in `docs/uml/diagrams/`.
- Keep shared PlantUML includes, such as themes and stereotypes, in `docs/uml/includes/`.
- Inside a module, keep a single top-level `docs/` directory and group documentation below it by scope, for example `docs/bdd/`, `docs/uml/diagrams/`, and `docs/uml/tools/`. Do not create nested `docs/` directories inside those scope folders.
- Keep module-specific UML publication notes and helper scripts next to that module's diagrams unless they are intentionally shared across modules.
- Every module diagram must include the shared theme and stereotypes unless the diagram has a documented reason not to.
- Name diagram files in kebab-case using the diagram purpose, for example `architecture.puml`, `ports-and-adapters.puml`, or `model-generation-flow.puml`.
- When a diagram is published to Figma, render the `.puml` to `.svg` and place the generated SVG in its own Figma section named after the full diagram file name, for example `architecture.puml`.
- Render temporary PlantUML SVGs under `tmp/uml/<module>/` and keep sanitized Figma import SVGs in the same temporary tree. Do not render generated SVGs next to the `.puml` source unless a task explicitly requires it.
- Group module diagrams inside a parent section named after the module, for example `figmaDesignSync`. The parent section is for module organization; the diagram-level sections are the publication targets.
- The generated SVG is an upload artifact for Figma sync, not the source of truth. Keep the `.puml` as the reviewed source in the repository.
- UML diagrams published to Figma must use the generated SVG directly. Do not recreate the diagram manually with native Figma UML components unless a task explicitly asks for an exploratory mockup.
- Follow the module-specific Figma import runbook when one exists. Keep detailed SVG sanitization, import, and troubleshooting rules in module docs unless they are intentionally shared across modules.
- Once a generated SVG has been successfully published to Figma, delete the generated SVG from `tmp/` if it is no longer needed. Do not keep rendered SVG artifacts locally after they already live in the locked Figma section.
- UML diagrams that represent BDD features are derived documentation. Keep the `.feature` file as the executable source of truth, update the `.puml` when the feature changes, and create feature diagrams only when the visual representation adds communication value.
- Lock every Figma section created or modified during UML publication after the SVG has been placed, so generated documentation cannot be edited accidentally in Figma.
- Figma diagram sections created for UML documentation must have no fill, a stroke bound to `md/sys/color/outline`, stroke align `INSIDE`, and stroke weight `2`.
- The UML documentation page is `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63308-2386`.
- Prefer one focused diagram per file. Do not create large catch-all diagrams that mix unrelated concerns.
- Use package names and class names from the source code when documenting implementation structure.
- Use stable architecture terms for diagram scopes:
  - `architecture` for high-level module boundaries and dependency direction.
  - `domain-model` for domain data structures.
  - `ports-and-adapters` for Clean Architecture boundaries.
  - `*-flow` for sequence or activity diagrams that describe runtime behavior.
- Generated image exports must be treated as build artifacts unless a task explicitly requires checked-in rendered diagrams.
