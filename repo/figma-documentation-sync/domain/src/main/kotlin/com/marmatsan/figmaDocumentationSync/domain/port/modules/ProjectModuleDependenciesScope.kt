package com.marmatsan.figmaDocumentationSync.domain.port.modules

/**
 * Logical repository area used when extracting module dependencies.
 *
 * `Main` covers the root project build. `IncludedBuild` covers a configured
 * Gradle included build.
 *
 * Example:
 * ```
 * ProjectModuleDependenciesScope.IncludedBuild
 * ```
 */
enum class ProjectModuleDependenciesScope {
    Main,
    IncludedBuild
}
