# Figma Trunk Sync

## Purpose

`main` is the source of truth for the dependency design model. Figma is considered synchronized only when the configured Figma page stores the same `modelHash` generated from the current branch.

This replaces field-by-field Figma checks. The repository generates a single JSON model, the Figma MCP sync step updates the Figma visualization, then writes sync metadata, and Gradle verifies that Figma points to the same model.

## CI Ownership

`figmaDesignSync` is a CI-owned verification step. Developers may run it locally for diagnosis, but CI is the source of truth before changes are merged into `main`.

In trunk-based development, pull requests target `main` and CI must verify that Figma reflects the model generated from the branch being validated. Local execution is useful when diagnosing a failed gate or checking credentials, but it is not a required manual step before every commit because it depends on external Figma state, access tokens, and the MCP-operated write flow.

## Sources

- Figma page URL: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62934-908`
- Figma file key: `YBZXsd8oyGLbcI2KWxJvRK`
- Figma metadata page node id: `62934:908`
- Shared plugin data namespace: `water_my_plants_sync`
- Generated model: `build/reports/figma-sync/design-model.json`
- Repository versions file: `build-logic/versions.properties`
- Figma versions section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62936-183`
- Figma versions variable collection: `build-logic\versions.properties`
- Figma Water My Plants libraries section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-629`
- Figma Water My Plants plugins section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-594`
- Figma custom Gradle convention plugins section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63216-6907`
- Figma custom Gradle plugins section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63330-551`
- Figma build-logic libraries section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63099-951`
- Figma build-logic plugins section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63100-2952`
- Figma UML documentation page: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63308-2386`
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

`content.catalogs` contains Water My Plants libraries/plugins, build-logic libraries/plugins, custom Gradle convention plugins, and regular custom Gradle plugins. Custom Gradle convention plugins are detected from `build-logic` build files that declare an implementation class ending in `GradleConventionPlugin`. Regular custom Gradle plugins are detected from `build-logic` build files that declare a Gradle plugin implementation class that does not end in `GradleConventionPlugin` and whose plugin id is applied from the main build.

`com.marmatsan.figmaDesignSync` is a regular Gradle plugin, not a Gradle convention plugin. It is intentionally excluded from `content.catalogs.waterMyPlants.customGradleConventionPlugins` and included in `content.catalogs.waterMyPlants.customGradlePlugins` because it registers sync tasks and extension configuration instead of applying build conventions to consumer modules. Applying it in the root build does not create a consumer module entry because `:` is not treated as an application module.

`content.moduleDependencies` contains two module dependency graphs:

- `main`: modules outside `build-logic`, read from `dependencies {}` blocks in root project module `build.gradle.kts` files.
- `buildLogic`: modules inside `build-logic`, read from `dependencies {}` blocks in build-logic module `build.gradle.kts` files.

Module dependency extraction reads explicit `project(":...")` calls and type-safe project accessors such as `projects.core.ui` and `projects.figmaDesignSync.domain`.

Module dependency graphs are no longer rendered into Figma's deleted `dependency of modules` section. They are architecture documentation and must be represented in PlantUML diagrams under the repository UML convention, then published to the Figma UML documentation page.

`modelHash` is calculated from the stable model content and excludes `generatedAt` and `modelHash` itself.

## Figma MCP Sync

Use the Figma MCP `use_figma` tool to update the visual Figma model and then write the generated metadata to the configured page node.

The version visual sync is implemented in TypeScript modules under:

- `build-logic/figmaDesignSync/tools/src/`

The tool source follows the same dependency direction as the Gradle sync code: `domain` contains generated-model types and pure catalog rules, `ports` contains gateway contracts, `usecases` coordinates sync behavior through those contracts, `figma` contains the Figma MCP API adapters, and `app` wires the concrete gateways for the generated MCP entrypoint.

Generate the Figma MCP JavaScript script after editing the TypeScript source:

```powershell
cd build-logic\figmaDesignSync\tools
npm ci
npm run build
```

The generated script is:

- `build-logic/figmaDesignSync/tools/sync-trunk-design-model.mcp.js`

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
- Update existing `.tree node` instances in the custom Gradle convention plugins section.
- Update existing `.tree node` instances in the custom Gradle plugins section.
- Update existing `.tree node` instances in the build-logic library/plugin sections.
- Match existing tree nodes by label inside each visual section. Labels must be unique per section until stable Figma path metadata is introduced.
- Update exposed component properties for library groups, plugin ids, plugin versions, and artifact visibility.
- Update existing library artifact name/version text overrides when the existing instance structure can represent the model.
- Update `Required by` module instances for library artifacts from `requiredByModules`.
- Update `Required by` module instances for `.artifacts bundle` entries from the bundle `requiredByModules`; child `.artifact` instances inside a bundle must not show their own `Required by` section.
- Update `Applied by` module instances for plugin tree nodes from `appliedToModules`.
- Consumer module entries must be `.module` instances. If the component exposes a `size` variant, the sync sets it to `small`; the deleted `big` variant is no longer used. The sync updates the `.module` `name` variant instead of editing inner text overrides directly.
- Create missing `.tree node` instances by cloning a compatible existing node from the same visual section, then applying the generated model values.
- Create missing top-level tree sections when a new top-level library group or plugin id appears.
- Create missing `simple-solid_arrow` connectors by cloning an existing connector and rebinding its endpoints to the parent/child tree nodes.
- Re-layout synced tree nodes so each parent is horizontally centered over its children, with 128 px between a parent bottom edge and its child top edge.
- Resize touched tree sections and their parent sections to fit after visual updates.
- Remove stale catalog tree nodes and their connectors when they no longer exist in the generated model.
- Fail without writing metadata if an existing or cloned instance cannot represent the artifact text or `.module` consumer structure from the generated model.

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

- Reconcile stale catalog tree nodes after the create/update flows are stable.
- Introduce stable Figma path metadata so duplicate labels and stale tree nodes can be reconciled safely.

### Visual Sync Targets

Use `content.catalogs` from `design-model.json` as the source for the MCP visual sync. Module dependency graphs are documented through PlantUML architecture diagrams instead of the previous Figma module dependency sections.

Water My Plants catalog targets:

- Libraries source: `build-logic/dependencies/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt`
- Libraries visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-629`
- Plugins source: `build-logic/dependencies/src/main/kotlin/com/marmatsan/dependencies/PluginTrees.kt`
- Plugins visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-594`
- Custom Gradle convention plugin source: `build-logic/**/build.gradle.kts`
- Custom Gradle convention plugin visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63216-6907`
- Custom Gradle plugin source: `build-logic/**/build.gradle.kts`
- Custom Gradle plugin visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63330-551`

Build-logic catalog targets:

- Source: `build-logic/settings.gradle.kts`
- Libraries visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63099-951`
- Plugins visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63100-2952`

UML documentation target:

- Page: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63308-2386`
- Source: PlantUML files such as `build-logic/figmaDesignSync/docs/uml/diagrams/architecture.puml`
- Rendered upload artifact: the generated `.svg` for each `.puml`
- Figma section name: the `.puml` file stem, for example `architecture`
- Visual structure: the generated SVG is placed directly in the Figma section and the section is locked after publication

