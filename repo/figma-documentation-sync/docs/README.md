# figma-documentation-sync Documentation

`figma-documentation-sync` owns the generated design model used by the Figma
publication workflow and the Gradle checks that verify Figma sync metadata.

The included-build root owns repositories, JUnit Platform setup, sources JARs,
shared Dokka policy, and the staging publication repository for its JVM
subprojects. Module build scripts retain capability-specific dependencies,
tests, Dokka inputs, and Maven publication identity. This boundary keeps the
modules autonomous without duplicating common Gradle policy.

Use this directory as the module documentation index:

| Path                                          | Role      | Purpose                                                                                                             |
|-----------------------------------------------|-----------|---------------------------------------------------------------------------------------------------------------------|
| `guides/adopting-figma-documentation-sync.md` | Guide     | Consume the versioned Gradle plugin and configure a repository adapter without copying the source modules.          |
| `reference/distribution-contract.md`          | Reference | Define public coordinates, version alignment, artifact boundaries, and the standalone-consumer gate.                |
| `runbooks/publishing-release.md`              | Runbook   | Stage, inspect, authorize, and publish a coordinated Maven and npm release.                                         |
| `runbooks/trunk-sync.md`                      | Runbook   | Execute the canonical Figma trunk sync path and route to the detailed runbooks.                                     |
| `runbooks/canonical-artifact-visual-sync.md`  | Runbook   | Validate the TeamCity `main` artifact and decide when branch-local visual iteration may reuse it.                   |
| `runbooks/mcp-chunk-transport.md`             | Runbook   | Build the MCP bundle, stage canonical payloads through PNG or chunk fallback, run targets, and write metadata.      |
| `runbooks/visual-sync-efficiency.md`          | Runbook   | Use visual plans, capability gates, checkpoints, and staging reuse to minimize safe MCP work.                       |
| `reference/target-scopes.md`                  | Reference | Choose the smallest visual target and Figma section for a sync operation.                                           |
| `reference/change-impact-classification.md`   | Reference | Define how changed repository paths select Figma verification scope and visual targets.                             |
| `standards/dependency-version-naming.md`      | Standard  | Define the repository version key format enforced by CI and rendered in Figma.                                      |
| `runbooks/visual-preview.md`                  | Runbook   | Iterate on Figma visual sync behavior with fixtures and sandbox sections without writing canonical metadata.        |
| `reference/visual-sync-contract.md`           | Reference | Define the Figma visual contract used by the MCP sync, including catalog trees, connectors, layout, and locking.    |
| `runbooks/troubleshooting.md`                 | Runbook   | Diagnose failed or visually incorrect Figma sync runs without weakening the metadata contract.                      |
| `bdd/README.md`                               | Reference | Explain executable BDD scenarios and their technical resource map.                                                  |
| `uml/figma-import.md`                         | Runbook   | Render PlantUML SVGs and import them into Figma. Kept under `uml/` so it stays next to diagrams and helper scripts. |
| `uml/diagrams/`                               | Source    | PlantUML diagrams for this module.                                                                                  |
| `uml/tools/`                                  | Tooling   | Helper scripts used by the UML publication runbook.                                                                 |

Keep this README as an index. Put operational steps in runbooks and detailed
test or model notes in the closest module reference document.
