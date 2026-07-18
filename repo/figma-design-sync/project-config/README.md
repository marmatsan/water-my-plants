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
- `water-my-plants/figma-config.ts`, which owns Figma file identity, node ids,
  component properties, GitHub links, visual targets, and MCP namespaces;
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
4. provides its Figma metadata URL, namespace, and TypeScript config module;
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

The TypeScript selection boundary is the
`@figma-design-sync/project-config` path in `tools/tsconfig.json`. Point that
alias at the new repository's config module. TypeScript remains necessary only
for code bundled into the Figma plugin/MCP runtime; repository generation,
classification, and artifact validation stay in Kotlin.

## Optional Operational Adapters

TeamCity is not required by the portable Kotlin plugin or writer. Water My
Plants supplies its TeamCity command through the Kotlin project-config plugin
and invokes the portable Gradle tasks from `.teamcity/settings.kts`.

The PowerShell files under `tools/teamcity/` are local Windows adapters for
secret retrieval, TeamCity CLI access, artifact download, and reruns. Neither
the Kotlin modules nor the TypeScript writer imports or invokes them.

## Verification

From the repository root:

```powershell
.\gradlew.bat :figma-design-sync:domain:check `
    :figma-design-sync:data:check `
    :figma-design-sync:teamcity-adapter:check `
    :figma-design-sync:plugin:check `
    :figma-design-sync:project-config:check

Push-Location repo\figma-design-sync\tools
npm test
npm run build
Pop-Location
```
