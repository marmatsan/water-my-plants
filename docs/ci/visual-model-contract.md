# CI Visual Model Contract

## Purpose

The Figma CI documentation represents the current, verifiable repository and
infrastructure contract. It does not describe planned deployment environments,
future automation, live build results, or runtime metrics.

The parent Figma section is named:

```text
Continuous Integration and Design Documentation
```

It contains five sections grouped into two reading levels:

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

The canonical Figma model jobs must materialize the effective TeamCity
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

The pull-request and post-merge flow sections keep job nodes compact so the
reader can follow triggers, gates, artifacts, and outcomes without crossing a
large task inventory. The separate `Job Tasks` section owns the ordered detail
for every executable TeamCity job. It places one job per column, preserves the
same job names as the flow sections, and exposes the pipeline name in each job
description. This separation is a presentation boundary only: the effective
TeamCity configuration remains the source of every phase, task, decision, and
outcome.

Links should target files rather than line numbers so that routine source edits
do not break them. Literal command content remains in the linked TeamCity DSL.

### Gradle Task Display

The visual model uses four composable components:

| Component | Reading purpose |
|-----------|-----------------|
| `.ci node` | Identifies the actor, system, pipeline, job, artifact, check, or gate and summarizes its responsibility. |
| `.ci phase` | Represents one ordered TeamCity build step and owns its Gradle-task detail. |
| `.ci step` | Represents a Gradle action, runtime decision, or compact task group inside one phase. |
| `.ci outcome` | Represents a published artifact or GitHub check after successful job execution. |

The semantic composition is `.ci node` -> `.ci phase` -> `.ci step`, with
`.ci outcome` as a sibling result under `.ci node`. The Figma layout places
phases inside the node's transparent `execution plan` frame, steps inside the
phase's transparent `steps` frame, and outcomes inside the node's transparent
`outcome` frame. These frames add layout structure, not execution levels.
Outcomes are not steps because they describe externally
observable results rather than work executed inside a TeamCity phase.

The Kotlin visual plan uses schema version `3`. Every node owns typed `phases`
and `outcomes` arrays. A phase contains its order, title, optional technical
identifier and description, plus a typed `steps` array. A step contains an
order, role, title, and optional technical identifier, description, and
condition. An outcome replaces `role` with `kind` and otherwise uses the same
display metadata.

The stable capacity is:

| Owner | Slot container | Exposed slots | Capacity |
|-------|----------------|---------------|----------|
| `.ci node` | `execution plan` | `phase 01` through `phase 08` | 8 phases |
| `.ci phase` | `steps` | `step 01` through `step 08` | 8 steps per phase |
| `.ci node` | `outcome` | `outcome 01` through `outcome 04` | 4 outcomes |

Every slot is visible in its master component so maintainers can inspect the
complete composition. Generated instances reveal only populated slots and hide
the remainder. Plans that exceed a capacity fail before Figma is mutated; split
the job or phase into a clearer visual boundary instead of dropping work.

The `.ci phase` master exposes `show steps=true` and binds it to the visibility
of its direct `steps` frame. Generated instances set it from the typed
`phase.steps` array: `true` when the phase has executable detail and `false`
when the array is empty. Hiding only the reserved step instances is not enough;
an empty visible Auto Layout frame can retain its master height and create a
large blank region in the generated job node.

Every property-backed field and optional section is also visible by default in
each master component and every master variant. Master visibility booleans use
`true` so the component surface documents its complete public contract. Only
instances may hide fields or sections according to their model data; the writer
must set those instance visibility properties explicitly instead of treating a
hidden master default as presentation policy.

Generated job nodes in the pull-request and post-merge flow sections receive
empty `phases` and `outcomes` arrays. Only their matching nodes in `ci.jobTasks`
receive and display executable detail. Do not duplicate the hierarchy across
both reading layers.

The node orders its content as summary, `execution plan`, `outcome`, then
optional runtime and source details. `execution plan heading` supplies the
visible `Execution plan` label and `show execution plan` hides the complete
frame when a node has no phases. `show outcome` hides the complete outcome
frame when the node has no observable results. The `show optional details`
boolean collapses the final details container when both kinds of context are
hidden. The `.ci node` does not expose the obsolete `steps` or `show steps`
properties; `show steps` belongs only to `.ci phase`, where it controls the
phase-owned step container.

The `summary` and outer `optional details` frames stretch to the current node
width. This keeps the header and details boundary aligned with expanded task
nodes while compact flow nodes remain narrow. `optional details` owns the
full-width runtime divider and an inside-aligned 2 px stroke bound to
`md/sys/color/outline`; its nested `details content` frame keeps runtime and
source text at the compact reading width.

Use the step variants consistently:

| Variant axis | Value | Meaning |
|--------------|-------|---------|
| `role` | `action` | Work executed by TeamCity or Gradle. |
| `role` | `decision` | A runtime selection or branch in the verification plan. |
| `role` | `group` | A compact family of tasks selected by the same impact rule. |

`role=group` keeps the exact identifiers in the typed `technicalId` field but
projects them through the component's public `tasks` property. The value is one
left-aligned `TextNode` containing one literal `• ` bullet per line. Literal
bullets are part of the property value because a Figma `TEXT` override replaces
range formatting; relying on native list styling would lose the bullets in
generated instances. The master shows at least two lines so this multiline
contract remains visible to component maintainers.

