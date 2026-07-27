# Project Agent Instructions

## Branching

- Follow `docs/standards/git-workflow.md` as the canonical Git workflow.
- Treat `main` as the only permanent branch and keep it stable, tested, and releasable.
- Create short-lived `feature/*`, `fix/*`, or `chore/*` branches from current `main`.
- Merge through a pull request using squash after `TeamCity CI` passes and all review conversations are resolved.
- Use feature flags or hidden entry points for incomplete work, and delete short-lived branches after merge or abandonment.
- Use immutable `vX.Y.Z` tags for releases. Create `release/<x.y.z>` only for exceptional stabilization and `hotfix/*` only for urgent production fixes.

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
test(figma-documentation-sync): add executable BDD coverage for design model generation

Add Cucumber scenarios for the figma design model so the expected repository
snapshot is documented as executable behavior.

The feature covers the domain generation flow and the Gradle task integration
because both are part of the contract used by the Figma sync pipeline.
```

## Documentation

- Treat `docs/documentation.md` as the canonical documentation standard.
- Choose the document type before creating prose: standard, guide, runbook,
  reference, ADR, or README index.
- Start typed documents from the matching template under `docs/templates/` and
  place them in the canonical project or module directory.
- Keep shared production rules under `docs/standards/`. Module documentation
  links to shared rules and records only module ownership, contracts, and
  explicit exceptions.
- Keep README files as orientation and navigation. Move detailed development
  workflows to guides and operational execution or recovery to runbooks.
- Keep Figma and rendered UML as derived publication surfaces linked to their
  versioned repository sources.
- Before completing an implementation, bootstrap, migration, or recovery,
  compare newly learned behavior, failure modes, permissions, and safety
  boundaries with the current documentation. Document reusable gaps in the
  same change using the appropriate standard, guide, runbook, or reference.
- Run `.\gradlew.bat checkDocumentation`
  after adding, moving, or editing documentation.

## Architecture and SOLID

- Follow `docs/standards/architecture.md` for module boundaries, dependency
  direction, composition roots, and the mandatory SOLID design rules.
- Apply SOLID to all code we implement or materially change, including
  production types and test support that contains reusable behavior. A change
  is not complete while a known SOLID violation remains in its implementation.
- Apply the Package Cohesion rules in `docs/standards/architecture.md` to every
  new or generated checked-in Kotlin source: use a meaningful capability
  package, mirror it in the source path, and reserve root packages for public
  entry points and composition roots.
- Give each type one cohesive reason to change. Composition roots may select
  concrete implementations but must delegate configuration, registration, and
  runtime behavior to focused collaborators.
- Depend on consumer-owned interfaces at architectural boundaries. Keep ports
  small enough that consumers implement only the operations they use.
- Before completing implementation, review the affected production code
  explicitly against SRP, OCP, LSP, ISP, and DIP. Document and obtain review
  for any necessary exception.

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
- When a diagram is published to Figma, render the `.puml` to `.svg` and place the generated SVG in a Figma diagram section named after the full diagram file name, for example `architecture.puml`.
- Render temporary PlantUML SVGs under `tmp/uml/<module>/` and keep sanitized Figma import SVGs in the same temporary tree. Do not render generated SVGs next to the `.puml` source unless a task explicitly requires it.
- Group diagrams inside a parent Figma section with a relevant scope name. Module diagrams use the module name, for example `figmaDocumentationSync`; project-wide diagrams use the project-wide concern, for example `projectModuleDependencies`.
- Each parent Figma section for UML diagrams must contain a `.Header` component that describes that parent section. Fill `Header`, `Link`, and `Definition` with relevant information, and make `Link` point to the GitHub `main` branch URL for the documented source file when possible.
- The parent Figma section is the visual/documentation container: bind its fill to `md/sys/color/surface`, set corner radius `28`, and keep only related diagram sections and its `.Header` inside it.
- The generated SVG is an upload artifact for Figma sync, not the source of truth. Keep the `.puml` as the reviewed source in the repository.
- UML diagrams published to Figma must use the generated SVG directly. Do not recreate the diagram manually with native Figma UML components unless a task explicitly asks for an exploratory mockup.
- CI for `main` must treat published UML as part of the releaseable documentation state. Every added or changed `.puml` file that reaches `main` must have its rendered SVG published in the Figma UML documentation page before the merge is considered complete.
- Follow the module-specific Figma import runbook when one exists. Keep detailed SVG sanitization, import, and troubleshooting rules in module docs unless they are intentionally shared across modules.
- Once a generated SVG has been successfully published to Figma, delete the generated SVG from `tmp/` if it is no longer needed. Do not keep rendered SVG artifacts locally after they already live in the locked Figma section.
- UML diagrams that represent BDD features are derived documentation. Keep the `.feature` file as the executable source of truth, update the `.puml` when the feature changes, and create feature diagrams only when the visual representation adds communication value.
- Lock every Figma section created or modified during UML publication after the SVG has been placed, so generated documentation cannot be edited accidentally in Figma.
- Figma diagram sections created for UML documentation must be children of the parent documentation section. They are named after the `.puml` file, contain only the imported SVG `Group`, and use a stroke bound to `md/sys/color/outline`, stroke align `INSIDE`, and stroke weight `2`.
- The UML documentation page is `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63308-2386`.
- Prefer one focused diagram per file. Do not create large catch-all diagrams that mix unrelated concerns.
- Use package names and class names from the source code when documenting implementation structure.
- Use stable architecture terms for diagram scopes:
  - `architecture` for high-level module boundaries and dependency direction.
  - `domain-model` for domain data structures.
  - `ports-and-adapters` for Clean Architecture boundaries.
  - `*-flow` for sequence or activity diagrams that describe runtime behavior.
- Generated image exports must be treated as build artifacts unless a task explicitly requires checked-in rendered diagrams.
