# Water My Plants Project Configuration

This included build is the Water My Plants composition root. It owns the
product dependency catalog, product versions, Figma/TeamCity identities, and
the adapters that connect reusable repository tooling. Reusable sibling builds
must not contain these paths, ids, or concrete implementations.

## Water My Plants Adapter

The active adapter consists of:

- `plugin/src/main/kotlin/.../WaterMyPlantsProjectConfigPlugin.kt`, which
  acts only as the composition entry point and delegates product wiring;
- `plugin/src/main/kotlin/.../WaterMyPlantsFigmaExtensionConfigurator.kt`,
  which supplies repository paths, Figma metadata identity, included builds,
  the TeamCity adapter class, its `teamCity` model key, and the optional
  TeamCity generation command;
- `plugin/src/main/kotlin/.../WaterMyPlantsFigmaWriterTasksRegistrar.kt` and
  `WaterMyPlantsTeamCityFigmaTasksRegistrar.kt`, which independently own writer
  task bindings and supervised TeamCity operations;
- `../figma-documentation-sync/teamcity-adapter/src/main/kotlin/.../TeamCityCiConfigurationProvider.kt`,
  which translates TeamCity's generated YAML/XML into the portable CI model;
- `catalog/src/main/kotlin/.../WaterMyPlantsCatalogProvider.kt`, which adapts
  the concrete product trees to Dependency Catalog's public API;
- `plugin/src/main/kotlin/.../WaterMyPlantsDependencyDslCatalogProvider.kt`,
  which composes consumer-specific mapping, usage-source, and enrichment ports
  before exposing Figma's catalog input port;
- `plugin/src/main/kotlin/.../WaterMyPlantsFigmaWriterProjectConfig.kt`, which owns
  Figma file identity, node ids, component properties, GitHub links, visual
  targets, and MCP namespaces as a typed Kotlin value;
- `water-my-plants/change-impact-policy.json`, which owns path classification
  for this repository layout. Its model-neutral entries name only existing
  host adapters; replacing an adapter requires removing the obsolete path so
  the policy cannot silently preserve a deleted integration.

The root build applies the adapter plugin:

```kotlin
plugins {
    id("com.marmatsan.waterMyPlantsProjectConfig")
}
```

The adapter then applies the reusable `com.marmatsan.figmaDocumentationSync` plugin.

## Reusing The Engine

A new Gradle repository reuses `domain/`, `data/`, `plugin/`, `tools/`, and
their tests. It reuses `teamcity-adapter/` only when TeamCity is its CI system,
and supplies a new project-config adapter that:

1. implements Dependency Catalog's resolved provider for Gradle and its
   version-aliased provider when documentation needs stable aliases;
2. adapts that model to Figma's `DependencyDslCatalogProvider` when Figma
   catalog documentation is enabled;
3. applies and configures `com.marmatsan.figmaDocumentationSync`;
4. declares its primary production catalog model name and included builds;
5. provides its Figma metadata URL, namespace, and typed
   `FigmaWriterProjectConfig`;
6. provides a change-impact policy for its repository paths;
7. optionally selects a `CiConfigurationProvider`, its stable JSON model key,
   generated configuration directory, default-branch alias, and materializing
   command.

An included build does not need its own provider or `*-catalog` module merely
because it has a local `versions.properties`. Water My Plants sets
`publishesCatalogs=false` for tooling builds and configures only
`waterMyPlants.libraries` and `waterMyPlants.plugins` as visual catalog targets.

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
KDoc outside this composition build describe the host repository through adapter
contracts rather than Water My Plants paths or identities.

The writer selection boundary is the `--project-config-json` input
of `figma-documentation-sync-build`. `WriteFigmaWriterProjectConfigTask` serializes the
portable Kotlin model through `FigmaWriterProjectConfigJson` to
`build/generated/figma-documentation-sync/writer-project-config.json`; the canonical
Gradle task consumes that transient file automatically. The config declares
its repository root and change-impact policy paths explicitly, so a published
package does not assume the Water My Plants layout. The TypeScript build
materializes an internal module from this JSON; there is no second editable
Water My Plants configuration.

