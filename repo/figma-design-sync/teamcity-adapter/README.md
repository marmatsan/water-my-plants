# TeamCity CI Adapter

`teamcity-adapter` is the optional Kotlin boundary between generated TeamCity
configuration and the portable `figma-design-sync` CI model. It owns every
TeamCity YAML/XML parsing rule required to populate pipelines, jobs, triggers,
artifacts, published checks, dependencies, and VCS roots.

It also exposes `TeamCityCliClient`, the typed CLI boundary used by
project-config operational tasks to inspect and download successful TeamCity
artifact sets without PowerShell orchestration.

The public entry point is `TeamCityCiConfigurationProvider`. A project selects
that class through `figmaDesignSync.ciConfigurationProviderClassName`, supplies
the generated configuration directory, and chooses the stable JSON key used
under `content.ci`. Water My Plants uses `teamCity`.

Dependency direction remains one-way:

```text
teamcity-adapter -> data -> domain
```

The portable `domain`, `data`, and `plugin` modules do not depend on this
module. A project using another CI system can provide a sibling adapter that
implements `CiConfigurationProvider`, or disable CI documentation entirely.

Verify the adapter from the repository root:

```powershell
.\gradlew.bat :figma-design-sync:teamcity-adapter:check
```
