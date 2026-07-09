# figma-design-sync Documentation

`figma-design-sync` owns the generated design model used by the Figma
publication workflow and the Gradle checks that verify Figma sync metadata.

Use this directory as the module documentation index:

| Path | Role | Purpose |
|------|------|---------|
| `runbooks/trunk-sync.md` | Runbook | Execute the official Figma trunk sync path and route to the detailed runbooks. |
| `runbooks/official-artifact-visual-sync.md` | Runbook | Validate the TeamCity `main` artifact and decide when branch-local visual iteration may reuse it. |
| `runbooks/mcp-chunk-transport.md` | Runbook | Build the MCP bundle, stage payloads through Figma shared plugin data chunks, run targets, and write metadata. |
| `runbooks/target-scopes.md` | Reference | Choose the smallest visual target and Figma section for a sync operation. |
| `runbooks/visual-preview.md` | Runbook | Iterate on Figma visual sync behavior with fixtures and sandbox sections without writing official metadata. |
| `runbooks/visual-sync-contract.md` | Reference | Define the Figma visual contract used by the MCP sync, including catalog trees, connectors, layout, and locking. |
| `runbooks/troubleshooting.md` | Runbook | Diagnose failed or visually incorrect Figma sync runs without weakening the metadata contract. |
| `bdd/README.md` | Reference | Explain executable BDD scenarios and their technical resource map. |
| `uml/figma-import.md` | Runbook | Render PlantUML SVGs and import them into Figma. Kept under `uml/` so it stays next to diagrams and helper scripts. |
| `uml/diagrams/` | Source | PlantUML diagrams for this module. |
| `uml/tools/` | Tooling | Helper scripts used by the UML publication runbook. |

Keep this README as an index. Put operational steps in runbooks and detailed
test or model notes in the closest module reference document.
