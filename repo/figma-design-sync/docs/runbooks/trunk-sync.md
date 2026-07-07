# Figma Trunk Sync Runbook

## Purpose

`main` is the source of truth for the dependency design model. Figma is
considered synchronized only when the configured metadata page stores the same
`modelHash` generated from `main`.

This runbook is the short execution path. Keep the detailed visual model rules
in [visual-sync-contract.md](visual-sync-contract.md) and failure recovery notes
in [troubleshooting.md](troubleshooting.md).

## Ownership

TeamCity owns the repository workflow:

- Pull request `CI` runs repository verification. It must not generate or
  publish `build/reports/figma-sync/design-model.json`.
- Post-merge `Figma Sync` runs on `main`, generates the only authoritative
  `design-model.json`, publishes it as an artifact, and verifies Figma metadata
  with `checkFigmaTrunkSync`.
- The Figma write step is currently MCP-operated. When repairing a failed
  `Figma Sync` run on `main`, use the `design-model.json` artifact published by
  TeamCity's `Generate main design model` job. Do not regenerate the model from
  a feature branch to repair `main`.

Local execution is useful for diagnosis, but it is not required before every
commit because Figma sync depends on external Figma state, credentials, and the
MCP write flow.

## Sources

| Source | Value |
|--------|-------|
| Figma page URL | `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62934-908` |
| Figma file key | `YBZXsd8oyGLbcI2KWxJvRK` |
| Metadata page node id | `62934:908` |
| Shared plugin data namespace | `water_my_plants_sync` |
| Generated model | `build/reports/figma-sync/design-model.json` |
| Repository versions file | `repo/dependency-catalog/versions.properties` |
| Figma versions section | `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62936-183` |
| Figma versions variable collection | `repo\dependency-catalog\versions.properties` |
| Figma UML documentation page | `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63308-2386` |
| Root settings file | `settings.gradle.kts` |

Default included builds:

| Gradle build name | Model name | Root directory | Module path prefix | Publishes catalogs | Publishes convention plugins |
|-------------------|------------|----------------|--------------------|--------------------|-----------------------------|
| `dependency-catalog` | `dependencyCatalog` | `repo/dependency-catalog` | `:dependency-catalog` | No | No |
| `figma-design-sync` | `figmaDesignSync` | `repo/figma-design-sync` | `:figma-design-sync` | Yes | No |
| `gradle-plugins` | `gradlePlugins` | `repo/gradle-plugins` | `:gradle-plugins` | Yes | Yes |

Catalog tree visual targets:

| Model target | Source | Figma section |
|--------------|--------|---------------|
| `waterMyPlants.libraries` | `repo/dependency-catalog/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt` | `63069:629` |
| `waterMyPlants.plugins` | `repo/dependency-catalog/src/main/kotlin/com/marmatsan/dependencies/PluginTrees.kt` | `63069:594` |
| `waterMyPlants.customGradleConventionPlugins` | `repo/gradle-plugins/**/build.gradle.kts` | `63216:6907` |
| `waterMyPlants.customGradlePlugins` | repository included-build `**/build.gradle.kts` files that declare regular Gradle plugins | `63330:551` |
| `gradlePlugins.libraries` | `repo/gradle-plugins/settings.gradle.kts` | `63099:951` |
| `gradlePlugins.plugins` | `repo/gradle-plugins/settings.gradle.kts` | `63100:2952` |
| `figmaDesignSync.libraries` | `repo/figma-design-sync/settings.gradle.kts` | `63573:260` |
| `figmaDesignSync.plugins` | `repo/figma-design-sync/settings.gradle.kts` | `63573:346` |

`repo/dependency-catalog` has no settings-catalog visual target. It contributes
the versions file and the standalone `:dependency-catalog` module.

## Official Sync Input

The only valid input for a Figma write is the `design-model.json` artifact from
TeamCity:

```text
Figma Sync > Generate main design model > build/reports/figma-sync/design-model.json
```

Before running the MCP write, verify the artifact:

- It comes from the `Figma Sync` pipeline, not from `CI`.
- The pipeline branch is `<default>` / `main`.
- The JSON contains `"branch": "main"`.
- The JSON `gitSha` matches the `main` revision from the TeamCity run.
- The MCP bundle is generated from code compatible with that `main` artifact.

`generateFigmaDesignModel` is intentionally guarded. It requires the TeamCity
Figma Sync pipeline to set `FIGMA_DESIGN_SYNC_OFFICIAL=true` and
`FIGMA_DESIGN_SYNC_BRANCH` to the build branch. The task normalizes
`<default>`, `refs/heads/main`, and `origin/main` to `main`, and rejects any
other branch before writing the artifact.

