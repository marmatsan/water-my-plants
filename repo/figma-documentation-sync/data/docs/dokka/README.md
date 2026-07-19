# Module figmaDocumentationSync-data

Adapter layer for the Figma design sync pipeline.

This module translates concrete project sources into the pure domain contracts
from `figmaDocumentationSync-domain`. It is allowed to read files, parse Gradle Kotlin
DSL snippets, call the Figma API, and depend on parser libraries. It should not
decide the final `design-model.json` shape; that belongs to the generator in
`figmaDocumentationSync-plugin`.

Read this module by source type:

1. `datasource` classes implement domain ports.
2. `gradle` readers parse Gradle settings and build files.
3. `dependencies` readers adapt the repository dependency DSL.
4. `properties` readers parse `versions.properties`.
5. `figma` classes access Figma file content and shared plugin metadata.

# Package com.marmatsan.figmaDocumentationSync.data.datasource

Datasource classes are the boundary between domain ports and concrete readers.
They select the right reader for each domain source variant.

# Package com.marmatsan.figmaDocumentationSync.data.gradle

Gradle readers extract project structure, dependency edges, and plugin/catalog
usage from Gradle files without exposing Gradle APIs to the domain.

# Package com.marmatsan.figmaDocumentationSync.data.figma

Figma classes handle URL parsing, HTTP access, and DTOs for the Figma file API.
