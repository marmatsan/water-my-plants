# Module figmaDesignSync-domain

Pure domain contracts for the Figma design sync pipeline.

This module describes the information that eventually becomes part of
`design-model.json`: repository versions, dependency catalog trees, Gradle
modules, module dependency edges, and Figma node references. It intentionally
does not know about Gradle APIs, files, HTTP clients, or Figma API transport.

Read this module from the outside in:

1. Ports in `com.marmatsan.figmaDesignSync.domain.port` describe what the
   generator needs.
2. Models in `com.marmatsan.figmaDesignSync.domain.model` describe the stable
   data contract.
3. Samples in `com.marmatsan.figmaDesignSync.domain.samples` show small
   in-memory examples used by KDoc `@sample` tags.

# Package com.marmatsan.figmaDesignSync.domain.model.catalog

Catalog models represent dependency and plugin trees independently from their
technical source. They can be populated from the dependency DSL, Gradle settings
files, or repository-owned Gradle plugin declarations.

# Package com.marmatsan.figmaDesignSync.domain.model.modules

Module models describe Gradle module relationships that Figma can render as
dependency diagrams.

# Package com.marmatsan.figmaDesignSync.domain.model.versions

Version models preserve the grouping and ordering from
`build-logic/versions.properties` so the generated design model can document
where repository versions come from.

# Package com.marmatsan.figmaDesignSync.domain.port

Ports keep the domain independent from IO. Adapters in `figmaDesignSync-data`
implement these interfaces by reading files, Gradle scripts, or other external
sources.
