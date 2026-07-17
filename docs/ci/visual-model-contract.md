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

- `.teamcity/settings.kts` owns pipelines, jobs, triggers, branch filters,
  commands, artifacts, and GitHub status publishers.
- TeamCity generated configuration is an unversioned extraction input. Files
  under `.teamcity/target/generated-configs` must not be committed.
- `docs/ci/external-topology.yaml` owns external systems and connections that
  are not represented by TeamCity DSL, including Cloudflare access and tunnel
  boundaries, user and CLI access, the GitHub webhook ingress, and the external
  MCP-operated Figma write.
- `docs/ci/windows-runtime.yaml` owns the reviewed Windows service inventory,
  startup modes, and service identities for the local CI host.
- The generated `design-model.json` aggregates these sources for the visual sync.
- Figma is derived documentation and is not a source of CI configuration.

The generated JSON stores this aggregate under `content.ci`:

- `externalTopology` contains the versioned YAML topology;
- `windowsRuntime` contains the versioned Windows service inventory;
- `teamCity` contains the effective generated pipelines and VCS roots.

The official TeamCity jobs must run `teamcity-configs:generate` before Gradle
generates the model or checks its hash. Gradle consumes the generated directory
as a declared input; it does not invoke Maven implicitly.

The external topology YAML must not duplicate TeamCity pipeline or job
definitions. It must not contain tokens, secrets, credential references,
account identifiers, personal names, or visual layout coordinates.
The Windows runtime YAML must not contain service command lines, credentials,
or tunnel configuration. In particular, it must never record the Cloudflared
service command line because it can contain the tunnel credential.

## Visual Reading Levels

### Overview

The overview contains two distinct journeys:

- Pull request integration: branch push, pull request, CI verification, GitHub
  check, merge gate, and merge to `main`.
- Post-merge design documentation: successful CI on `main`, Figma Sync model
  generation and verification, the external operator and Codex/MCP write, and
  the verification rerun.

Cloudflare appears as a simplified boundary in the overview. Its policies and
authentication paths belong in `Infrastructure and Access`.

`Windows Service Runtime` is a connector-free inventory. It shows TeamCity
Server, TeamCity Build Agent, and Cloudflared as independent service nodes with
their platform, Windows service name, startup mode, and service identity. The
operational recovery order remains in the linked runbook and is not modeled as
a dependency between services.

### Operational Detail

Operational sections show:

- exact pipeline, job, check, and artifact names;
- a separate short description for each named element;
- triggers and branch conditions;
- summarized commands such as `Gradle check` and `Generate design model`;
- relevant artifacts and published GitHub checks;
- links from section headers and operational nodes to canonical files on
  GitHub `main`.

Links should target files rather than line numbers so that routine source edits
do not break them. Literal command content remains in the linked TeamCity DSL.

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
  -> HTTPS rerun client
  -> Check Figma trunk sync
```

The rerun uses the repository-owned HTTPS client. It performs active-run checks
in a read session, then queues the complete `Figma Sync` pipeline with a fresh
cookie-free session, Cloudflare's raw `cf-access-token`, and TeamCity Bearer
authentication. This avoids forwarding Cloudflare's session cookie into
TeamCity's CSRF check. The TeamCity UI remains the recovery interface. The
current temporary transport mechanism used to stage the official artifact for
MCP is represented as a technical annotation on the handoff connection, not as
another domain artifact.

`TeamCity CI` participates in the pull request merge gate. `TeamCity Figma
Sync` is a post-merge documentation status and must not be represented as a pull
request requirement.

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
