package com.marmatsan.figmaDesignSync.domain.port.modules

import com.marmatsan.figmaDesignSync.domain.model.modules.ModuleDependency

/**
 * Port for reading directed module dependency edges from a project source.
 *
 * The generated edges become the `moduleDependencies` section of
 * `design-model.json`, which is the source used by Figma module dependency
 * documentation.
 *
 * @sample com.marmatsan.figmaDesignSync.domain.samples.DomainKDocSamples.projectModuleDependenciesPortSample
 */
interface ProjectModuleDependenciesPort {
    fun readModuleDependencies(source: ProjectModuleDependenciesSource): Set<ModuleDependency>
}
