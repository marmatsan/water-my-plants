# Figma Develop Sync

## Purpose

`develop` is the source of truth for the dependency design model. Figma is considered synchronized only when the configured Figma page stores the same `modelHash` generated from the current branch.

This replaces field-by-field Figma checks. The repository generates a single JSON model, the Figma MCP sync step updates the Figma visualization, then writes sync metadata, and Gradle verifies that Figma points to the same model.

## Sources

- Figma page URL: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62934-908`
- Figma file key: `YBZXsd8oyGLbcI2KWxJvRK`
- Figma metadata page node id: `62934:908`
- Shared plugin data namespace: `water_my_plants_sync`
- Generated model: `build/reports/figma-sync/design-model.json`
- Repository versions file: `build-logic/versions.properties`
- Figma versions section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62936-183`
- Figma versions variable collection: `build-logic\versions.properties`
- Root settings file: `settings.gradle.kts`
- Build-logic settings file: `build-logic/settings.gradle.kts`

## Model Generation

Generate the design model from the repository root:

```powershell
.\gradlew.bat generateFigmaDesignModel
```

The generated JSON contains:

- `schemaVersion`
- `branch`
- `gitSha`
- `generatedAt`
- `content`
- `modelHash`

`content.versions` is the sorted flat map used for deterministic comparison. `content.versionSections` preserves the section grouping from `build-logic/versions.properties` so the Figma MCP sync can place new visual version nodes in the correct frame.

`modelHash` is calculated from the stable model content and excludes `generatedAt` and `modelHash` itself.

## Figma MCP Sync

Use the Figma MCP `use_figma` tool to update the visual Figma model and then write the generated metadata to the configured page node.

The version visual sync is implemented in:

- `build-logic/figmaDesignSync/tools/sync-develop-design-model.mcp.js`

The script expects the generated `design-model.json` to be injected as `DESIGN_MODEL` before execution. It must run in the Figma MCP runtime because it uses the Figma plugin API.

Current version sync behavior:

- Read `content.versionSections` from `build/reports/figma-sync/design-model.json`.
- Find Figma variable collection `build-logic\versions.properties`.
- Find each existing variable whose name is either the version key or ends with `/<versionKey>`, such as `Libraries/kotestVersion`.
- Ensure `Version alias` mode equals the version key.
- Set `Version number` mode to the repository value.
- Create a missing Figma variable under the matching section folder.
- Create a missing `.project version` instance in the matching visual frame.
- Bind both component properties to the created/existing variable.
- Do not edit the visual text nodes directly. `.project version` instances update through variable bindings.
- Fail without writing metadata if the design model contains an unknown version section.

Current catalog tree sync behavior:

- Read `content.catalogs` from `build/reports/figma-sync/design-model.json`.
- Update existing `.tree node` instances in the Water My Plants library/plugin sections.
- Update existing `.tree node` instances in the build-logic library/plugin sections.
- Match existing tree nodes by label inside each visual section. Labels must be unique per section until stable Figma path metadata is introduced.
- Update exposed component properties for library groups, plugin ids, plugin versions, and artifact visibility.
- Update existing library artifact name/version text overrides when the existing instance structure can represent the model.
- Update `Required by` module instances for library artifacts from `requiredByModules`.
- Update `Required by` module instances for `.artifacts bundle` entries from the bundle `requiredByModules`; child `.artifact` instances inside a bundle must not show their own `Required by` section.
- Update `Applied by` module instances for plugin tree nodes from `appliedToModules`.
- Consumer module entries must be `.module` instances using the `size=small` variant. The sync updates the `.module` `name` variant instead of editing inner text overrides directly.
- Create missing `.tree node` instances by cloning a compatible existing node from the same visual section, then applying the generated model values.
- Create missing top-level tree sections when a new top-level library group or plugin id appears.
- Create missing `simple-solid_arrow` connectors by cloning an existing connector and rebinding its endpoints to the parent/child tree nodes.
- Do not delete, move, or reconcile existing stale tree nodes yet.
- Fail without writing metadata if an existing or cloned instance cannot represent the artifact text or `.module size=small` consumer structure from the generated model.

After the visual sync succeeds, write the sync metadata.

Read these values from `build/reports/figma-sync/design-model.json`:

- `schemaVersion`
- `branch`
- `gitSha`
- `modelHash`

The metadata write still targets file key `YBZXsd8oyGLbcI2KWxJvRK`:

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

Figma shared plugin data namespaces accept only alphanumeric characters, `_`, and `.`. Do not use hyphens in the namespace.

The next visual sync phases are:

- Reconcile stale visual nodes after the create/update flows are stable.
- Introduce stable Figma path metadata so duplicate labels and stale tree nodes can be reconciled safely.

### Catalog tree visual sync backlog

Use `content.catalogs` from `design-model.json` as the source for the next MCP phases.

Water My Plants catalog targets:

- Libraries source: `build-logic/dependencies/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt`
- Libraries visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-629`
- Plugins source: `build-logic/dependencies/src/main/kotlin/com/marmatsan/dependencies/PluginTrees.kt`
- Plugins visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-594`

Build-logic catalog targets:

- Source: `build-logic/settings.gradle.kts`
- Libraries visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63099-951`
- Plugins visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63100-2952`

Catalog tree rendering rules:

- Use `.tree node` component `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-678`.
- Use the `Library` variant for libraries.
- Use the `Plugin` variant for plugins.
- Connect parent/child tree nodes with `simple-solid_arrow` connectors.
- Write metadata only after all visual catalog updates complete successfully.

## Verification

The verification task reads Figma shared plugin data through the Figma REST API and compares it with a freshly generated model from the current branch:

```powershell
$line = Get-Content -Path .env | Where-Object { $_ -like 'FIGMA_FILE_CONTENT_ACCESS_TOKEN=*' } | Select-Object -First 1
$env:FIGMA_FILE_CONTENT_ACCESS_TOKEN = ($line -split '=', 2)[1].Trim('"')
.\gradlew.bat checkFigmaDevelopSync
```

The token must have read access to the Figma file and the `file_content:read` scope.

The check fails when:

- Figma does not expose shared plugin data for `water_my_plants_sync`.
- Figma is missing `modelHash` or `gitSha`.
- Figma's `modelHash` differs from the model generated from the current branch.
- The visual MCP sync step refused to write metadata because Figma was missing required visual variables.

## TeamCity Integration

TeamCity should generate `design-model.json` after a feature is merged into `develop` and publish it as a build artifact.

Recommended build steps for `develop`:

- Run normal verification: unit, integration, and end-to-end tests where available.
- Run `generateFigmaDesignModel`.
- Publish `build/reports/figma-sync/design-model.json`.

The Figma write step is currently MCP-operated. After the MCP sync step writes the metadata into Figma, run:

```powershell
.\gradlew.bat checkFigmaDevelopSync
```

TeamCity parameter setup for verification:

- Secure parameter: `figma.file.content.access.token`
- Environment parameter: `env.FIGMA_FILE_CONTENT_ACCESS_TOKEN=%figma.file.content.access.token%`

## Release Barrier

Before creating `release/<version>`:

- `develop` must pass the normal build and test suite.
- `generateFigmaDesignModel` must produce the current model.
- Figma must be synced through the MCP step.
- `checkFigmaDevelopSync` must pass.

Only after that barrier is green:

- Create `release/<version>`.
- Stabilize the release branch.
- Merge `release/<version>` into `main`.
- Publish the stable version.

After committing changes to this flow, regenerate the model and sync Figma again because `gitSha` changes with the commit.
