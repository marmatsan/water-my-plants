package com.marmatsan.figmaDesignSync.domain.port.modules

/**
 * Source directory and logical scope used to read module dependency edges.
 *
 * [rootDirPath] points to either the repository root or the `build-logic` root,
 * depending on [scope].
 *
 * Example:
 * ```
 * ProjectModuleDependenciesSource(
 *     rootDirPath = "build-logic",
 *     scope = ProjectModuleDependenciesScope.BuildLogic
 * )
 * ```
 *
 * @property rootDirPath Root directory used by the dependency reader.
 * @property scope Repository area represented by this source.
 */
data class ProjectModuleDependenciesSource(
    val rootDirPath: String,
    val scope: ProjectModuleDependenciesScope
)
