# Figma Trunk Sync Runbook

## Purpose

`main` is the source of truth for the dependency design model. Figma is considered synchronized only when the configured Figma page stores the same `modelHash` generated from `main`.

This replaces field-by-field Figma checks. The repository generates a single JSON model, the Figma MCP sync step updates the Figma visualization, then writes sync metadata, and Gradle verifies that Figma points to the same model.

## CI Ownership

`figmaDesignSync` is split between pull request validation and post-merge Figma verification. Developers may run it locally for diagnosis, but TeamCity is the source of truth for the repository workflow.

In trunk-based development, pull requests target `main` and CI must verify that the design model can be generated from the branch being validated. CI does not require Figma to already reflect a temporary branch. After the pull request is merged, CI runs on `main`; once that `main` CI run succeeds, the post-merge Figma pipeline generates the model from `main`, the MCP-operated sync updates Figma, and `checkFigmaTrunkSync` verifies the metadata.

Local execution is useful when diagnosing a failed gate or checking credentials, but it is not a required manual step before every commit because Figma sync depends on external Figma state, access tokens, and the MCP-operated write flow.

## Sources

- Figma page URL: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62934-908`
- Figma file key: `YBZXsd8oyGLbcI2KWxJvRK`
- Figma metadata page node id: `62934:908`
- Shared plugin data namespace: `water_my_plants_sync`
- Generated model: `build/reports/figma-sync/design-model.json`
- Repository versions file: `repo/dependency-catalog/versions.properties`
- Figma versions section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62936-183`
- Figma versions variable collection: `repo\dependency-catalog\versions.properties`
- Figma Water My Plants libraries section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-629`
- Figma Water My Plants plugins section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-594`
- Figma custom Gradle convention plugins section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63216-6907`
- Figma custom Gradle plugins section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63330-551`
- Figma gradle-plugins libraries section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63099-951`
- Figma gradle-plugins plugins section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63100-2952`
- Figma figma-design-sync libraries section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63573-260`
- Figma figma-design-sync plugins section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63573-346`
- Figma UML documentation page: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63308-2386`
- Root settings file: `settings.gradle.kts`
- Default included build names: `dependency-catalog`, `figma-design-sync`, `gradle-plugins`
- Default included build model names: `dependencyCatalog`, `figmaDesignSync`, `gradlePlugins`

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

`content.versions` is the sorted flat map used for deterministic comparison. `content.versionSections` preserves the section grouping from `repo/dependency-catalog/versions.properties` so the Figma MCP sync can place new visual version nodes in the correct frame.

`content.catalogs` contains Water My Plants libraries/plugins, catalogs from included builds that publish settings catalogs, custom Gradle convention plugins, and regular custom Gradle plugins. The default settings-catalog contributors are `gradlePlugins` and `figmaDesignSync`. `dependencyCatalog` contributes modules and module dependencies but not a `content.catalogs.dependencyCatalog` entry because `repo/dependency-catalog/settings.gradle.kts` does not declare `versionCatalogs.create("libs")` or `versionCatalogs.create("plugins")`.

Custom Gradle convention plugins are detected from `repo/gradle-plugins` build files that declare an implementation class ending in `GradleConventionPlugin`. Regular custom Gradle plugins are detected from repository included builds that declare a Gradle plugin implementation class that does not end in `GradleConventionPlugin` and whose plugin id is applied from the main build.

`com.marmatsan.figmaDesignSync` is a regular Gradle plugin, not a Gradle convention plugin. It is intentionally excluded from `content.catalogs.waterMyPlants.customGradleConventionPlugins` and included in `content.catalogs.waterMyPlants.customGradlePlugins` because it registers sync tasks and extension configuration instead of applying build conventions to consumer modules. Applying it in the root build does not create a consumer module entry because `:` is not treated as an application module.

Included builds are configured through `figmaDesignSync.includedBuilds`. The defaults model all repository tooling under `repo/`:

```kotlin
figmaDesignSync {
    includedBuilds.named("dependency-catalog") {
        modelName.set("dependencyCatalog")
        rootDirectory.set(layout.projectDirectory.dir("repo/dependency-catalog"))
        settingsFile.set(layout.projectDirectory.file("repo/dependency-catalog/settings.gradle.kts"))
        modulePathPrefix.set(":dependency-catalog")
        publishesCatalogs.set(false)
    }

    includedBuilds.named("figma-design-sync") {
        modelName.set("figmaDesignSync")
        rootDirectory.set(layout.projectDirectory.dir("repo/figma-design-sync"))
        settingsFile.set(layout.projectDirectory.file("repo/figma-design-sync/settings.gradle.kts"))
        modulePathPrefix.set(":figma-design-sync")
    }

    includedBuilds.named("gradle-plugins") {
        modelName.set("gradlePlugins")
        rootDirectory.set(layout.projectDirectory.dir("repo/gradle-plugins"))
        settingsFile.set(layout.projectDirectory.file("repo/gradle-plugins/settings.gradle.kts"))
        modulePathPrefix.set(":gradle-plugins")
        publishesCatalogs.set(true)
        publishesConventionPlugins.set(true)
    }
}
```

`content.moduleDependencies` contains one graph for the root build and one graph per configured included build:

- `main`: root project modules outside nested Gradle builds, read from `dependencies {}` blocks in root project module `build.gradle.kts` files.
- `dependencyCatalog`: the standalone root module at `repo/dependency-catalog`; this graph is usually empty because the build has no project-to-project dependencies.
- `figmaDesignSync`: included-build modules inside `repo/figma-design-sync`, read from `dependencies {}` blocks in that included build.
- `gradlePlugins`: included-build modules inside `repo/gradle-plugins`, read from `dependencies {}` blocks in that included build.

Module dependency extraction reads explicit `project(":...")` calls and type-safe project accessors such as `projects.core.ui` and `projects.domain`.

Module dependency graphs are no longer rendered into Figma's deleted `dependency of modules` section. They are architecture documentation and must be represented in PlantUML diagrams under the repository UML convention, then published to the Figma UML documentation page.

`modelHash` is calculated from the stable model content and excludes `branch`, `gitSha`, `generatedAt`, and `modelHash` itself. `branch` and `gitSha` remain in the JSON as traceability metadata, but they do not force a Figma sync when the visual model content has not changed.

## Figma MCP Sync

Use the Figma MCP `use_figma` tool to update the visual Figma model and then write the generated metadata to the configured page node.

The version visual sync is implemented in TypeScript modules under:

- `repo/figma-design-sync/tools/src/`

The tool source follows the same dependency direction as the Gradle sync code: `domain` contains generated-model types and pure catalog rules, `ports` contains gateway contracts, `usecases` coordinates sync behavior through those contracts, `figma` contains the Figma MCP API adapters, and `app` wires the concrete gateways for the generated MCP entrypoint.

The TypeScript files under `tools/src/` are the source of truth. The Figma MCP
runtime executes JavaScript, so build a temporary JavaScript artifact after
editing the TypeScript source:

```powershell
cd repo\figma-design-sync\tools
npm ci
npm run build
```

The generated script is:

- `repo/figma-design-sync/tools/sync-trunk-design-model.mcp.js`

This JavaScript file is generated output and must not be committed. The script
expects the generated `design-model.json` to be injected as `DESIGN_MODEL`
before execution. It must run in the Figma MCP runtime because it uses the Figma
plugin API.

### MCP Payload Transport

Keep the MCP sync source in TypeScript. JavaScript exists only as the generated
runtime artifact that Figma executes, and it stays out of Git.

The Figma MCP `use_figma` call has a practical source-size limit near 50k
characters. The plugin runtime supports `atob`, `btoa`, and `Function`, but it
does not provide browser or Node transfer helpers such as `fetch`,
`XMLHttpRequest`, `importScripts`, `TextDecoder`, `Blob`, `Response`, or
`DecompressionStream`. Do not design the sync handoff around those APIs.

When the generated script plus injected model is small enough, pass it as
base64 split into string chunks inside one `use_figma` call:

- Join the chunks in Figma with `chunks.join("")`.
- Validate the joined encoded length before decoding.
- Decode with `atob`.
- Execute with `new Function("figma", source)` or an equivalent wrapper.
- Read back the returned metadata and verify the expected `modelHash`.

When the payload approaches the call limit, stage it in shared plugin data using
a temporary namespace and several small `use_figma` calls:

- Generate the chunks mechanically from the local artifact.
- Store `runId`, expected chunk count, expected encoded length, and every chunk.
- In the final call, read all chunks, validate count and length, decode, run the
  script, verify the returned `modelHash`, then delete the temporary shared
  plugin data.
- If the final call fails before execution, Figma visuals stay unchanged; only
  temporary staged chunk data may need cleanup.

Do not copy long base64 payloads manually from terminal output. One failed sync
attempt was caused by a truncated payload: the expected encoded length was
`45228`, but only `44624` characters reached Figma. The runner must fail on
length mismatch before it mutates the file.

Current version sync behavior:

- Read `content.versionSections` from `build/reports/figma-sync/design-model.json`.
- Find Figma variable collection `repo\dependency-catalog\versions.properties`.
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
- Update existing `.tree node` instances in the gradle-plugins library/plugin sections.
- Update existing `.tree node` instances in the figma-design-sync library/plugin sections.
- Match existing tree nodes by label inside each visual section. Labels must be unique per section until stable Figma path metadata is introduced.
- Update exposed component properties for library groups, plugin ids, plugin versions, and artifact visibility.
- Update existing library artifact name/version text overrides when the existing instance structure can represent the model.
- Update `Required by` module instances for library artifacts from `requiredByModules`.
- Update `Required by` module instances for `.artifacts bundle` entries from the bundle `requiredByModules`; child `.artifact` instances inside a bundle must not show their own `Required by` section.
- Update `Applied by` module instances for plugin tree nodes from `appliedToModules`.
- Consumer module entries must be `.module` instances. If the component exposes a `size` variant, the sync sets it to `small`; the deleted `big` variant is no longer used. The sync updates the `.module` `name` variant instead of editing inner text overrides directly.
- Create missing `.tree node` instances by cloning a compatible existing node from the same visual section, then applying the generated model values.
- Create missing top-level tree sections when a new top-level library group or plugin id appears.
- Create missing `simple-solid_arrow` connectors by cloning an existing connector and binding its endpoints to the parent/child tree nodes.
- Rebind existing synced connector endpoints after layout so connectors follow moved tree nodes.
- Re-layout synced tree nodes so each parent is horizontally centered over its children, with 128 px between a parent bottom edge and its child top edge.
- Resize touched tree sections and their parent sections to fit after visual updates, including direct `.Header` instances that must span the section width.
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

The next visual sync phase is to introduce stable Figma path metadata so duplicate labels can be reconciled safely.

### Visual Sync Targets

Use `content.catalogs` from `design-model.json` as the source for the MCP visual sync. Module dependency graphs are documented through PlantUML architecture diagrams instead of the previous Figma module dependency sections.

Water My Plants catalog targets:

- Libraries source: `repo/dependency-catalog/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt`
- Libraries visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-629`
- Plugins source: `repo/dependency-catalog/src/main/kotlin/com/marmatsan/dependencies/PluginTrees.kt`
- Plugins visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-594`
- Custom Gradle convention plugin source: `repo/gradle-plugins/**/build.gradle.kts`
- Custom Gradle convention plugin visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63216-6907`
- Custom Gradle plugin source: repository included-build `**/build.gradle.kts` files that declare regular Gradle plugins
- Custom Gradle plugin visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63330-551`

Included-build catalog targets:

- Source: `repo/gradle-plugins/settings.gradle.kts`
- Libraries visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63099-951`
- Plugins visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63100-2952`
- Source: `repo/figma-design-sync/settings.gradle.kts`
- Libraries visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63573-260`
- Plugins visual section: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63573-346`
- `repo/dependency-catalog` has no settings-catalog visual target; it contributes the versions file and the standalone `:dependency-catalog` module.

UML documentation target:

- Page: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63308-2386`
- Source: PlantUML files such as `repo/figma-design-sync/docs/uml/diagrams/architecture.puml`
- Rendered upload artifact: the generated `.svg` for each `.puml`
- Module grouping section: `figmaDesignSync`
- Diagram section name: the full `.puml` file name, for example `architecture.puml`
- Diagram section style: no fill, `md/sys/color/outline` stroke, stroke align `INSIDE`, stroke weight `2`
- Visual structure: each generated SVG is imported into its own diagram section, then flattened so only the imported `Group` remains inside the section. Center that `Group` horizontally and lock the section after publication.
- `.Header` links: the visible `Link` property must list the relevant repository files, and each filename must hyperlink to its GitHub `main` branch URL. When multiple files are listed, apply the URL to each filename range and keep separators unlinked.

