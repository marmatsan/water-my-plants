# figmaDesignSync BDD

## Source Of Truth

The `.feature` files are the executable source of truth for behavior-level
module contracts.

UML diagrams and Figma sections are derived documentation:

```text
.feature -> .puml -> generated .svg -> locked Figma section
```

Do not use `@manual` scenarios. Add or update a scenario only when the matching
Cucumber step definitions are executable in the same change.

## Current Executable Features

- `../../plugin/src/test/resources/com/marmatsan/figmaDesignSync/plugin/bdd/figma-design-model.feature`
- `../../plugin/src/test/kotlin/com/marmatsan/figmaDesignSync/plugin/bdd/RunCucumberTest.kt`

## Running Cucumber

Run all `figmaDesignSync` plugin tests:

```powershell
.\gradlew.bat :figma-design-sync:plugin:test
```

Run selected scenarios by tag:

```powershell
.\gradlew.bat :figma-design-sync:plugin:test -Dcucumber.filter.tags="@figma-design-model"
```

## Test Strategy

Cucumber scenarios document observable behavior and integration contracts.

Kotest and MockK remain the default tools for unit-level design feedback. Use
unit tests for focused implementation rules, edge cases, and fast feedback. Use
Cucumber when the scenario describes a behavior contract that should be readable
as living documentation.

## Scenario Language

`.feature` files should describe behavior in the module's domain language. They
should not become technical inventories of implementation files.

Prefer steps that explain the capability the system needs:

```gherkin
Given repository versions are available
```

Avoid replacing behavior language with file-system details unless the file path
is the behavior being tested:

```gherkin
Given the repository version source exists at "repo/dependency-catalog/versions.properties"
```

The step definition owns the implementation detail. For example, the current
`repository versions are available` step prepares the `RepositoryVersionsPort`
used by the design model generator. In the real Gradle task, that port is backed
by `repo/dependency-catalog/versions.properties`; in the domain scenario, it is backed by a
test double so the behavior stays fast and focused.

Use this rule when adding or editing scenarios:

- Put the expected behavior in Gherkin.
- Put the concrete files, adapters, ports, and generated artifacts in this
  README or in module architecture docs.
- Put a path in a step only when the scenario is explicitly about that path or
  about a generated file at that path.
- Keep integration scenarios more concrete than domain scenarios when the
  observable behavior is a Gradle project, a report file, or a plugin-applied
  temporary build.

## Technical Resource Map

These resources explain what the current feature language means in the real
project. They are supporting documentation, not the preferred wording for
Gherkin steps.

| Feature language                               | Runtime resource or adapter                                                                                  | Test double or setup                                         |
|------------------------------------------------|--------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| `repository versions are available`            | `repo/dependency-catalog/versions.properties` through `RepositoryVersionsPort`                                           | `FakeRepositoryVersionsPort` in `DesignModelSteps.kt`        |
| `repository catalog trees are available`       | `water-my-plants-catalog` trees and configured included-build settings catalogs through `ProjectCatalogTreesPort` | `FakeProjectCatalogTreesPort` in `DesignModelSteps.kt`       |
| `repository project modules are available`     | `settings.gradle.kts` and configured included-build settings files through `ProjectModulesPort`                     | `FakeProjectModulesPort` in `DesignModelSteps.kt`            |
| `repository module dependencies are available` | Project `build.gradle.kts` dependency blocks through `ProjectModuleDependenciesPort`                         | `FakeProjectModuleDependenciesPort` in `DesignModelSteps.kt` |
| `the design model is generated`                | `FigmaDesignModelGenerator` producing the in-memory design model                                             | Direct generator call from `DesignModelSteps.kt`             |
| `generateFigmaDesignModel runs`                | Gradle task writing `build/reports/figma-sync/design-model.json`                                             | Temporary Gradle project assembled by `GradleTaskSteps.kt`   |

## Generated Design Model Contract

`figma-design-model.feature` describes the executable contract for creating the
`design-model.json` artifact. The feature should stay focused on observable
behavior: what repository information is available, what generation action is
performed, and what guarantees the resulting model provides.

The generated design model is the boundary between repository analysis and the
Figma publication pipeline. It does not update Figma by itself. It captures a
reviewable repository snapshot that later sync steps can publish to Figma and
verify against `main`.

The contract has these inputs:

