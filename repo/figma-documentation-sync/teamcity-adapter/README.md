# TeamCity CI Adapter

`teamcity-adapter` is the optional Kotlin boundary between generated TeamCity
configuration and the portable `figma-documentation-sync` CI model. It owns every
TeamCity YAML/XML parsing rule required to populate pipelines, jobs, triggers,
artifacts, dependencies, and VCS roots. It also reads versioned classic status
gates: a gate's VCS trigger and Commit Status Publisher are mapped onto the
Pipeline selected by its snapshot dependency. This preserves the effective CI
contract without encoding `commit-status-publisher` as an unsupported Pipeline
YAML job feature.

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
The optional [`teamcity-operations`](../teamcity-operations/README.md) plugin
consumes these typed clients to expose supervised handoff, upload, and rerun
tasks without moving TeamCity knowledge into the portable plugin.

Verify the adapter from the repository root:

```powershell
.\gradlew.bat :figma-documentation-sync:teamcity-adapter:check
```