The MCP sync code rejects models whose `branch` is not `main`. A branch-local
model is never an authorized Figma write input, even when its `modelHash`
matches the expected content.

## Local Diagnostics

Do not generate `design-model.json` locally to repair Figma. Local execution is
diagnostic only and must not create the official Figma write input. Use the
artifact from `Figma Sync > Generate main design model` when inspecting the
model that should be synchronized.

The TeamCity artifact JSON contains:

- `schemaVersion`
- `branch`
- `gitSha`
- `generatedAt`
- `content`
- `modelHash`

`content` is the stable comparison body. It includes `versions`,
`versionSections`, `catalogs`, `modules`, and `moduleDependencies`.
`modelHash` is calculated from stable model content and excludes `branch`,
`gitSha`, `generatedAt`, and `modelHash` itself.

Important generation details:

- `content.versions` is the sorted flat map used for deterministic comparison.
- `content.versionSections` preserves grouping from
  `repo/dependency-catalog/versions.properties` so the MCP sync can place
  visual version nodes in the correct frame.
- `content.catalogs` contains Water My Plants libraries/plugins, catalog data
  from included builds that publish settings catalogs, custom Gradle convention
  plugins, and regular custom Gradle plugins.
- `dependencyCatalog` contributes modules and module dependencies but not
  `content.catalogs.dependencyCatalog` because
  `repo/dependency-catalog/settings.gradle.kts` does not declare
  `versionCatalogs.create("libs")` or `versionCatalogs.create("plugins")`.
- Custom Gradle convention plugins are detected from `repo/gradle-plugins`
  build files that declare an implementation class ending in
  `GradleConventionPlugin`.
- Regular custom Gradle plugins are detected from repository included builds
  that declare a Gradle plugin implementation class that does not end in
  `GradleConventionPlugin` and whose plugin id is applied from the main build.
- `com.marmatsan.figmaDesignSync` is a regular Gradle plugin, not a convention
  plugin. It is included in `content.catalogs.waterMyPlants.customGradlePlugins`.
- `content.moduleDependencies` contains one graph for the root build and one
  graph per configured included build: `main`, `dependencyCatalog`,
  `figmaDesignSync`, and `gradlePlugins`.
- Module dependency extraction reads explicit `project(":...")` calls and
  type-safe project accessors such as `projects.core.ui` and
  `projects.domain`.
- Module dependency graphs are no longer rendered into the deleted Figma
  `dependency of modules` section. They belong in PlantUML architecture docs.

## Build The MCP Bundle

The visual sync source lives under `repo/figma-design-sync/tools/src/`.
TypeScript is the source of truth; JavaScript is only the generated runtime
artifact executed by Figma MCP.

```powershell
cd repo\figma-design-sync\tools
npm ci
npm run build
```

The generated script is:

- `repo/figma-design-sync/tools/sync-trunk-design-model.mcp.js`

Do not commit the generated JavaScript. The script expects
`design-model.json` to be injected as `DESIGN_MODEL` before execution and must
run in the Figma MCP runtime because it uses the Figma plugin API.

Build and execute the MCP bundle from a code version compatible with the
TeamCity `main` artifact being synced. If local sync tooling is ahead of `main`,
run a bundle generated from `main` or pass an explicit target list that matches
the catalogs present in the TeamCity model.

## Run The MCP Sync

Use the Figma MCP `use_figma` tool to:

1. Read the TeamCity `Figma Sync > Generate main design model` artifact.
2. Update version variables and `.project version` visual nodes.
3. Update catalog tree sections listed in this runbook.
4. Apply the visual contract from
   [visual-sync-contract.md](visual-sync-contract.md).
5. Verify the visual mutation result.
6. Write metadata only after all visual updates complete successfully.

The Figma MCP `use_figma` call has a practical source-size limit near 50k
characters. If the generated script plus injected model is small enough, pass a
base64 payload split into chunks in a single call. When it approaches the limit,
stage chunks in temporary shared plugin data and validate chunk count and length
before decoding. Do not copy long base64 payloads manually from terminal output.

The runtime supports `atob`, `btoa`, and `Function`, but not browser or Node
transfer helpers such as `fetch`, `XMLHttpRequest`, `importScripts`,
`TextDecoder`, `Blob`, `Response`, or `DecompressionStream`.

## Write Metadata

Metadata must be written last. `checkFigmaTrunkSync` trusts the metadata hash, so
writing it before visuals are reconciled can make CI pass while Figma is still
visually stale.

Read these values from `design-model.json`:

- `schemaVersion`
- `branch`
- `gitSha`
- `modelHash`

