# Project Agent Instructions

## Decision Protocol

1. Read [`docs/documentation.md`](docs/documentation.md) for source precedence
   and document ownership.
2. Apply the
   [`code-generation standard`](docs/standards/code-generation.md) to every
   generated or materially changed source file.
3. Select the affected row in the
   [`code-generation context map`](docs/reference/code-generation-context.md)
   and load its standards, guide, reference, executable evidence, and focused
   checks.
4. Read any `AGENTS.md` between the repository root and the target file. Scoped
   instructions add local boundaries and cannot weaken a project standard.
5. Load the active package under [`specs/`](specs/README.md) when one governs
   the change. A specification defines desired intent, not current behavior.
6. Inspect current code, tests, schemas, and versioned configuration before
   selecting an implementation pattern.
7. Stop and record a decision when an active specification conflicts with a
   standard or accepted ADR, or when materially different architectures remain
   valid.

State and implement the supported pattern first. Pair an invalid alternative
with the replacement the repository expects or with the safety boundary that
leaves no valid replacement.

## Change Workflow

1. Confirm the observable outcome, affected consumers, scope, and non-goals.
2. Create or update an active specification when the work spans several
   implementation decisions, contracts, or sessions.
3. Start a short-lived branch from current `main` and keep one coherent change
   per pull request.
4. Drive behavior with concrete examples and the smallest deterministic test.
5. Generate the smallest implementation that preserves the documented
   dependency direction, ownership, typed failures, package cohesion, and
   public API.
6. Run focused checks while iterating and aggregate checks before completion.
7. Review every materially changed production or reusable test-support type
   against SRP, OCP, LSP, ISP, and DIP.
8. Perform the documentation-learning review and promote reusable decisions
   before closing the change.

## Code Boundaries

- Use the [architecture standard](docs/standards/architecture.md) for module
  dependencies, consumer-owned ports, composition roots, SOLID, and package
  cohesion.
- Use the [Gradle standard](docs/standards/gradle.md) for build scripts,
  convention plugins, catalogs, included builds, and task cache contracts.
- Use the [Kotlin standard](docs/standards/kotlin.md) for source layout,
  APIs, coroutines, formatting, and KDoc.
- Use the [typed-error standard](docs/standards/error-handling.md) for expected
  failures, exception boundaries, and `kotlin-result` ownership.
- Use the [testing standard](docs/standards/testing.md) for unit, adapter, BDD,
  UI, reliability, and living-documentation responsibilities.
- Use the [repository-hygiene standard](docs/standards/repository-hygiene.md)
  for temporary artifacts, generated output, and module retirement.

Checked-in Kotlin source uses a meaningful capability package that matches its
directory. Root packages are reserved for public entry points and composition
roots. An abstraction requires a demonstrated consumer boundary or variation
point; otherwise implement the focused concrete behavior.

## Git And Commits

Follow the [Git workflow standard](docs/standards/git-workflow.md) and the
[short-lived branch guide](docs/guides/work-with-short-lived-branches.md).
`main` is the only permanent branch; integrate through a squash pull request
after strict `TeamCity CI` passes and review conversations are resolved.

Use Conventional Commits: `<type>(<scope>): <summary>`. State the concrete
result in the summary. Add a body when the reason is not evident from the diff;
explain the decision or tradeoff and what maintainers should preserve.

## Documentation Decisions

Use the type that owns the value of a new decision:

| Decision | Canonical destination |
|----------|-----------------------|
| Current observable behavior | Code, test, schema, or `.feature` |
| Recurring implementation rule | Standard |
| Durable architectural choice and tradeoff | ADR |
| Exact name, path, field, inventory, or relationship | Reference |
| Supported development outcome | Guide |
| Operational execution, recovery, permissions, or safety | Runbook |
| Approved temporary change intent | Active specification |
| AI-specific reusable procedure | Thin skill linked to canonical documents |

Start typed documents from `docs/templates/`, link them from the nearest index,
and run `.\gradlew.bat checkDocumentation` after editing documentation or an
active specification.

Before completing implementation, migration, bootstrap, or recovery work,
compare verified behavior, failure modes, permissions, recovery steps, and
safety constraints with current documentation. Add a reusable gap in the same
change. Do not normalize credentials, machine-specific paths, transient
identifiers, or unsafe emergency workarounds.

## UML Documentation

Apply the [UML standard](docs/standards/uml.md) for PlantUML source layout,
naming, temporary SVG ownership, and Figma publication. Follow the owning
module's import runbook for rendering, sanitization, structural inspection,
visual inspection, and locking.

PlantUML remains the source of truth. A changed `.puml` reaching `main` must be
published as its generated SVG in the canonical Figma UML documentation page
before the documentation state is complete.

## Completion

- Run the smallest affected tests during implementation.
- Run the focused checks selected by the context map.
- Run `.\gradlew.bat checkDocumentation` for documentation or specification
  changes.
- Run `.\gradlew.bat check` before completion.
- Inspect the final diff for stale paths, copied policy, temporary artifacts,
  scaffold identities, and undocumented decisions.
- Prepare a squash pull request and require strict `TeamCity CI` before merge.
