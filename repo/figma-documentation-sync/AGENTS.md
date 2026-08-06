# Figma Documentation Sync Agent Instructions

## Context

Apply the root [`AGENTS.md`](../../AGENTS.md), the
[architecture standard](../../docs/standards/architecture.md), the
[Gradle standard](../../docs/standards/gradle.md), the
[testing standard](../../docs/standards/testing.md), and this included build's
[README](README.md) and [documentation index](docs/README.md).

## Local Architecture

- Keep `domain` portable and independent from Gradle, filesystems, Figma,
  TeamCity, and concrete data modules.
- Keep portable infrastructure adapters in `data`; they implement
  consumer-owned domain ports.
- Keep `plugin` as the Gradle adapter and portable composition root.
- Keep TeamCity translation in `teamcity-adapter` and supervised credentialed
  operations in `teamcity-operations`. Portable modules do not depend on those
  provider-specific modules.
- Keep Water My Plants identities and concrete product composition in
  `repo/project-config`; reusable Figma modules depend on adapter contracts.
- Keep the Figma Plugin API runtime in `tools` and select product configuration
  through its project adapter. Do not add product constants to portable writer
  source.
- Use `kotlin-inject` for portable component-created collaborators and Gradle's
  supported injection model for Gradle-created extensions, tasks, and plugins.

Use capability packages that match their source paths. Add the exact package,
task, model, target, artifact, or writer contract to the closest module
reference instead of maintaining a package inventory in this instruction file.

## Workflow Routing

| Work                                              | Canonical document                                                                                              |
|---------------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| Public coordinates and independent distribution   | [`docs/reference/distribution-contract.md`](docs/reference/distribution-contract.md)                            |
| Canonical `main` sync                             | [`docs/runbooks/trunk-sync.md`](docs/runbooks/trunk-sync.md)                                                    |
| Authorized TeamCity artifact and visual handoff   | [`docs/runbooks/canonical-artifact-visual-sync.md`](docs/runbooks/canonical-artifact-visual-sync.md)            |
| MCP transport and runner execution                | [`docs/runbooks/mcp-chunk-transport.md`](docs/runbooks/mcp-chunk-transport.md)                                  |
| Smallest visual target                            | [`docs/reference/target-scopes.md`](docs/reference/target-scopes.md)                                            |
| Visual structure, layout, connectors, and locking | [`docs/reference/visual-sync-contract.md`](docs/reference/visual-sync-contract.md)                              |
| Change-impact selection                           | [`docs/reference/change-impact-classification.md`](docs/reference/change-impact-classification.md)              |
| Preview-only iteration                            | [`docs/runbooks/visual-preview.md`](docs/runbooks/visual-preview.md)                                            |
| Failure diagnosis                                 | [`docs/runbooks/troubleshooting.md`](docs/runbooks/troubleshooting.md)                                          |
| PlantUML publication                              | [`docs/uml/figma-import.md`](docs/uml/figma-import.md) and the root [UML standard](../../docs/standards/uml.md) |

Only TeamCity Figma Sync on `main` produces the canonical design-model artifact.
Use local or branch execution for diagnosis and preview under the documented
scope; do not present it as authoritative publication state.

## BDD And Tests

- Keep executable feature behavior in
  `plugin/src/test/resources/com/marmatsan/figmaDocumentationSync/plugin/bdd/`
  and Java 8 style step definitions in the matching test package.
- Describe observable guarantees in feature files. Keep packages, adapters,
  ports, and generated artifact inventories in technical documentation.
- Add focused tests to the owning portable service, adapter, Gradle task, or
  writer boundary.
- Keep filesystem, network, Gradle, TeamCity, and Figma details outside pure
  domain tests.

## Verification

Run focused checks for the changed module, then before completion run:

```powershell
.\gradlew.bat :figma-documentation-sync:domain:check `
    :figma-documentation-sync:data:check `
    :figma-documentation-sync:teamcity-adapter:check `
    :figma-documentation-sync:teamcity-operations:check `
    :figma-documentation-sync:plugin:check

.\gradlew.bat :project-config:check
```

Run root catalog, version, boundary, documentation, and aggregate checks when
the corresponding contract changes. Complete Figma/UML work with the
documented structural and visual verification, not only local code tests.
