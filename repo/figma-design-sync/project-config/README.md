# Figma Design Sync Project Configuration

`project-config` contains repository adapters for the portable
`figma-design-sync` engine. The reusable contract, Kotlin implementation,
TypeScript writer, and their tests live in sibling directories and must not
contain Water My Plants file identities, Figma node ids, catalog names, or CI
commands.

## Water My Plants Adapter

The active adapter consists of:

- `src/main/kotlin/.../WaterMyPlantsFigmaDesignSyncGradlePlugin.kt`, which
  applies `com.marmatsan.figmaDesignSync` and supplies repository paths, Figma
  metadata identity, included builds, the TeamCity adapter class, its
  `teamCity` model key, and the optional TeamCity generation command;
- `../teamcity-adapter/src/main/kotlin/.../TeamCityCiConfigurationProvider.kt`,
  which translates TeamCity's generated YAML/XML into the portable CI model;
- `src/main/kotlin/.../WaterMyPlantsDependencyCatalogProvider.kt`, which adapts
  the concrete `WaterMyPlantsCatalog` to the portable catalog provider;
- `src/main/kotlin/.../WaterMyPlantsFigmaWriterProjectConfig.kt`, which owns
  Figma file identity, node ids, component properties, GitHub links, visual
  targets, and MCP namespaces as a typed Kotlin value;
- `water-my-plants/figma-config.ts`, retained temporarily as the executable
  parity oracle while the remaining writer implementation is migrated;
- `water-my-plants/change-impact-policy.json`, which owns path classification
  for this repository layout.

The root build applies the adapter plugin:

```kotlin
plugins {
    id("com.marmatsan.waterMyPlantsFigmaDesignSync")
}
```

The adapter then applies the reusable `com.marmatsan.figmaDesignSync` plugin.

## Reusing The Engine

A new Gradle repository reuses `domain/`, `data/`, `plugin/`, `tools/`, and
their tests. It reuses `teamcity-adapter/` only when TeamCity is its CI system,
and supplies a new project-config adapter that:

1. implements `DependencyCatalogProvider` for its dependency source;
2. applies and configures `com.marmatsan.figmaDesignSync`;
3. declares its primary catalog model name and included builds;
4. provides its Figma metadata URL, namespace, and typed
   `FigmaWriterProjectConfig`;
5. provides a change-impact policy for its repository paths;
6. optionally selects a `CiConfigurationProvider`, its stable JSON model key,
   generated configuration directory, default-branch alias, and materializing
   command.

The reusable plugin defaults `ciDocumentationEnabled` to `false`. A project
that does not enable it needs no generated CI directory, provider, topology
files, or materialization command; its generated model simply omits
`content.ci`. Water My Plants enables the flag and selects
`TeamCityCiConfigurationProvider` from Kotlin project configuration. The
portable plugin knows only `CiConfigurationProvider`, not TeamCity file
formats or branch aliases.

This portability boundary is executable. The Gradle integration suite applies
the reusable plugin with its default CI setting, removes both `docs/ci` and
`.teamcity` from the fixture, generates the design model, and verifies that the
portable content is present while `content.ci` is absent. Production source and
KDoc outside `project-config` describe the host repository through adapter
contracts rather than Water My Plants paths or identities.

The preferred writer selection boundary is the `--project-config-json` input
of `figma-design-sync-build`. `WriteFigmaWriterProjectConfigTask` serializes the
portable Kotlin model through `FigmaWriterProjectConfigJson` to
`build/generated/figma-design-sync/writer-project-config.json`; the official
Gradle task consumes that transient file automatically. The config declares
its repository root and change-impact policy paths explicitly, so a published
package does not assume the Water My Plants layout. `--project-config` remains
available only as a transitional TypeScript compatibility input.

The `test:project-config-parity` test generates the Kotlin JSON and compares
every projected export with `water-my-plants/figma-config.ts`, including the
catalog target model paths. A TypeScript config value cannot drift silently
while both representations coexist.

After publication, repositories consume the engine through the versioned
`com.marmatsan.figmaDesignSync` plugin and keep only their adapter in source.
See the [adoption guide](../docs/guides/adopting-figma-design-sync.md) for that
workflow. The current Water My Plants build continues to use `includeBuild`
while developing the engine itself.

## Optional Operational Adapters

TeamCity is not required by the portable Kotlin plugin or writer. Water My
Plants supplies its TeamCity command through the Kotlin project-config plugin
and invokes the portable Gradle tasks from `.teamcity/settings.kts`.

The Kotlin `prepareTeamCityFigmaSyncHandoff` task owns TeamCity artifact
inspection, download, contract validation, and executor preparation. The
Kotlin `rerunTeamCityFigmaSync` task owns Cloudflare token exchange, active-run
deduplication, queueing, and optional waiting. It obtains credentials through
`TeamCityAutomationCredentialsProvider`; the default environment adapter keeps
PowerShell SecretStore and other workstation-specific vaults outside the
module. No Figma synchronization workflow depends on `tools/teamcity/`.

The rerun task accepts these Gradle properties:

| Property | Default | Meaning |
|----------|---------|---------|
| `figmaTeamCityServerUrl` | `https://teamcity.marmatsan.dev` | Public HTTPS TeamCity endpoint. |
| `figmaTeamCityValidateOnly` | `false` | Validate both authentication layers without queueing. |
| `figmaTeamCityWait` | `false` | Wait for the reused or queued run and require success. |
| `figmaTeamCityPollIntervalSeconds` | `10` | TeamCity CLI watch interval. |
| `figmaTeamCityTimeoutMinutes` | `60` | Maximum TeamCity CLI watch duration. |

Credentials enter only through `TEAMCITY_TOKEN` plus either a short-lived
`TEAMCITY_HEADER_CF_ACCESS_TOKEN` or the
`TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID` and
`TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET` pair. The task exchanges the pair for
the short-lived token before starting the CLI child process and never logs the
values.

## Verification

From the repository root:

```powershell
.\gradlew.bat :figma-design-sync:domain:check `
    :figma-design-sync:data:check `
    :figma-design-sync:teamcity-adapter:check `
    :figma-design-sync:plugin:check `
    :figma-design-sync:project-config:check

.\gradlew.bat writeFigmaWriterProjectConfig

Push-Location repo\figma-design-sync\tools
npm test
npm run build
Pop-Location
```