Visual rendering rules:

- Use `.tree node` component `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-678`.
- Use the `Library` variant for libraries.
- Use the `Plugin` variant for plugins.
- Connect parent/child tree nodes with `simple-solid_arrow` connectors.
- Keep synced parent nodes centered over their children and use 128 px vertical parent-child spacing.
- Apply Resize to fit to every section touched by node movement or creation, including child sections and their parent sections.
- Write metadata only after all visual updates complete successfully.

### Failure Recovery Notes

The sync writes metadata last on purpose. `checkFigmaTrunkSync` trusts the
metadata hash, so writing metadata before visuals are reconciled can make CI pass
while Figma is still visually stale. A metadata-only write is acceptable only
when the visual model is already known to match `design-model.json` and the last
remaining mismatch is stale shared plugin metadata.

Figma component instances can contain hidden template internals that remain
addressable through the plugin API. Do not treat exact child counts as a stable
contract for reused instances. For example, library artifact text updates must
allow extra hidden `artifact name` and `artifact version` text nodes and update
only the expected visible entries. The failure signature is similar to:

```text
expected 0 'artifact name' text nodes, found 8
```

Cloned tree nodes and cloned connectors must be made visible before connector
endpoints are rebound. Otherwise Figma can reject connector assignment with:

```text
set_connectorStart: Connecting to this node type is not supported
```

