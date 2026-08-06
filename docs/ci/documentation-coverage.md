# CI Documentation Coverage

`.teamcity/documentation-coverage.json` is the versioned documentation contract
for changes that can alter CI, Figma, the documentation system, or the product
module graph.

`checkDocumentation` is the Gradle-owned verification entry point. Its Kotlin
domain service checks the typed documentation contract from
`docs/documentation.md`: canonical placement, frontmatter, review dates,
canonical sources, runbook and ADR sections, and local Markdown links. Expired
review dates warn; structural or link errors fail the task.

The task consumes `build/reports/ci/ci-plan.json`, which resolves the committed
revision range against `origin/main`, and evaluates every affected coverage
rule. Filesystem traversal and manifest parsing live in `repo/verification-platform/data`; Gradle
composition lives in `repo/verification-platform/plugin`. No PowerShell runtime is required for
documentation validation. Figma scope is classified separately by the portable
`classifyFigmaChangeImpact` Gradle task and
`config/figma/change-impact-policy.json`.

CI validation scripts and the coverage manifest are `model-neutral`: their
normal CI build still runs Gradle, but post-merge Figma Sync publishes only the
scope artifact because those files cannot change the generated design model or
compiled visual writer. This is distinct from `transport-only`, which is
reserved for MCP runner transport changes.

For each affected rule, the validator requires at least one changed file
matching that rule's `documentationPaths`. The failure is part of the same
Gradle verification contract used locally and by CI.

The contract is deliberately conservative:

- An unknown path is never documentation-only.
- Coverage matching is path-based, not semantic. A formatting-only change to a
  covered source such as a module `build.gradle.kts` still requires reviewing
  and updating one accepted documentation path; do not bypass the rule because
  runtime behavior is unchanged.
- If `origin/main` is unavailable, plan generation fails instead of guessing a
  diff.
- Updating unrelated Markdown does not satisfy a rule.
- Figma-only component edits cannot be inferred from Git. Update the matching
  runbook in the same pull request when a component contract changes.
- PlantUML publication remains governed by `AGENTS.md`: a changed `.puml` must
  be rendered and published to Figma before its merge is complete.

A repository-wide mechanical source migration can match several coverage
areas even when it preserves runtime behavior. In that case, review and update
one canonical document for every matched area. Each update records whether the
area's executable contract changed and links to the standard that caused the
migration. This evidence is required before committing or publishing the
branch; an earlier `checkDocumentation` run against an uncommitted working tree
does not prove committed-diff coverage.

The 2026-07-29 repository migration disabled trailing commas according to the
[Kotlin standard](../standards/kotlin.md). The corresponding review of
`FileSystemDocumentationSource` and `DocumentationCoverageJson` found no change
to traversal, manifest parsing, JSON projection, or failure behavior; only
their Kotlin formatting changed. This note is the committed coverage evidence
for that documentation-system review.

When adding a documentation coverage area, add a narrow rule to the manifest
and focused Kotlin cases under `repo/verification-platform/domain` and `repo/verification-platform/data`. When adding
a Figma-relevant source area, update
`config/figma/change-impact-policy.json`
and the Kotlin classifier
tests.

## CI Execution

`WaterMyPlantsCi` generates the plan and passes its allow-listed
`ci.plan.gradleTasks` value to one Gradle invocation. A documentation-only
revision selects `checkDocumentation` and `checkRepositoryDiff`, then publishes
the same required `TeamCity CI` status without running the full Gradle `check`
lifecycle. All other revisions retain documentation validation and their
targeted module checks or the fail-closed root `check`.

After a successful `main` CI run, `Figma Sync` reads the same classification. A
documentation-only revision publishes a scope artifact and exits successfully
without generating a design model or checking Figma metadata. The pipeline still
runs, so the CI-to-Figma ordering and visible post-merge status remain stable.

## Cache And Queue Policy

Gradle configuration cache and build cache are enabled in `gradle.properties`.
The current TeamCity Pipeline DSL dependency does not expose the typed Build
Cache feature, so CI relies on the Windows agent's local Gradle cache rather
than a manually authored TeamCity cache feature.

TeamCity's VCS-trigger queue optimization must remain enabled. It coalesces
queued work when a newer revision exists. Running jobs are not automatically
cancelled: the checked-in Pipeline DSL has no safe typed control for that, and a
custom REST cancellation race would be less reliable than allowing the current
job to finish.
