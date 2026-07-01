package com.marmatsan.figmaDesignSync.domain.port.modules

/**
 * Logical repository area used when extracting module dependencies.
 *
 * `Main` covers the root Water My Plants build. `BuildLogic` covers the
 * `build-logic` included build.
 *
 * Example:
 * ```
 * ProjectModuleDependenciesScope.BuildLogic
 * ```
 */
enum class ProjectModuleDependenciesScope {
    Main,
    BuildLogic
}