`.ci outcome` has the independent `kind` variants `artifact` and `check`.
There is no `level` variant: component composition owns hierarchy. Do not nest
another `.ci step` inside a step. Split the phase when a third execution level
would otherwise be needed.

Every `.ci step` renders as a compact horizontal Gradle row with the order badge
first, the Gradle icon, and one flexible content column. Actions and decisions
identify one executable entry point with `TASK`; groups use `TASKS` followed by
their multiline list. `.ci phase` supplies the TeamCity context; `.ci outcome`
supplies the result context without pretending either is a Gradle task.

The environment icon in the `.ci node` header describes the owner of the whole
node. A TeamCity job therefore remains `environment=teamcity` even when its
nested steps invoke Gradle. Use `environment=gradle` on a node only when the
whole node represents the Gradle Build Tool rather than a TeamCity job.

The row has no outline. Its surface fill, order badge, spacing, and typography
provide separation from adjacent steps. The condition metadata is borderless as
well; it remains identifiable through the `WHEN` label instead of another
outlined container.

Render `.ci phase` as a transparent section rather than a filled card around
its steps. Its header uses `md/sys/color/primary-container` with 8 px padding;
the transparent `steps` frame uses 16 px left padding and 8 px row spacing.
`.ci step` rows use `md/sys/color/surface-container-highest` with 4 px padding,
while `.ci outcome` uses `md/sys/color/secondary-container` with 6 px padding.
This token and spacing boundary distinguishes TeamCity phases, Gradle detail,
and published results without creating a card-inside-card hierarchy.

Phase and outcome orders use two digits, such as `01` and `04`. Step orders
extend their parent phase, such as `03.1`. Actions emphasize a human-readable
title, exact task identifier, and execution condition; their longer description
remains in the versioned plan but is hidden in Figma. Decisions, groups, and
outcomes retain their descriptions because they explain selection or downstream
contracts.

Exact task identifiers make failures traceable to their executable source
while the linked files remain authoritative for arguments and implementation
details. The model does not add a Gradle icon or expand third-party task
internals.

The pull request `Verify` job shows three phases and two outcomes. `Generate
verification plan` contains:

- `prepareTeamCityCiPlan` and its `generateCiPlan` dependency;

`Run planned Gradle checks` contains:

- `ci.plan.gradleTasks` as a dynamic selection boundary;
- an `Always` group for `checkGitWorkflow` and `checkDocumentation`;
- an `According to changes` group for `checkRepositoryDiff`, `checkTeamCityDsl`,
  affected `:<module>:check` tasks, and `checkFigmaCatalogUsage`;
- a `Full verification` group for root `check`, including Kotlin style,
  catalogs, versions, CI freshness, and `verification-platform` verification.

The sibling outcomes publish `build/reports/ci` as an artifact and `TeamCity CI`
as a GitHub check. Grouping avoids presenting mutually exclusive or
dependency-expanded tasks as repeated linear work while preserving their exact
identifiers in the versioned plan.

The post-merge Figma jobs show the exact phased task entry points. A condition
named `Full Figma verification` means the task executes only when the validated
Figma scope requires full verification. A condition beginning with
`Gradle dependency of` records a Gradle `dependsOn` relationship; TeamCity step
order remains distinct from Gradle task dependency order when
`figmaCanonicalTeamCityPhasedExecution=true`.

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
main
  -> Figma Sync pipeline
  -> Generate main design model
  -> design-model.json -----------------------------+
                                                       |
Figma Design Document -> current metadata -------------+
                                                       v
                                            Check Figma trunk sync
                                              |-- Metadata matches -> success
                                              `-- Visual sync required
                                                    -> Operator
                                                    -> Codex/MCP client
                                                    -> Figma Design Document
                                                    -> rerunTeamCityFigmaSync
                                                    -> Figma Sync pipeline
```

The first three nodes are derived from TeamCity artifact publication and job
dependency data. A published directory such as `build/reports/figma-sync`
contains `design-model.json` and must create the same visual edge as publishing
the file explicitly. None of these generation, artifact, or verification nodes
may remain isolated.

`Check Figma trunk sync` has two direct `.ci outcome` children. `Metadata
matches` is a terminal success signal. `Visual sync required` is the decision
that enters the supervised operator loop. These are internal job results, not
GitHub checks: the planner must derive them from the canonical verification job
itself and must not depend on an optional `publishedChecks` configuration.

The rerun uses the repository-owned Kotlin Gradle task. It exchanges the
Cloudflare service credential for a short-lived raw `cf-access-token` and
delegates active-run checks and waiting to `teamcity.exe`. Queueing uses a
cookie-free Kotlin REST adapter with dedicated Bearer authentication and no
redirect following. This keeps Cloudflare's session cookie out of TeamCity's
CSRF check. The rerun queues the complete `Figma Sync` pipeline, so canonical
generation and metadata verification execute again; it does not jump directly
to the check job. The TeamCity UI remains the recovery interface. The
repository-owned handoff downloads and validates the canonical artifact, then
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
