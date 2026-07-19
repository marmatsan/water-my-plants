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
- `water-my-plants/change-impact-policy.json`, which owns path classification
  for this repository layout. Its model-neutral entries name only existing
  host adapters; replacing an adapter requires removing the obsolete path so
  the policy cannot silently preserve a deleted integration.

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

The writer selection boundary is the `--project-config-json` input
of `figma-design-sync-build`. `WriteFigmaWriterProjectConfigTask` serializes the
portable Kotlin model through `FigmaWriterProjectConfigJson` to
`build/generated/figma-design-sync/writer-project-config.json`; the official
Gradle task consumes that transient file automatically. The config declares
its repository root and change-impact policy paths explicitly, so a published
package does not assume the Water My Plants layout. The TypeScript build
materializes an internal module from this JSON; there is no second editable
Water My Plants configuration.

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
Kotlin `uploadOfficialFigmaPayload` task accepts only a successful main
`Generate main design model` build, verifies the manifest-declared PNG bytes,
and posts them only to the exact single-use HTTPS endpoint returned by Figma
`upload_assets`. It runs the TeamCity CLI from the repository root so the
versioned `teamcity.toml` connection is authoritative. Its URL is internal task
state and the task never logs or writes it. The
Kotlin `rerunTeamCityFigmaSync` task owns Cloudflare token exchange, active-run
deduplication, queueing, and optional waiting. It obtains credentials through
`TeamCityAutomationCredentialsProvider`; the default environment adapter keeps
PowerShell SecretStore and other workstation-specific vaults outside the
module. Read-only run discovery and waiting use TeamCity CLI. Queueing uses the
cookie-free Kotlin REST adapter so Bearer-authenticated POST requests do not
enter TeamCity's CSRF session flow. No Figma synchronization workflow depends
on `tools/teamcity/`.

The upload task accepts these Gradle properties:

| Property | Default | Meaning |
|----------|---------|---------|
| `figmaTeamCityBuildId` | none | Required successful main `Generate main design model` job id. |
| `figmaArtifactDirectory` | none | Alternative validated official artifact directory; mutually exclusive with the build id. |
| `figmaMcpUploadUrl` | none | Required single-use `mcp.figma.com` PNG submit URL returned by `upload_assets`. |
| `figmaExpectedGitSha` | none | Exact revision expected in the official artifact contract; required with `figmaArtifactDirectory`. |
| `figmaHandoffDestinationRoot` | `tmp/teamcity` | Ignored directory used for the validated TeamCity download. |

When the workstation supports reusable command approvals, scope the standing
permission to `.\gradlew.bat uploadOfficialFigmaPayload`. Do not grant a
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
.\gradlew.bat :figma-design-sync:domain:check `
    :figma-design-sync:data:check `
    :figma-design-sync:teamcity-adapter:check `
    :figma-design-sync:plugin:check `
    :figma-design-sync:project-config:check

.\gradlew.bat testFigmaDesignSyncTools buildFigmaDesignSyncTools
```
