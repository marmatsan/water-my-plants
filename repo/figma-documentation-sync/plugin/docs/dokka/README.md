# Module figmaDocumentationSync-plugin

Gradle plugin and orchestration layer for the Figma design sync pipeline.

This module composes domain ports with data adapters, generates
`design-model.json`, and verifies that the Figma document contains metadata for
the same model hash. It is the only `figmaDocumentationSync` module that should know
about Gradle tasks.

Read this module by workflow:

1. `FigmaDocumentationSyncGradlePlugin` is the composition root and delegates
   model, MCP, verification, and canonical-sync task registration to focused
   registrars.
2. `GenerateFigmaDesignModelTask` creates the local `design-model.json`.
3. `FigmaDesignModelGenerator` builds the executable design model contract.
4. `CheckFigmaTrunkSyncTask` verifies that Figma was synced from that contract.

# Package com.marmatsan.figmaDocumentationSync.plugin.generator

Generator classes define the JSON artifact consumed by the Figma sync step.
They should remain deterministic for the same repository inputs, except for
explicit metadata such as `generatedAt`.

# Package com.marmatsan.figmaDocumentationSync.plugin.task

Task classes expose the pipeline to Gradle. Keep IO and Gradle annotations here
instead of leaking them into the domain module.

# Package com.marmatsan.figmaDocumentationSync.plugin.checker

Checker classes compare generated model metadata with the shared plugin data
stored in the Figma document.
