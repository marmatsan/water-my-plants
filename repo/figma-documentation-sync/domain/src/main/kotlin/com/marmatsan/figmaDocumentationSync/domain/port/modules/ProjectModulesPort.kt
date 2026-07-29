package com.marmatsan.figmaDocumentationSync.domain.port.modules

/**
 * Port for reading the set of Gradle modules declared by settings sources.
 *
 * The module list is written into `design-model.json` so Figma can document the
 * repository structure alongside dependency graphs.
 *
 * @sample com.marmatsan.figmaDocumentationSync.domain.samples.DomainKDocSamples.projectModulesPortSample
 *
 * @see ProjectModulesSource
 */
interface ProjectModulesPort {
    /**
     * Reads all module paths that should appear in the generated design model.
     *
     * Returned values use Gradle path notation such as `:app` or
     * `:gradle-plugins:dependencies`.
     */
    fun readModules(
        source: ProjectModulesSource
    ): Set<String>
}
