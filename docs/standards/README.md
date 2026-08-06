# Engineering Standards

These documents define project-wide implementation rules. `MUST` rules are
review-blocking; `SHOULD` rules require a documented reason when not followed.

| Standard | Scope |
|----------|-------|
| [Code generation](code-generation.md) | Context selection, positive-first instructions, active specifications, generation workflow, and completion. |
| [Architecture](architecture.md) | Module boundaries, dependency direction, composition, and SOLID design. |
| [Gradle](gradle.md) | Build scripts, convention plugins, catalogs, included builds, and custom tasks. |
| [Repository hygiene](repository-hygiene.md) | Temporary artifacts, generated output, and safe module retirement. |
| [Kotlin](kotlin.md) | Kotlin source design and concurrency. |
| [Android](android.md) | Android modules, lifecycle, dependency injection, and build conventions. |
| [Compose](compose.md) | Compose APIs, state, design-system use, and previews. |
| [API client](api-client.md) | Transport boundaries, DTOs, errors, and security. |
| [Testing](testing.md) | Unit, BDD, integration, and UI test responsibilities. |
| [Typed errors](error-handling.md) | Recoverable failures, exception boundaries, lifecycle state, and kotlin-result. |
| [Accessibility](accessibility.md) | Semantics, interaction, text, and visual accessibility. |
| [UML](uml.md) | PlantUML source layout and Figma publication contract. |
| [Git workflow](git-workflow.md) | Trunk, short-lived branches, pull requests, merges, and release tags. |
