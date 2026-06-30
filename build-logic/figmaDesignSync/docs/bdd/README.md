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
.\gradlew.bat :figmaDesignSync:plugin:test
```

Run selected scenarios by tag:

```powershell
.\gradlew.bat :figmaDesignSync:plugin:test -Dcucumber.filter.tags="@figma-design-model"
```

## Test Strategy

Cucumber scenarios document observable behavior and integration contracts.

Kotest and MockK remain the default tools for unit-level design feedback. Use
unit tests for focused implementation rules, edge cases, and fast feedback. Use
Cucumber when the scenario describes a behavior contract that should be readable
as living documentation.

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

Figma import notes and helper scripts for this module live in:

- `../uml/figma-import.md`
- `../uml/tools/sanitize-svg-for-figma.ps1`

## Current Contracts

| Contract                               | Executable source | Step glue  | UML diagram | Figma publication         |
|----------------------------------------|-------------------|------------|-------------|---------------------------|
| Generated `design-model.json` artifact | [feature][1]      | [steps][2] | [uml][3]    | `figmaDesignSync` section |

[1]: ../../plugin/src/test/resources/com/marmatsan/figmaDesignSync/plugin/bdd/figma-design-model.feature
[2]: ../../plugin/src/test/kotlin/com/marmatsan/figmaDesignSync/plugin/bdd
[3]: ../uml/diagrams/figma-design-model-feature.puml
