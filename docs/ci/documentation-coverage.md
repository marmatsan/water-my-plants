# CI Documentation Coverage

`.teamcity/documentation-coverage.json` is the versioned documentation contract
for changes that can alter the CI or Figma documentation state.

`get-change-impact.ps1` compares the build revision with `origin/main`. It
classifies the change as `documentation-only` only when every changed path is an
explicitly allowed Markdown path. Every other change remains
`full-verification`.

For each affected rule, the script requires at least one changed file matching
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

When adding a new Figma-relevant source area, add a narrow rule to the manifest
and a matching test in `.teamcity/scripts/tests/get-change-impact.tests.ps1`.

## CI Execution

`WaterMyPlantsCi` executes this contract before Gradle. A documentation-only
revision validates whitespace and local Markdown links, then publishes the same
required `TeamCity CI` status without running Gradle. All other revisions run
the complete Gradle `check` lifecycle.

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
