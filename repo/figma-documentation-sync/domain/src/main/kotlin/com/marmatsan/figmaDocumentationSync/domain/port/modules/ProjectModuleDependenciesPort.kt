package com.marmatsan.figmaDocumentationSync.domain.port.modules

import com.marmatsan.figmaDocumentationSync.domain.model.modules.ModuleDependency

/**
 * Port for reading directed module dependency edges from a project source.
 *
 * The generated edges become the `moduleDependencies` section of
 * `design-model.json`, which is the source used by Figma module dependency
 * documentation.
 *
 * @sample com.marmatsan.figmaDocumentationSync.domain.samples.DomainKDocSamples.projectModuleDependenciesPortSample
 *
 * @see ProjectModuleDependenciesSource
 * @see ModuleDependency
 */
interface ProjectModuleDependenciesPort {
    /**
     * Reads dependency edges for the repository area described by [source].
     *
     * Implementations should return a set because duplicate declarations in
     * Gradle files do not represent distinct documentation edges.
     */
    fun readModuleDependencies(source: ProjectModuleDependenciesSource): Set<ModuleDependency>
}
