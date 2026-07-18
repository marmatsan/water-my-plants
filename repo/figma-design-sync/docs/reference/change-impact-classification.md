---
title: Figma change-impact classification
type: reference
scope: repo/figma-design-sync
owner: figma-design-sync
status: active
last-reviewed: 2026-07-18
review-cycle-days: 90
sources:
  - repo/figma-design-sync/change-impact-policy.json
  - repo/figma-design-sync/plugin/src/main/kotlin/com/marmatsan/figmaDesignSync/plugin/checker/impact/FigmaChangeImpactClassifier.kt
  - repo/figma-design-sync/plugin/src/main/kotlin/com/marmatsan/figmaDesignSync/plugin/task/impact/ClassifyFigmaChangeImpactTask.kt
---

# Figma Change-Impact Classification

## Purpose

`classifyFigmaChangeImpact` determines whether changed repository paths can
alter the generated Figma model or its visual writer. The result lets CI avoid
model generation and MCP writes when the authoritative Figma state cannot
change.

The classifier is deterministic domain logic. Git discovery and JSON parsing
are adapters, and the Gradle task is the execution boundary used locally and by
TeamCity.

## Inputs

The default task compares `HEAD` with `origin/main`. When `HEAD` is `main`, it
compares the current commit with its first parent so a post-merge build
classifies the merged revision.

Path rules live in `repo/figma-design-sync/change-impact-policy.json`. The
policy is the only source for documentation-only, transport-only,
model-neutral, model-content, visual-writer, and visual-target path patterns.

Tests may provide `figmaChangedPaths` and `figmaComparisonBase` Gradle
properties. Production CI MUST use the Git-derived defaults.

## Output

The task writes:

```text
build/reports/figma-sync/change-impact.json
```

The contract contains:

| Field | Meaning |
|-------|---------|
| `scope` | Repository verification scope. |
| `figmaImpact` | Kind of possible Figma change. |
| `affectedVisualTargets` | Smallest configured writer targets, or `all` when a writer path is unmapped. |
| `comparisonBase` | Git revision used as the diff base. |
| `changedPaths` | Normalized repository-relative paths classified by the task. |

## Classification Precedence

The classifier applies these outcomes in order:

1. `documentation-only`: every path is explicitly documentary;
2. `transport-only`: every non-document path affects only MCP transport;
3. `model-neutral`: every remaining path is declared unable to change the
   model or compiled writer;
4. `model-content`: at least one path can alter `design-model.json`;
5. `visual-targets`: a compiled writer path changed;
6. `unknown`: no safe rule recognizes the change.

`documentation-only`, `transport-only`, and `model-neutral` are Figma no-op
scopes. `unknown` always keeps `full-verification`. An unmapped visual writer
uses target `all`; classification must fail closed rather than infer a smaller
scope.

## Platform Contract

The classifier and its tests are Kotlin and run through the Gradle wrapper on
Windows, macOS, and Linux. Filesystem paths are normalized to `/` before policy
matching. Git is invoked through `ProcessBuilder` rather than a shell-specific
script.

PowerShell scripts may consume `change-impact.json` to orchestrate the current
Windows TeamCity agent, but they MUST NOT duplicate classification rules.
Platform-specific scripts select `gradlew.bat`, `mvnw.cmd`, and `npm.cmd` on
Windows and their Unix counterparts elsewhere. The checked-in `gradlew` and
`mvnw` wrappers MUST retain their executable Git mode for macOS and Linux
agents.

## Boundaries

The classifier does not validate documentation coverage, generate the design
model, execute MCP, write Figma metadata, or authorize publication from a
branch. Documentation coverage remains owned by
`.teamcity/scripts/validate-documentation.ps1`; official publication remains
restricted to TeamCity Figma Sync on `main`.
