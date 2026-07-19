---
title: CI verification plan
type: reference
scope: repository
owner: repository-tooling
status: active
last-reviewed: 2026-07-19
review-cycle-days: 180
sources:
  - repo/ci/domain/src/main/kotlin/com/marmatsan/ci/domain/model/CiPlan.kt
  - repo/ci/domain/src/main/kotlin/com/marmatsan/ci/domain/model/CiExecutionTopology.kt
  - repo/ci/domain/src/main/kotlin/com/marmatsan/ci/domain/service/CiPlanFactory.kt
  - repo/ci/domain/src/main/kotlin/com/marmatsan/ci/domain/service/CiTopologyPlanner.kt
  - repo/ci/domain/src/main/kotlin/com/marmatsan/ci/domain/service/ModuleImpactAnalyzer.kt
---

# CI Verification Plan

## Purpose

This reference defines the provider-neutral contract that selects repository
verification work. TeamCity consumes the plan sequentially while the project
has one agent. A future multi-agent topology may execute independent units in
parallel without changing this contract.

## Contract

`generateCiPlan` writes `build/reports/ci/ci-plan.json` with:

| Field | Meaning |
|-------|---------|
| `schemaVersion` | Version of the JSON compatibility contract. |
| `mode` | `enforced` when provider execution decisions are derived from this plan. |
| `comparisonBase` | Git revision used as the start of the committed diff. |
| `head` | Exact revision being planned. |
| `scope` | Primary path classification for reporting. |
| `changedFiles` | Normalized repository-relative paths. |
| `changedModules` | Gradle modules that own changed implementation paths. |
| `affectedModules` | Changed modules plus all transitive reverse dependents. |
| `verificationUnits` | Allow-listed work units, dependencies, capabilities, Gradle tasks, and reasons. |
| `fullVerification` | Whether root `check` remains required. |
| `fallbackReason` | Fail-closed explanation when targeted classification is unsafe. |

Stable verification unit identifiers are `documentation`, `repository-diff`,
`teamcity-dsl`, `figma-tooling`, `dependency-catalog`,
`gradle-verification`, and `publish-reports`.

## Invariants

- Unknown or empty change sets require full verification.
- Plan output contains no executable shell commands.
- CI adapters map stable identifiers to versioned, reviewed commands.
- Documentation validation remains repository-wide because its coverage rules
  relate implementation paths to required documentation changes.
- Documentation-only classification is deliberately narrow. Unknown Markdown
  locations fail closed to full verification instead of being treated as docs.
- The evaluated Gradle project model is the source of truth for module
  directories and project dependency edges. Synthetic parent projects without
  build files are not executable modules.
- An empty graph, duplicate module identity or directory, unresolved edge, or
  unclassified path fails closed to the root `check` task.
- Module-only changes run `check` for the changed modules and every transitive
  consumer. They also run `checkFigmaCatalogUsage`, because removing source can
  make a dependency declaration unused even when catalog files did not change.
- The required GitHub status remains `TeamCity CI`.

## TeamCity Execution

The current single-agent adapter executes one `Verify` job with visible,
sequential steps. `prepareTeamCityCiPlan` emits allow-listed build parameters;
later steps consume only those parameters through small inline Windows command
adapters:

| Plan unit | TeamCity execution |
|-----------|--------------------|
| `documentation` | Always run the repository documentation validator. |
| `repository-diff` | Run `git diff --check` for documentation-only changes. |
| `teamcity-dsl` | Generate the TeamCity Kotlin DSL with the Maven wrapper when `.teamcity` changes. |
| `figma-tooling` | Coalesced into the heavy Gradle verification on the single agent. |
| `dependency-catalog` | Coalesced into the heavy Gradle verification on the single agent. |
| `gradle-verification` | Run affected module checks plus catalog usage for safe module-only changes; otherwise run root `check`. |
| `publish-reports` | Publish `build/reports/ci` through the job artifact contract. |

This topology keeps one checkout, one agent allocation, and one authoritative
GitHub status. Coalesced units remain explicit in the JSON so a later
multi-agent adapter can split them without changing classification policy.

## Multi-Agent Topology Preview

`generateCiTopologyPreview -PciAvailableAgents=<count>` writes
`build/reports/ci/ci-topology-preview.json`. This is a provider-neutral,
`preview-only` contract: the active TeamCity Kotlin DSL does not read it and
continues to define one `Verify` job while only one agent exists.

| Available agents | Previewed execution |
|------------------|---------------------|
| `1` | One `verify` lane runs every required unit sequentially and publishes the authoritative status. |
| `2` | `documentation` runs first; `supplemental-verification` and `gradle-verification` may then run in parallel; `ci-gate` waits for both and publishes the status. |
| `3+` | `documentation` runs first; repository, tooling, and Gradle lanes may then run in parallel; `ci-gate` waits for every required lane and publishes the status. |

The planner omits empty lanes, derives lane capabilities from their units, and
preserves every required dependency. It fails when no agent is available,
when `publish-reports` is missing, when any required unit is lost or reordered,
or when more than one lane would publish the authoritative status.

Adding agents alone does not activate this topology. Activation requires a
separate reviewed TeamCity change that provisions equivalent agent
capabilities, maps each allow-listed lane to a job, keeps `TeamCity CI` as the
only required GitHub status, and validates cache isolation plus artifact
handoff. Figma publication remains a default-branch workflow and is not made
parallel with branch CI by this preview.

## Performance Baseline

The ten most recent successful `main` CI pipeline heads before this rollout
(TeamCity runs `1615` through `1685`, sampled on 2026-07-19) establish the
comparison baseline:

| Metric | Minimum | Median | Maximum |
|--------|---------|--------|---------|
| Queue wait | 19 s | 28.5 s | 47 s |
| Aggregate execution | 21 s | 41.5 s | 51 s |

Enforced selection records the same metrics for documentation-only,
module-only, tooling, and full-verification changes. A targeted path must not
reduce correctness, and full verification must not regress materially merely
because the plan is visible.

## Sources

- `repo/ci/domain` contains provider-neutral models, classifiers, ports, and
  topology planning.
- `repo/ci/data` contains Git, Gradle-model, JSON, HTTP, and the narrow
  TeamCity parameter and service-message adapters.
- `repo/ci/plugin` contains the Gradle tasks and composition root used by
  `prepareTeamCityCiPlan` and the preview-only
  `generateCiTopologyPreview` projector.
- `.teamcity/settings.kts` maps allow-listed parameters to visible sequential
  steps, performs their skip/run decision, and publishes the report. The
  TeamCity 2026.1 Pipeline YAML generator does not serialize inherited build
  step conditions, so the decision is explicit in each generated step command.
  Referenced parameters have job-level defaults to prevent unresolved automatic
  agent requirements; heavy verification defaults to enabled and therefore
  fails closed if runtime replacement is unavailable.
