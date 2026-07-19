# Figma Documentation Sync Tools

This package contains the portable TypeScript boundary executed inside the
Figma Plugin API runtime. Kotlin owns official runner generation, MCP transport,
capability probing, execution planning, and checkpoints. The package is built
with a repository-owned configuration so Figma node identities and visual
targets remain outside the reusable writer.

Prepare a configured tool workspace with:

```powershell
npx figma-documentation-sync-build `
    --project-config-json=path\to\writer-project-config.json `
    --output-dir=build\figma-documentation-sync-tools
```

The JSON input uses schema version `1` and is the only public project-config
format. `FIGMA_DOCUMENTATION_SYNC_PROJECT_CONFIG` may supply the same path when the
command is invoked through npm; an explicit command-line path takes precedence.

Water My Plants generates the JSON and runs the package through Kotlin-owned
Gradle tasks:

```powershell
.\gradlew.bat testFigmaDocumentationSyncTools buildFigmaDocumentationSyncTools
```

The package remains `private` until a release is explicitly authorized. The
publication runbook describes the release gate and version alignment contract.

`fixtures/contracts/writer-runtime-contract.json` is the language-neutral
baseline for runner manifests, targets, transports, and generated MCP file
roles. Kotlin replacements must satisfy this fixture before a TypeScript
implementation is removed.

Use the root `probeFigmaMcp` and `runFigmaMcp` Gradle tasks for MCP operations.
The TypeScript `mcp:preview` command is a preview-only packager for supervised
visual inspection. It rejects official mode, metadata writes, PNG transport,
preflight, and multi-target execution. Official manifests, payload transport,
fingerprints, execution plans, and checkpoints are generated and enforced by
Kotlin.
