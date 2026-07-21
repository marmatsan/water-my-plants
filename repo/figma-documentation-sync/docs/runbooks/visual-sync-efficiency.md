---
title: Efficient visual sync
type: runbook
scope: repo/figma-documentation-sync
owner: figma-documentation-sync
status: active
last-reviewed: 2026-07-18
review-cycle-days: 90
sources:
  - repo/figma-documentation-sync/data/src/main/kotlin/com/marmatsan/figmaDocumentationSync/data/mcp/McpRunnerExecutor.kt
  - repo/figma-documentation-sync/domain/src/main/kotlin/com/marmatsan/figmaDocumentationSync/domain/service/writer/McpExecutionPlanner.kt
  - repo/figma-documentation-sync/domain/src/main/kotlin/com/marmatsan/figmaDocumentationSync/domain/service/writer/VisualSyncPlanner.kt
  - repo/figma-documentation-sync/data/src/main/kotlin/com/marmatsan/figmaDocumentationSync/data/json/writer/VisualSyncPlanJson.kt
---

# Efficient Visual Sync Runbook

## Purpose

Use this runbook to execute the smallest safe Figma synchronization without
losing the strict `main` artifact, preflight, checkpoint, and metadata
contracts. The optimization target is MCP work and operator context, not a
weaker visual result.

The canonical `design-model.json` still comes only from TeamCity `Figma Sync` on
`main`. A partial plan narrows execution units from that canonical model; it does
not authorize a branch-local model or an early metadata write.

## Execution Identity

Generated runner manifests use these independent identity fields:

| Field | Meaning | Invalidates |
|-------|---------|-------------|
| `modelHash` | Stable hash of the visual model content. | Targets whose model fingerprints changed. |
| `writerHash` | Hash of the compiled visual writer. | Starts writer-scope comparison. |
| `writerScopeFingerprints` | Source fingerprints for each writer target family. | Only changed writer scopes when the change is mapped safely. |
| `writerScopeFingerprintSchemaVersion` | Version of writer source classification. | A complete migration sync when it changes. |
| `transportHash` | Hash of PNG/chunk staging behavior. | Staging only; it does not make unchanged visuals stale. |
| `gitSha` | Revision that produced the canonical artifact and checkpoint. | Artifact/checkpoint traceability, not visual state by itself. |

`manifestHash` binds those values to the exact generated files. Each visual
scope also has a model `targetFingerprint`. Model and writer fingerprints are
compared independently, then their changed scopes are combined into one plan.
Catalog roots and cleanup units inherit the fingerprint of their catalog
writer because they execute the same gateway.

## TeamCity Artifacts

For a model-affecting `main` revision, `Generate main design model` publishes:

- `design-model.json`;
- `sync-scope.json` with repository change impact and runner identities;
- `visual-sync-plan.json`;
- the complete visual runner and its `manifest.json`;
- the metadata-only runner and its `manifest.json`;
- the effective TeamCity configuration used to build the CI visual model.

`visual-sync-plan.json` has one of these decisions:

| Decision | Meaning |
|----------|---------|
| `none` | `modelHash` and `writerHash` already match Figma; skip visual and metadata writes. |
| `partial` | Known model-target or writer-scope fingerprints changed; execute `preflight` plus their union. |
| `full` | Metadata is unavailable or legacy, the fingerprint schema changed, shared writer code changed, or a model/writer difference cannot be mapped safely. |

Metadata read failures fail closed to `full`. A plan never turns an unknown
change into a no-op. The first canonical sync after introducing or changing the
writer fingerprint schema is deliberately `full`; its final metadata write
establishes the baseline used by later partial plans.

## Local Capability Probe

Probe the configured local endpoint through the Kotlin MCP SDK client:

```powershell
.\gradlew.bat probeFigmaMcp
```

The current Figma Desktop endpoint at `http://127.0.0.1:3845/mcp` advertises
read-oriented tools but not the `use_figma` and `upload_assets` write tools
required by the runner. In that state `runFigmaMcp` refuses to mutate Figma.
Keep using the canonical
Codex-operated Figma MCP write path until the endpoint advertises the required
capabilities.

When an endpoint becomes write-capable, the executor must also load
`skill://figma/figma-use/SKILL.md` before its first `use_figma` call. Missing
guidance is a hard failure, not a reason to execute without the Figma contract.

This capability gate is separate from the older unsupported remote headless
TeamCity path. Do not bypass either failure with direct REST mutations.

## Checkpointed Execution