When connector creation fails, report the parent and child node ids in the
diagnostic path before writing metadata. That keeps the failed visual relation
actionable instead of reducing the problem to a generic Figma API error.

## Verification

The verification task reads Figma shared plugin data through the Figma REST API and compares it with a freshly generated model from the current checkout. In TeamCity, this check belongs to the post-merge `main` workflow:

```powershell
$line = Get-Content -Path .env | Where-Object { $_ -like 'FIGMA_FILE_CONTENT_ACCESS_TOKEN=*' } | Select-Object -First 1
$env:FIGMA_FILE_CONTENT_ACCESS_TOKEN = ($line -split '=', 2)[1].Trim('"')
.\gradlew.bat checkFigmaTrunkSync
```

The token must have read access to the Figma file and the `file_content:read` scope.

The check fails when:

- Figma does not expose shared plugin data for `water_my_plants_sync`.
- Figma is missing `modelHash` or `gitSha`.
- Figma's `modelHash` differs from the model generated from the current checkout.
- The visual MCP sync step refused to write metadata because Figma was missing required visual variables, sections, instances, or connector templates.

`checkFigmaTrunkSync` verifies the sync metadata hash, not every visual node. Manual edits in Figma can go undetected if they do not update or remove the shared plugin metadata. The visual MCP sync step is responsible for updating or recreating supported visual elements before writing the hash.

