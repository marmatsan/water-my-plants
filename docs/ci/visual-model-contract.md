# CI Visual Model Contract

## Purpose

The Figma CI documentation represents the current, verifiable repository and
infrastructure contract. It does not describe planned deployment environments,
future automation, live build results, or runtime metrics.

The parent Figma section is named:

```text
Continuous Integration and Design Documentation
```

It contains two levels of detail:

1. `Overview`
2. `Pull Request Integration`
3. `Post-merge Design Documentation`
4. `Infrastructure and Access`
5. `Windows Service Runtime`

All visual labels and descriptions use English.

## Sources Of Truth

The visual model aggregates sources without duplicating their ownership:

- `.teamcity/settings.kts` owns pipelines, classic build configurations, jobs,
  triggers, snapshot dependencies, build features, branch filters, commands,
  artifacts, and repository attachment.
- TeamCity generated configuration is an unversioned extraction input. Files
  under `.teamcity/target/generated-configs` must not be committed.
- `docs/ci/external-topology.yaml` owns external systems and connections that
  are not represented by TeamCity DSL, including Cloudflare access and tunnel
  boundaries, user and CLI access, the GitHub webhook ingress, the GitHub status
  transport boundary, and the external MCP-operated Figma write.
- `docs/ci/windows-runtime.yaml` owns the reviewed Windows service inventory,
  startup modes, and service identities for the local CI host.
- The generated `design-model.json` aggregates these sources for the visual sync.
- Figma is derived documentation and is not a source of CI configuration.

The generated JSON stores this aggregate under `content.ci`:

- `externalTopology` contains the versioned YAML topology;
- `windowsRuntime` contains the versioned Windows service inventory;
- `teamCity` contains the effective generated pipelines and VCS roots.

The official Figma model jobs must materialize the effective TeamCity
configuration before Gradle generates the model or checks its hash. Gradle
consumes that generated directory as a declared input. The independent CI
verification task `checkTeamCityDsl` may invoke the Maven wrapper explicitly;
it does not participate in Figma model generation.

The external topology YAML must not duplicate TeamCity pipeline or job
definitions. It must not contain tokens, secrets, credential references,
account identifiers, personal names, or visual layout coordinates.
The Windows runtime YAML must not contain service command lines, credentials,
or tunnel configuration. In particular, it must never record the Cloudflared
service command line because it can contain the tunnel credential.

## Visual Reading Levels

### Overview

The overview contains three distinct journeys:

- Pull request integration: branch push, pull request, CI verification, GitHub
  check, merge gate, and merge to `main`.
- Post-merge design documentation: successful CI on `main`, Figma Sync model
  generation and verification, the external operator and Codex/MCP write, and
  the verification rerun.
- Infrastructure health: the Windows startup request plus the daily TeamCity
  fallback schedule, agent capability and disk checks, HTTPS boundary probes,
  and the published `ci-health` report. This is an internal host-driven signal,
  not an independent uptime monitor.

Cloudflare appears as a simplified boundary in the overview. Its policies and
authentication paths belong in `Infrastructure and Access`.

`Windows Service Runtime` is a connector-free inventory. It shows TeamCity
Server, Build Agent, and Cloudflare Tunnel as independent service nodes with
their platform, Windows service name, startup mode, and service identity. The
operational recovery order remains in the linked runbook and is not modeled as
a dependency between services.

### Operational Detail

Operational sections show:

- exact pipeline, job, check, and artifact names;
- a separate short description for each named element;
- triggers and branch conditions;
- exact repository-owned Gradle task entry points, selection conditions, and
  relevant task dependencies;
- summarized names for non-Gradle operational steps;
- relevant artifacts and published GitHub checks;
- links from section headers and operational nodes to canonical files on
  GitHub `main`.

Links should target files rather than line numbers so that routine source edits
do not break them. Literal command content remains in the linked TeamCity DSL.

### Gradle Task Display

The visual model uses two complementary components:

| Component | Reading purpose |
|-----------|-----------------|
| `.ci node` | Identifies the actor, system, pipeline, job, artifact, check, or gate and summarizes its responsibility. |
| `.ci step` | Explains the ordered work performed by a job, including nested Gradle tasks, selection decisions, conditions, and published outcomes. |

The Kotlin visual plan owns a typed `steps` array for every node. Each entry
contains an order, role, level, title, and optional technical identifier,
description, and condition. The `.ci node` component reserves exactly 20
exposed `.ci step` instances named `step 01` through `step 20`. All slots are
hidden by default. The writer configures and reveals only the slots required by
the node and keeps the unused slots hidden. A plan that needs more than 20
steps fails before Figma is mutated; split that node into a clearer visual
boundary instead of silently dropping work.

The component orders its content as summary, executable steps, then optional
runtime and source details. The `show optional details` boolean collapses that
last container when both kinds of context are hidden. The legacy `.ci node`
`Steps` text property remains part of the component API for compatibility, but
generated nodes hide it; it is not a second source of step content.

Use the step variants consistently:

| Variant axis | Value | Meaning |
|--------------|-------|---------|
| `role` | `action` | Work executed by TeamCity or Gradle. |
| `role` | `decision` | A runtime selection or branch in the verification plan. |
| `role` | `outcome` | An artifact or check published after successful execution. |
| `level` | `phase` | A top-level TeamCity step or job outcome. |
| `level` | `nested` | A Gradle task or decision expanded from a TeamCity phase. |

Phase orders use two digits, such as `01` and `02`. Nested orders extend their
parent phase, such as `02.1`. Nested actions emphasize a human-readable title,
the exact task identifier, and any execution condition; their longer
description remains in the versioned plan but is hidden in Figma to keep large
jobs scannable. Decisions and outcomes retain their description because it
explains why the branch exists or what downstream contract is produced.