Inspect a runner without executing it:

```powershell
.\gradlew.bat runFigmaMcp -PfigmaMcpManifest="PATH\TO\manifest.json" -PfigmaMcpPlan="PATH\TO\visual-sync-plan.json" -PfigmaMcpDryRun=true
.\gradlew.bat runFigmaMcp -PfigmaMcpManifest="PATH\TO\manifest.json" -PfigmaMcpPlan="PATH\TO\visual-sync-plan.json" -PfigmaMcpNext=true
```

When Codex executes a generated file through the supported Figma MCP writer,
record the result in the same checkpoint used by the deterministic executor:

```powershell
.\gradlew.bat runFigmaMcp -PfigmaMcpManifest="PATH\TO\manifest.json" -PfigmaMcpPlan="PATH\TO\visual-sync-plan.json" -PfigmaMcpRecordSuccess="99-00-preflight.mcp.js" -PfigmaMcpSummary="Preflight passed"
.\gradlew.bat runFigmaMcp -PfigmaMcpManifest="PATH\TO\manifest.json" -PfigmaMcpPlan="PATH\TO\visual-sync-plan.json" -PfigmaMcpRecordFailure="99-01-versions.mcp.js" -PfigmaMcpSummary="Figma component contract failed"
```

Continue from the checkpoint with `-PfigmaMcpResume=true`; use
`-PfigmaMcpRetryFailed=true` to select
only the failed execution unit. Resume is rejected when `modelHash`, `gitSha`,
`writerHash`, `transportHash`, or `manifestHash` differs from the checkpoint.

For metadata, reuse staging only after the completed visual checkpoint matches
the metadata runner identity:

```powershell
.\gradlew.bat runFigmaMcp -PfigmaMcpManifest="PATH\TO\metadata\manifest.json" -PfigmaMcpReuseStaging=true -PfigmaMcpVisualState="PATH\TO\visual\execution-state.json" -PfigmaMcpDryRun=true
```

Never record metadata success before every execution scope selected by the
visual plan has completed.

## Batching Policy

Keep one checkpoint unit per generated target/root/cleanup file. Do not merge
catalog roots merely to reduce call count: root-level units are the established
timeout and recovery boundary.

Only introduce batching after checkpoint durations show that several adjacent,
non-catalog units are consistently small. A batch must preserve lexical order,
preflight first, metadata last, and exact failed-unit reporting. Without that
evidence, batching makes retries more expensive and less diagnosable.

## Token And Output Budget

- Prefer `visual-sync-plan.json` and `-PfigmaMcpNext=true` over pasting complete
  manifests.
- Pass one generated runner file to `use_figma`; do not paste the compiled
  writer, model, or previous tool responses into chat.
- Record a short result summary and duration in `execution-state.json`.
- Inspect only the failed target and its Figma section during recovery.
- Keep PNG as the default staging transport and chunks as a fallback.

These rules reduce repeated context while keeping the canonical full/partial
decision and every successful execution unit auditable.

## Prerequisites

- Use the complete TeamCity-generated runner and visual plan.
- Probe the MCP endpoint and require the capabilities selected by the transport.
- Keep the matching model, writer, transport, manifest, and checkpoint files
  together.

## Verification

Confirm the plan decision matches changed fingerprints, each selected runner
unit has a successful checkpoint, and metadata staging reuses only a complete
compatible visual state.

## Recovery

Retry only the failed atomic unit when execution identity matches. Regenerate
the plan and checkpoints after any identity change. Fall back from PNG to
chunks only for a verified transport limitation.

## Prohibited Actions

- Do not choose `none` or `partial` manually for a canonical integration.
- Do not skip hash validation to reuse staging.
- Do not resend successful runner units merely to rebuild conversational
  context.

## Sources

- `data/src/main/kotlin/com/marmatsan/figmaDocumentationSync/data/mcp/McpRunnerExecutor.kt`
- `domain/src/main/kotlin/com/marmatsan/figmaDocumentationSync/domain/service/writer/McpExecutionPlanner.kt`
- `domain/src/main/kotlin/com/marmatsan/figmaDocumentationSync/domain/service/writer/VisualSyncPlanner.kt`
- `data/src/main/kotlin/com/marmatsan/figmaDocumentationSync/data/json/writer/VisualSyncPlanJson.kt`
- `plugin/src/main/kotlin/com/marmatsan/figmaDocumentationSync/plugin/task/mcp/RunFigmaMcpTask.kt`
