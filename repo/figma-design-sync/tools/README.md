# Figma Design Sync Tools

This package contains the portable TypeScript boundary executed inside the
Figma Plugin API runtime. Kotlin owns official runner generation, MCP transport,
capability probing, execution planning, and checkpoints. The package is built
with a repository-owned configuration so Figma node identities and visual
targets remain outside the reusable writer.

Prepare a configured tool workspace with:

```powershell
npx figma-design-sync-build `
    --project-config-json=path\to\writer-project-config.json `
    --output-dir=build\figma-design-sync-tools
```

The JSON input uses schema version `1` and is the only public project-config
format. `FIGMA_DESIGN_SYNC_PROJECT_CONFIG` may supply the same path when the
command is invoked through npm; an explicit command-line path takes precedence.

Water My Plants generates the JSON and runs the package through Kotlin-owned
Gradle tasks:

```powershell
.\gradlew.bat testFigmaDesignSyncTools buildFigmaDesignSyncTools
```

The package remains `private` until a release is explicitly authorized. The
publication runbook describes the release gate and version alignment contract.

`fixtures/contracts/writer-runtime-contract.json` is the language-neutral
baseline for runner manifests, targets, transports, and generated MCP file
roles. Kotlin replacements must satisfy this fixture before a TypeScript
implementation is removed.

Use the root `probeFigmaMcp` and `runFigmaMcp` Gradle tasks for MCP operations.
The TypeScript `mcp:runner` command remains only as a preview and compatibility
packager during the gradual migration. It does not calculate CI visual
structure. A CI target must receive `--ci-visual-plan=PATH` pointing to output
from the Kotlin `generateFigmaCiVisualPlan` Gradle task.
