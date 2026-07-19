---
title: CI verification plan
type: reference
scope: repository
owner: repository-tooling
status: active
last-reviewed: 2026-07-19
review-cycle-days: 180
sources:
  - repo/ci/src/main/kotlin/com/marmatsan/ci/domain/model/CiPlan.kt
  - repo/ci/src/main/kotlin/com/marmatsan/ci/domain/service/CiPlanFactory.kt
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
| `mode` | `observation` until plan-based task omission becomes authoritative, then `enforced`. |
| `comparisonBase` | Git revision used as the start of the committed diff. |
| `head` | Exact revision being planned. |
| `scope` | Primary path classification for reporting. |
| `changedFiles` | Normalized repository-relative paths. |
| `changedModules` | Directly changed Gradle modules; empty in schema version 1 observation mode. |
| `affectedModules` | Changed modules plus transitive consumers; empty in schema version 1 observation mode. |
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
- Observation mode cannot omit verification that the previous CI flow ran.
- The required GitHub status remains `TeamCity CI`.

## Performance Baseline

The ten most recent successful `main` CI pipeline heads before this rollout
(TeamCity runs `1615` through `1685`, sampled on 2026-07-19) establish the
comparison baseline:

| Metric | Minimum | Median | Maximum |
|--------|---------|--------|---------|
| Queue wait | 19 s | 28.5 s | 47 s |
| Aggregate execution | 21 s | 41.5 s | 51 s |

Future enforced selection records the same metrics for documentation-only,
module-only, tooling, and full-verification changes. A targeted path must not
reduce correctness, and full verification must not regress materially merely
because the plan is visible.

## Sources

- `repo/ci` contains the executable models, classifier, Git adapter, JSON
  writer, Gradle task, and tests.
- `.teamcity/settings.kts` publishes the report.
- `.teamcity/scripts/invoke-ci-verification.ps1` invokes the plan in
  observation mode before the existing authoritative classifier.
