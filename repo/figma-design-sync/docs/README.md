# figma-design-sync Documentation

`figma-design-sync` owns the generated design model used by the Figma
publication workflow and the Gradle checks that verify Figma sync metadata.

Use this directory as the module documentation index:

| Path | Role | Purpose |
|------|------|---------|
| `runbooks/trunk-sync.md` | Runbook | Generate, publish, and verify the Figma trunk design model. |
| `bdd/README.md` | Reference | Explain executable BDD scenarios and their technical resource map. |
| `uml/figma-import.md` | Runbook | Render PlantUML SVGs and import them into Figma. Kept under `uml/` so it stays next to diagrams and helper scripts. |
| `uml/diagrams/` | Source | PlantUML diagrams for this module. |
| `uml/tools/` | Tooling | Helper scripts used by the UML publication runbook. |

Keep this README as an index. Put operational steps in runbooks and detailed
test or model notes in the closest module reference document.
