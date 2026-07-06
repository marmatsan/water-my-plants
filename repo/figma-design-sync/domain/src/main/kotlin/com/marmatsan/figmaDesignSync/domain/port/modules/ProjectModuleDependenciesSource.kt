package com.marmatsan.figmaDesignSync.domain.port.modules

/**
 * Source directory and logical scope used to read module dependency edges.
 *
 * [rootDirPath] points to either the repository root or an included-build root,
 * depending on [scope]. Included-build sources also provide the logical
 * [modulePathPrefix] used in the generated model.
 *
 * Example:
 * ```
 * ProjectModuleDependenciesSource(
 *     rootDirPath = "repo/gradle-plugins",
 *     scope = ProjectModuleDependenciesScope.IncludedBuild,
 *     modulePathPrefix = ":gradle-plugins"
 * )
 * ```
 *
 * @property rootDirPath Root directory used by the dependency reader.
 * @property scope Repository area represented by this source.
 * @property modulePathPrefix Prefix applied to included-build module paths.
 */
data class ProjectModuleDependenciesSource(
    val rootDirPath: String,
    val scope: ProjectModuleDependenciesScope,
    val modulePathPrefix: String = ""
)
