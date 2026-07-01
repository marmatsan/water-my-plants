package com.marmatsan.figmaDesignSync.domain.port.modules

/**
 * Port for reading the set of Gradle modules declared by settings sources.
 *
 * The module list is written into `design-model.json` so Figma can document the
 * repository structure alongside dependency graphs.
 *
 * @sample com.marmatsan.figmaDesignSync.domain.samples.DomainKDocSamples.projectModulesPortSample
 */
interface ProjectModulesPort {
    fun readModules(source: ProjectModulesSource): Set<String>
}