Exact task identifiers make failures traceable to their executable source
while the linked files remain authoritative for arguments and implementation
details. The model does not add a Gradle icon or expand third-party task
internals.

The pull request `Verify` job shows:

- `prepareTeamCityCiPlan` and its `generateCiPlan` dependency;
- `ci.plan.gradleTasks` as a dynamic selection boundary;
- the always-required `checkGitWorkflow` and `checkDocumentation` tasks;
- `checkRepositoryDiff` for documentation-only changes;
- `checkTeamCityDsl` plus `check` for TeamCity changes;
- affected `:<module>:check` tasks plus `checkFigmaCatalogUsage` when the module
  graph permits targeted verification;
- `check` as the fail-closed fallback;
- the repository-owned checks, including `checkKotlinStyle`, and
  the included-build aggregate wired into the root `check` lifecycle.

The post-merge Figma jobs show the exact phased task entry points. A condition
named `Full Figma verification` means the task executes only when the validated
Figma scope requires full verification. A condition beginning with
`Gradle dependency of` records a Gradle `dependsOn` relationship; TeamCity step
order remains distinct from Gradle task dependency order when
`figmaOfficialTeamCityPhasedExecution=true`.

Do not expand Android, Kotlin, or third-party plugin task internals in Figma.
Module `check` tasks are the stable contract boundary for those implementation
details.

## Current Systems And Entities

The model distinguishes these entities when they participate in a flow:

- operator;
- browser;
- TeamCity CLI;
- GitHub Repository;
- GitHub App;
- Pull Request;
- `main` branch;
- Cloudflare Access;
- Cloudflare Tunnel;
- TeamCity Server;
- Build Agent;
- TeamCity pipelines, jobs, checks, and merge gate;
- `design-model.json` as a relevant artifact;
- Codex/MCP client;
- Figma API;
- Figma Design Document.

Short-lived branches are represented by the push connection instead of a
dedicated visual entity. Artifacts become independent visual entities only when
they form a contract between systems.

## Connection Contract

Connections are directed and describe their purpose. In particular, the model
must not collapse these GitHub and TeamCity interactions into one bidirectional
connection:

1. GitHub sends the HTTPS webhook to TeamCity through the public webhook path,
   the Cloudflare bypass policy, and Cloudflare Tunnel.
2. TeamCity obtains repository sources from GitHub.
3. TeamCity publishes checks and statuses to GitHub.

The detailed infrastructure view also distinguishes:

- browser access through Cloudflare Access user authentication;
- TeamCity CLI access through Cloudflare Service Auth followed by TeamCity
  token authentication;
- TeamCity Server scheduling work on the Build Agent;
- the Build Agent returning build results and artifacts;
- TeamCity reading Figma metadata through the Figma API;
- the operator initiating Codex/MCP and the client writing the Figma document.

Cloudflare `Allow`, `Service Auth`, and `Bypass` policies are connection
attributes, not independent systems. Connections may contain protocol,
authentication type, automation mode, branch condition, and a concise purpose,
but never concrete credentials.

## Post-merge Verification Loop

The current Figma process must be shown as a loop, not as an automatic TeamCity
write:

```text
Generate main design model
  -> design-model.json
  -> Check Figma trunk sync
  -> mismatch
  -> Operator
  -> Codex/MCP client
  -> Figma Design Document
  -> Kotlin TeamCity rerun task
  -> Check Figma trunk sync
```

The first three nodes are derived from TeamCity artifact publication and job
dependency data. A published directory such as `build/reports/figma-sync`
contains `design-model.json` and must create the same visual edge as publishing
the file explicitly. None of these generation, artifact, or verification nodes
may remain isolated.

The rerun uses the repository-owned Kotlin Gradle task. It exchanges the
Cloudflare service credential for a short-lived raw `cf-access-token` and
delegates active-run checks and waiting to `teamcity.exe`. Queueing uses a
cookie-free Kotlin REST adapter with dedicated Bearer authentication and no
redirect following. This keeps Cloudflare's session cookie out of TeamCity's
CSRF check. The TeamCity UI remains the recovery interface. The
repository-owned handoff downloads and validates the official artifact, then
selects the next checkpoint unit without writing Figma. It is represented as a
technical annotation on the handoff connection, not as another domain artifact.

`TeamCity CI` is published by the versioned classic `CI Gate` and participates
in the pull request merge gate. `Figma Sync` is post-merge documentation
verification and does not currently publish a GitHub status or participate in
pull request requirements.

## External Topology Validation

The external topology is versioned and compared manually with the active
GitHub, Cloudflare, TeamCity, and Figma configuration. The YAML records the date
of the last validation, but not the operator identity. The validation date is
repository metadata and is not rendered in Figma.

CI emits a non-blocking warning when more than 90 days have passed since the
recorded validation. The operational runbook must describe how to verify each
external connection before updating that date.

The executable warning is `checkCiExternalTopologyFreshness`, wired into the
root Gradle `check` lifecycle.

The Windows runtime follows the same 90-day manual validation contract through
`checkCiWindowsRuntimeFreshness`. Its validation procedure is documented in
`docs/ci/windows-runtime-validation.md`.

## Excluded Content

The visual model excludes:

- test and production deployment stages that are not implemented;
- future or target architecture;
- live build status, timestamps, durations, and historical metrics;
- raw tokens, secrets, IDs, account details, and personal names;
- Figma coordinates, colors, dimensions, and component property bindings;
- manually duplicated TeamCity pipelines or jobs in the external topology.
- Windows service command lines or credentials.