Write them to page `62934:908` under namespace `water_my_plants_sync`, plus
`syncedAt`. Figma shared plugin data namespaces accept only alphanumeric
characters, `_`, and `.`.

```javascript
const namespace = "water_my_plants_sync";
const page = await figma.getNodeByIdAsync("62934:908");

if (!page || page.type !== "PAGE") {
  throw new Error("Expected node 62934:908 to be a PAGE");
}

await figma.setCurrentPageAsync(page);

page.setSharedPluginData(namespace, "schemaVersion", String(SCHEMA_VERSION_FROM_JSON));
page.setSharedPluginData(namespace, "branch", BRANCH_FROM_JSON);
page.setSharedPluginData(namespace, "gitSha", GIT_SHA_FROM_JSON);
page.setSharedPluginData(namespace, "modelHash", MODEL_HASH_FROM_JSON);
page.setSharedPluginData(namespace, "syncedAt", new Date().toISOString());

return {
  mutatedNodeIds: [page.id],
  pageName: page.name,
  namespace,
  modelHash: page.getSharedPluginData(namespace, "modelHash"),
  gitSha: page.getSharedPluginData(namespace, "gitSha")
};
```

A metadata-only write is acceptable only when the visual model is already known
to match `design-model.json` and the only mismatch is stale shared plugin
metadata.

## Verify

`checkFigmaTrunkSync` reads Figma shared plugin data through the Figma REST API
and compares it with a freshly generated model from the current checkout.

```powershell
$line = Get-Content -Path .env | Where-Object { $_ -like 'FIGMA_FILE_CONTENT_ACCESS_TOKEN=*' } | Select-Object -First 1
$env:FIGMA_FILE_CONTENT_ACCESS_TOKEN = ($line -split '=', 2)[1].Trim('"')
.\gradlew.bat checkFigmaTrunkSync
```

The token must have read access to the Figma file and the `file_content:read`
scope.

The check fails when:

- Figma does not expose shared plugin data for `water_my_plants_sync`.
- Figma is missing `modelHash` or `gitSha`.
- Figma's `modelHash` differs from the model generated from the checkout.
- The visual MCP sync refused to write metadata because required variables,
  sections, instances, or connector templates were missing.

The check contract is intentionally narrow:

- Generate the model from the current checkout.
- Read `water_my_plants_sync.modelHash` from the configured Figma page.
- Pass only when both hashes are identical.

It does not prove that every `.tree node`, connector, variable, or UML section is
visually correct. Those are obligations of the MCP write step and the UML
publication runbook.

## TeamCity Integration

`CI` is the pull request gate required by GitHub branch protection:

- Run normal verification.
- Do not run `generateFigmaDesignModel`.
- Do not publish `build/reports/figma-sync/design-model.json`.

`CI` does not generate or verify Figma sync state because Figma represents
`main`, not every short-lived branch.

`Figma Sync` is the post-merge pipeline for `main`:

- Trigger after `CI` succeeds on `<default>`.
- Run `generateFigmaDesignModel` from `main`.
- Publish `build/reports/figma-sync/design-model.json`.
- Run `checkFigmaTrunkSync` against the metadata currently stored in Figma.
- Verify every added or changed `.puml` diagram has been rendered and published
  to the Figma UML documentation page in its own locked section.

Until the Figma write step is automated inside TeamCity, the first `Figma Sync`
run after a model-affecting merge can identify that `main` is not yet reflected
in Figma, but the actual write still happens through MCP. After the MCP sync
writes metadata with the TeamCity artifact, rerun `Figma Sync`. A local
`.\gradlew.bat checkFigmaTrunkSync` run is diagnostic only and does not replace
the TeamCity status.

TeamCity parameter setup for verification:

- Secure parameter: `figma.file.content.access.token`
- Environment parameter:
  `env.FIGMA_FILE_CONTENT_ACCESS_TOKEN=%figma.file.content.access.token%`

## Release Barrier

Before creating `release/<version>`:

- `main` must pass the normal build and test suite.
- `generateFigmaDesignModel` must produce the current model.
- Figma must be synced through the MCP step.
- Every `.puml` diagram added or changed on `main` must already live in the
  Figma UML documentation page.
- `checkFigmaTrunkSync` must pass.

Only after that barrier is green:

- Create `release/<version>`.
- Stabilize the release branch.
- Merge `release/<version>` into `main`.
- Publish the stable version.

After merging changes that affect generated model content, regenerate the model
from `main` and sync Figma again. Commits that only change traceability
metadata, CI settings, or unrelated files do not require a Figma sync if
`modelHash` stays unchanged.