This means the check contract is intentionally narrow:

- Generate the model from the current checkout.
- Read `water_my_plants_sync.modelHash` from the configured Figma page.
- Pass only when both hashes are identical.

It does not prove that every `.tree node`, connector, variable, or UML section is
visually correct. Those are obligations of the MCP write step and the UML
publication runbook. If a visual mutation fails, leave the old hash in Figma and
let `checkFigmaTrunkSync` fail until the visual sync can be rerun successfully.

## TeamCity Integration

TeamCity uses two separate pipelines for this workflow.

`CI` is the pull request gate required by GitHub branch protection:

- Run normal verification: unit, integration, and end-to-end tests where available.
- Run `generateFigmaDesignModel`.
- Publish `build/reports/figma-sync/design-model.json`.

`CI` does not run `checkFigmaTrunkSync`. Figma represents `main`, not every short-lived branch.

`Figma Sync` is the post-merge pipeline for `main`:

- Trigger after the `CI` pipeline succeeds on `<default>`.
- Run `generateFigmaDesignModel` from `main`.
- Publish `build/reports/figma-sync/design-model.json`.
- Run `checkFigmaTrunkSync` against the metadata currently stored in Figma.
- Verify every added or changed `.puml` diagram has been rendered and published
  to the Figma UML documentation page in its own locked section.

The Figma write step is currently MCP-operated. Until it is automated inside TeamCity, the first `Figma Sync` run after a model-affecting merge can identify that `main` is not yet reflected in Figma, but the actual write still happens through the MCP step. Use the published `design-model.json` artifact to run the Figma MCP visual sync. After the MCP sync step writes the metadata into Figma, rerun `Figma Sync` or run:

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
- Every `.puml` diagram added or changed on `main` must already live in the
  Figma UML documentation page.
- `checkFigmaTrunkSync` must pass.

Only after that barrier is green:

- Create `release/<version>`.
- Stabilize the release branch.
- Merge `release/<version>` into `main`.
- Publish the stable version.

After merging changes that affect the generated model content, regenerate the model from `main` and sync Figma again. Commits that only change traceability metadata, CI settings, or unrelated files do not require a Figma sync if `modelHash` stays unchanged.
