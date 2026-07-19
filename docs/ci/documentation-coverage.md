# CI Documentation Coverage

`.teamcity/documentation-coverage.json` is the versioned documentation contract
for changes that can alter CI, Figma, the documentation system, or the product
module graph.

`validate-documentation.ps1` runs before change-scope classification. It checks
the typed documentation contract from `docs/documentation.md`: canonical
placement, frontmatter, review dates, canonical sources, runbook and ADR
sections, and local Markdown links. Expired review dates warn; structural or
link errors fail CI.

The validator MUST remain compatible with Windows PowerShell 5.1 because
TeamCity invokes repository scripts through `powershell.exe`. Local validation
SHOULD execute both the focused test and the validator with that same runtime;
using `pwsh` alone is insufficient for CI compatibility.

`validate-documentation.ps1 -FailOnCoverageGap` compares the build revision
with `origin/main` and checks each affected documentation rule. Figma scope is
classified separately by the portable `classifyFigmaChangeImpact` Gradle task
and `repo/figma-documentation-sync/project-config/water-my-plants/change-impact-policy.json`.

CI validation scripts and the coverage manifest are `model-neutral`: their
normal CI build still runs Gradle, but post-merge Figma Sync publishes only the
scope artifact because those files cannot change the generated design model or
compiled visual writer. This is distinct from `transport-only`, which is
reserved for MCP runner transport changes.

For each affected rule, the validator requires at least one changed file matching
that rule's `documentationPaths`. CI fails before Gradle when the source surface
changes without its canonical documentation.

The contract is deliberately conservative:

- An unknown path is never documentation-only.
- If `origin/main` is unavailable, the script fails instead of guessing a diff.
- Updating unrelated Markdown does not satisfy a rule.
- Figma-only component edits cannot be inferred from Git. Update the matching
  runbook in the same pull request when a component contract changes.
- PlantUML publication remains governed by `AGENTS.md`: a changed `.puml` must
  be rendered and published to Figma before its merge is complete.

When adding a documentation coverage area, add a narrow rule to the manifest
and a matching case in
`.teamcity/scripts/tests/validate-documentation.tests.ps1`. When adding a
Figma-relevant source area, update
`repo/figma-documentation-sync/project-config/water-my-plants/change-impact-policy.json`
and the Kotlin classifier
tests.

## CI Execution

`WaterMyPlantsCi` executes this contract before the full Gradle check. A
documentation-only revision validates the complete documentation structure and changed whitespace,
then publishes the same required `TeamCity CI` status without running the full
Gradle `check` lifecycle. The lightweight classifier task still runs. All other
revisions run the complete Gradle `check` lifecycle.

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