After publication, repositories consume the engine through the versioned
`com.marmatsan.figmaDocumentationSync` plugin and keep only their adapter in source.
See the [adoption guide](../figma-documentation-sync/docs/guides/adopting-figma-documentation-sync.md) for that
workflow. The current Water My Plants build continues to use `includeBuild`
while developing the engine itself.

## Optional Operational Adapters

TeamCity is not required by the portable Kotlin plugin or writer. Water My
Plants supplies its TeamCity command through the Kotlin project-config plugin
and invokes the portable Gradle tasks from `.teamcity/settings.kts`.

The Kotlin `prepareTeamCityFigmaSyncHandoff` task owns TeamCity artifact
inspection, download, contract validation, and executor preparation. The
Kotlin `uploadCanonicalFigmaPayload` task accepts only a successful main
`Generate main design model` build, verifies the manifest-declared PNG bytes,
and posts them only to the exact single-use HTTPS endpoint returned by Figma
`upload_assets`. It runs the TeamCity CLI from the repository root so the
versioned `teamcity.toml` connection is authoritative. Its URL is internal task
state and the task never logs or writes it. The
Kotlin `rerunTeamCityFigmaSync` task owns Cloudflare token exchange, active-run
deduplication, queueing, and optional waiting. It obtains credentials through
`TeamCityAutomationCredentialsProvider`; the default environment adapter keeps
PowerShell SecretStore and other workstation-specific vaults outside the
module. The repository-owned
`.teamcity/scripts/invoke-figma-sync-rerun.ps1` launcher is only a Windows
SecretStore bridge: it supplies the environment port for one invocation and
restores the previous process environment afterwards. Read-only run discovery
and waiting use TeamCity CLI. Queueing uses the cookie-free Kotlin REST adapter
so Bearer-authenticated POST requests do not enter TeamCity's CSRF session
flow. No Figma synchronization workflow depends on `tools/teamcity/`.

The upload task accepts these Gradle properties:

| Property | Default | Meaning |
|----------|---------|---------|
| `figmaTeamCityBuildId` | none | Required successful main `Generate main design model` job id. |
| `figmaArtifactDirectory` | none | Alternative validated canonical artifact directory; mutually exclusive with the build id. |
| `figmaMcpUploadUrl` | none | Required single-use `mcp.figma.com` PNG submit URL returned by `upload_assets`. |
| `figmaExpectedGitSha` | none | Exact revision expected in the canonical artifact contract; required with `figmaArtifactDirectory`. |
| `figmaHandoffDestinationRoot` | `tmp/teamcity` | Ignored directory used for the validated TeamCity download. |

When the workstation supports reusable command approvals, scope the standing
permission to `.\gradlew.bat uploadCanonicalFigmaPayload`. Do not grant a
generic PowerShell or arbitrary HTTP-upload permission.

The rerun task accepts these Gradle properties:

| Property | Default | Meaning |
|----------|---------|---------|
| `figmaTeamCityServerUrl` | `https://teamcity.marmatsan.dev` | Public HTTPS TeamCity endpoint. |
| `figmaTeamCityValidateOnly` | `false` | Validate both authentication layers without queueing. |
| `figmaTeamCityWait` | `false` | Wait for the reused or queued run and require success. |
| `figmaTeamCityPollIntervalSeconds` | `10` | TeamCity CLI watch interval after queueing. |
| `figmaTeamCityTimeoutMinutes` | `60` | Maximum TeamCity CLI watch duration after queueing. |

Credentials enter only through `TEAMCITY_TOKEN` plus either a short-lived
`TEAMCITY_HEADER_CF_ACCESS_TOKEN` or the
`TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID` and
`TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET` pair. The task exchanges the pair for
the short-lived token before starting either transport and never logs the
values or stores response cookies.

## Verification

From the repository root:

```powershell
.\gradlew.bat :figma-documentation-sync:domain:check `
    :figma-documentation-sync:data:check `
    :figma-documentation-sync:teamcity-adapter:check `
    :figma-documentation-sync:plugin:check

.\gradlew.bat -p repo\water-my-plants-project-config check

.\gradlew.bat testFigmaDocumentationSyncTools buildFigmaDocumentationSyncTools
```
