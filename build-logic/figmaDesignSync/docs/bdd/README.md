# figmaDesignSync BDD scenarios

`figma-design-sync.feature` is a behavior index for `figmaDesignSync`.
Executable Cucumber scenarios live with the module they exercise.

Current executable suite:

- `../plugin/src/test/resources/features/figma-design-model.feature`
- `../plugin/src/test/kotlin/com/marmatsan/figmaDesignSync/plugin/bdd/RunCucumberTest.kt`

Build-logic unit and integration tests still use Kotest and MockK. Cucumber is
reserved for behavior-level scenarios that describe observable module contracts.

Use the scenarios to decide which UML diagrams are worth maintaining:

| Scenario                                                    | Test coverage                                                                | UML diagram                         |
|-------------------------------------------------------------|------------------------------------------------------------------------------|-------------------------------------|
| Generate the dependency design model                        | `plugin/generator/FigmaDesignModelGeneratorTest.kt`; executable Cucumber in `plugin/src/test/resources/features/figma-design-model.feature` | `../uml/model-generation-flow.puml` |
| Synchronize the visual Figma model from the generated model | TypeScript MCP sync tests to add                                             | `figma-sync-flow.puml`              |
| Reject an incomplete Figma visual model                     | TypeScript MCP sync tests to add                                             | `figma-sync-failure-flow.puml`      |
| Verify that Figma reflects trunk                            | `plugin/checker/sync` tests to add or extend                                 | `trunk-verification-flow.puml`      |
| Publish a PlantUML diagram to the UML Figma page            | Documentation workflow; automation tests to add when publication is scripted | `uml-publication-flow.puml`         |

Keep `.puml` files as the source of truth for UML diagrams. Rendered SVG files
are upload artifacts for Figma publication.
