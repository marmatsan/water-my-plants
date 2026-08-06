# Gradle Plugins Agent Instructions

## Context

Apply the root [`AGENTS.md`](../../AGENTS.md), the
[Gradle standard](../../docs/standards/gradle.md), the
[architecture standard](../../docs/standards/architecture.md), the
[testing standard](../../docs/standards/testing.md), and this included build's
[README](README.md) and [documentation index](docs/README.md).

## Local Boundaries

- Keep this included build reusable and independently publishable. Product,
  feature, UI, screen, and Water My Plants configuration belongs outside it.
- Keep convention-plugin responsibilities in their existing capability module.
  Keep assertion-framework-independent test behavior in `repo/unit-testing` and
  consume its published `com.marmatsan.repo:unit-test-dsl` coordinate.
- Own compile, test, plugin, and publication versions in this build's
  `versions.properties`. Consume dependency-catalog APIs by stable coordinate
  and let composition roots substitute source during repository development.
- Name convention plugin implementations `<Capability>GradleConventionPlugin`
  and preserve the stable `com.marmatsan.<name>` plugin-id family.
- Extend catalog alias behavior through the reusable dependency-catalog API.
  Keep alias creation and alias lookup on the same canonical mapping.
- Configure plugins through lazy typed Gradle APIs. Use `afterEvaluate` only
  when no supported lazy API can express the required ordering, and document
  that external constraint.

Use the supported replacement when enforcing a boundary: published coordinates
instead of sibling filesystem paths, consumer configuration instead of product
branches, type-safe catalogs instead of hard-coded versions, and focused
capability modules instead of catch-all plugin behavior.

## Testing And Documentation

- Use Kotest and MockK for Kotlin tests.
- Express Given-When-Then unit behavior with the typed
  `given { }.whenever { }.then { }` chain from `unit-test-dsl`.
- Keep assertions in Kotest and executable business behavior in Cucumber.
- Update the owning module `docs/README.md` when a plugin's public behavior,
  requirements, or focused verification changes.
- Apply the root documentation-learning review before completion.

## Verification

Run focused module checks while iterating. Before completion run:

```powershell
.\gradlew.bat -p repo/gradle-plugins check verifyStagedPublication
```

Run the root boundary, documentation, and aggregate checks selected by the
[code-generation context map](../../docs/reference/code-generation-context.md)
when composition, versions, or shared contracts change.
