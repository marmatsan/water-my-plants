# figmaDesignSync BDD scenarios

Every `.feature` file in this directory is executable documentation. Add a
scenario only in the same change that adds or updates the matching Cucumber step
definitions. Do not use `@manual` scenarios.

Current executable suite:

- `features/figma-design-model.feature`
- `../plugin/src/test/kotlin/com/marmatsan/figmaDesignSync/plugin/bdd/RunCucumberTest.kt`

Build-logic unit and integration tests still use Kotest and MockK. Cucumber is
reserved for behavior-level scenarios that describe observable module contracts.

Use the scenarios to decide which UML diagrams are worth maintaining:

| Scenario                             | Test coverage                                                                                                     | UML diagram                         |
|--------------------------------------|-------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| Generate the dependency design model | `plugin/generator/FigmaDesignModelGeneratorTest.kt`; executable Cucumber in `features/figma-design-model.feature` | `../uml/model-generation-flow.puml` |

Planned behavior should become a `.feature` only when it has executable step
definitions in the same change.

Keep `.puml` files as the source of truth for UML diagrams. Rendered SVG files
are upload artifacts for Figma publication.
