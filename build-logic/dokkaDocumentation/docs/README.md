# Dokka Documentation Gradle Convention Plugin

## Plugin

```kotlin
plugins {
    id("com.marmatsan.dokkaDocumentation")
}
```

## Purpose

Configures shared Dokka API documentation defaults for build-logic modules that
expose documented Kotlin APIs.

Use this convention when a module should generate Dokka HTML documentation with
consistent visibility, source links, and source-set defaults.

## Behavior

- Applies `org.jetbrains.dokka`.
- Sets the Dokka module name from the Gradle project path.
- Documents public API only.
- Keeps undocumented declarations as non-failing while documentation is being
  introduced incrementally.
- Skips empty packages.
- Suppresses generated files.
- Adds source links for `src/main/kotlin` declarations when that source
  directory exists.

## Source Links

Generated API pages should link back to GitHub source files.

The project-specific remote root lives in `build-logic/gradle.properties`:

```properties
dokkaDocumentation.remoteSourceRootUrl=https://github.com/marmatsan/water-my-plants/tree/main/build-logic
```

Do not hardcode repository URLs in the convention plugin implementation. The
plugin should compose source links from that property and each subproject's
local `src/main/kotlin` path.

Use `remoteLineSuffix = "#L"` for GitHub line links.

## Dokka Version And DSL

- Use Dokka Gradle Plugin v2.
- Keep the Dokka version in `build-logic/versions.properties`.
- Configure Dokka with the top-level `dokka { ... }` DSL.
- Do not add new task-based Dokka v1 configuration such as
  `tasks.withType<DokkaTask>()`.

## Tasks

Use the DGP v2 task names:

```powershell
.\gradlew.bat -p build-logic :<module>:dokkaGenerate
```

For example:

```powershell
.\gradlew.bat -p build-logic :figmaDesignSync:domain:dokkaGenerate
```

`dokkaGenerateHtml` may appear in the IDE, but it is an alias for the HTML
publication task. Prefer `dokkaGenerate` in documentation and verification
notes.

## Samples

Use `@sample` only where an example clarifies how the API is intended to be
used.

When KDoc in `src/main` references a sample, keep the sample visible to the main
source set if IDE resolution matters. In practice, internal sample helpers under
`src/main/kotlin/.../samples/` work well: Dokka can render them, Android Studio
can resolve them, and they do not become public API when Dokka documents only
public declarations.

Register sample files in the consuming module:

```kotlin
dokka {
    dokkaSourceSets.main {
        samples.from(file("src/main/kotlin/.../samples/DomainKDocSamples.kt"))
    }
}
```

## Module And Package Documentation

Use Dokka `includes.from(...)` for Markdown pages that explain a whole module or
package. This is useful for architecture-level context that does not belong in a
single class KDoc.

The Markdown headings must follow Dokka's format:

```markdown
# Module figmaDesignSync/domain

Module-level documentation.

# Package com.marmatsan.figmaDesignSync.domain.model.catalog

Package-level documentation.
```

## Output Format

HTML is the default and preferred output format. Do not add Javadoc output unless
there is a publishing requirement for a `javadoc.jar`; Dokka's Javadoc format is
still less suitable for this project than the HTML API reference.

## Verification

For changes to this plugin, prefer focused build-logic verification:

```powershell
.\gradlew.bat -p build-logic :dokkaDocumentation:check
```
