# Figma Develop Sync

## Purpose

`develop` is the source of truth for the dependency design model. Figma is considered synchronized only when the configured Figma page stores the same `modelHash` generated from the current branch.

This replaces field-by-field Figma checks. The repository generates a single JSON model, the Figma MCP sync step writes its metadata into Figma, and Gradle verifies that Figma points to the same model.

## Sources

- Figma page URL: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62934-908`
- Figma file key: `YBZXsd8oyGLbcI2KWxJvRK`
- Figma metadata page node id: `62934:908`
- Shared plugin data namespace: `water_my_plants_sync`
- Generated model: `build/reports/figma-sync/design-model.json`
- Repository versions file: `build-logic/versions.properties`
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

`modelHash` is calculated from the stable model content and excludes `generatedAt` and `modelHash` itself.

## Figma MCP Sync

Use the Figma MCP `use_figma` tool to write the generated metadata to the configured page node.

Read these values from `build/reports/figma-sync/design-model.json`:

- `schemaVersion`
- `branch`
- `gitSha`
- `modelHash`

Then run this JavaScript through Figma MCP against file key `YBZXsd8oyGLbcI2KWxJvRK`:

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