Visual rendering rules:

- Use `.tree node` component `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-678`.
- Use the `Library` variant for libraries.
- Use the `Plugin` variant for plugins.
- Connect parent/child tree nodes with `simple-solid_arrow` connectors.
- Keep synced parent nodes centered over their children and use 128 px vertical parent-child spacing.
- Apply Resize to fit to every section touched by node movement or creation, including child sections and their parent sections.
- Write metadata only after all visual updates complete successfully.

## Verification

The verification task reads Figma shared plugin data through the Figma REST API and compares it with a freshly generated model from the current branch:

```powershell
$line = Get-Content -Path .env | Where-Object { $_ -like 'FIGMA_FILE_CONTENT_ACCESS_TOKEN=*' } | Select-Object -First 1
$env:FIGMA_FILE_CONTENT_ACCESS_TOKEN = ($line -split '=', 2)[1].Trim('"')
.\gradlew.bat checkFigmaTrunkSync
```

The token must have read access to the Figma file and the `file_content:read` scope.

The check fails when:

- Figma does not expose shared plugin data for `water_my_plants_sync`.
- Figma is missing `modelHash` or `gitSha`.
- Figma's `modelHash` differs from the model generated from the current branch.
- The visual MCP sync step refused to write metadata because Figma was missing required visual variables, sections, instances, or connector templates.

`checkFigmaTrunkSync` verifies the sync metadata hash, not every visual node. Manual edits in Figma can go undetected if they do not update or remove the shared plugin metadata. The visual MCP sync step is responsible for updating or recreating supported visual elements before writing the hash.

## TeamCity Integration

TeamCity should generate `design-model.json` after a feature is merged into `main` and publish it as a build artifact.

Recommended build steps for `main`:

- Run normal verification: unit, integration, and end-to-end tests where available.
- Run `generateFigmaDesignModel`.
- Publish `build/reports/figma-sync/design-model.json`.

The Figma write step is currently MCP-operated. After the MCP sync step writes the metadata into Figma, run:

```powershell
.\gradlew.bat checkFigmaTrunkSync
```

TeamCity parameter setup for verification:

- Secure parameter: `figma.file.content.access.token`
- Environment parameter: `env.FIGMA_FILE_CONTENT_ACCESS_TOKEN=%figma.file.content.access.token%`

## Release Barrier

Before creating `release/<version>`:

- `main` must pass the normal build and test suite.
- `generateFigmaDesignModel` must produce the current model.
- Figma must be synced through the MCP step.
- `checkFigmaTrunkSync` must pass.

Only after that barrier is green:

- Create `release/<version>`.
- Stabilize the release branch.
- Merge `release/<version>` into `main`.
- Publish the stable version.

After committing changes to this flow, regenerate the model and sync Figma again because `gitSha` changes with the commit.