| Input concept            | Runtime source                                                                                           |
|--------------------------|----------------------------------------------------------------------------------------------------------|
| Repository metadata      | Current branch, current git SHA, and generation timestamp                                                |
| Versions                 | `repo/dependency-catalog/versions.properties`                                                                        |
| Version sections         | Ordered sections from `repo/dependency-catalog/versions.properties`                                                  |
| Catalog trees            | Water My Plants declarations from `dependency-catalog:water-my-plants-catalog` and configured included-build catalog declarations |
| Project modules          | Root and configured included-build Gradle settings                                                          |
| Module dependency graphs | Parsed `build.gradle.kts` dependency blocks for root and configured included-build modules                  |

The contract has one main output:

| Output artifact     | Runtime location                             | Purpose                                                              |
|---------------------|----------------------------------------------|----------------------------------------------------------------------|
| `design-model.json` | `build/reports/figma-sync/design-model.json` | Deterministic repository snapshot consumed by later Figma sync tasks |

`content` is the stable body of that artifact and participates in
`modelHash`. It contains these sections:

| Content key          | Meaning                                                                                                      |
|----------------------|--------------------------------------------------------------------------------------------------------------|
| `versions`           | Sorted flat map of version keys to repository values, used for deterministic comparison.                     |
| `versionSections`    | Ordered version groups from `repo/dependency-catalog/versions.properties`, used to preserve the source section layout.   |
| `catalogs`           | Dependency and plugin trees for Water My Plants, configured included builds, custom Gradle convention plugins, and plugins, including direct and convention-plugin-provided usage metadata. |
| `modules`            | Sorted Gradle module paths discovered from root and configured included-build settings files.                            |
| `moduleDependencies` | Main and configured included-build module dependency edges, grouped by graph scope.                                      |

The current executable scenarios assert these guarantees:

- The model contains repository metadata.
- The model contains `versions`, `versionSections`, `catalogs`, `modules`, and
  `moduleDependencies`.
- Version keys are sorted.
- Version sections keep repository order.
- The model stores a reproducible `modelHash`.
- `generatedAt` is written to the model but does not affect `modelHash`.
- `gitSha` is written to the model but does not affect `modelHash`.
- The Gradle task writes the report file in a temporary plugin-applied project.

When this contract changes, update the `.feature` first if the behavior changes.
Update this README when only the technical explanation or resource mapping needs
more clarity.

## Step Organization

Step definitions live in the same package as the Cucumber glue:

- `../../plugin/src/test/kotlin/com/marmatsan/figmaDesignSync/plugin/bdd/DesignModelSteps.kt`
- `../../plugin/src/test/kotlin/com/marmatsan/figmaDesignSync/plugin/bdd/GradleTaskSteps.kt`
- `../../plugin/src/test/kotlin/com/marmatsan/figmaDesignSync/plugin/bdd/CucumberTypes.kt`

Use `io.cucumber.java8.En` style definitions. Do not add `io.cucumber.java`
annotations unless the project intentionally changes style.

Group steps by domain concept, not by feature file. Avoid feature-coupled steps
and conjunction steps. Prefer helper methods inside step files when several
steps need the same setup or assertion code.

## UML And Figma Publication

Module UML documentation lives in:

- `../uml/`

PlantUML diagrams live in:

- `../uml/diagrams/`

Shared PlantUML includes stay global:

- `../../../../docs/uml/includes/theme.puml`
- `../../../../docs/uml/includes/stereotypes.puml`

Feature diagrams, such as `../uml/diagrams/figma-design-model-feature.puml`, are derived
from `.feature` files. Update them only when the visual representation helps
communication.

Rendered SVGs are temporary publication artifacts. After the SVG is published to
the locked Figma UML section, delete the generated SVG locally.

The Figma import runbook and helper scripts for this module live in:

- `../uml/figma-import.md`
- `../uml/tools/sanitize-svg-for-figma.ps1`

## Current Contracts

| Contract                               | Executable source | Step glue  | UML diagram | Figma publication                                     |
|----------------------------------------|-------------------|------------|-------------|-------------------------------------------------------|
| Generated `design-model.json` artifact | [feature][1]      | [steps][2] | [uml][3]    | `figmaDesignSync` / `figma-design-model-feature.puml` |

[1]: ../../plugin/src/test/resources/com/marmatsan/figmaDesignSync/plugin/bdd/figma-design-model.feature
[2]: ../../plugin/src/test/kotlin/com/marmatsan/figmaDesignSync/plugin/bdd
[3]: ../uml/diagrams/figma-design-model-feature.puml
