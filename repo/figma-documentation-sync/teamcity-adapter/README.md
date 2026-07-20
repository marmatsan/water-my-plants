# TeamCity CI Adapter

`teamcity-adapter` is the optional Kotlin boundary between generated TeamCity
configuration and the portable `figma-documentation-sync` CI model. It owns every
TeamCity YAML/XML parsing rule required to populate pipelines, jobs, triggers,
artifacts, dependencies, and VCS roots. Repository status publication is a
native TeamCity Pipelines repository integration and is therefore not encoded
as a generated job feature or parsed by this adapter.

It also exposes typed operational boundaries without PowerShell orchestration.
`TeamCityCliClient` inspects and downloads successful TeamCity artifact sets,
finds active runs, and waits for their results. `TeamCityRestRunStarter` queues
a run through a cookie-free Bearer request, while `TeamCityCompositeRunClient`
combines those read and write transports. Authentication is supplied at the
adapter boundary so this module does not own a particular secret store.

The public entry point is `TeamCityCiConfigurationProvider`. A project selects
that class through `figmaDocumentationSync.ciConfigurationProviderClassName`, supplies
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
.\gradlew.bat :figma-documentation-sync:teamcity-adapter:check
```
