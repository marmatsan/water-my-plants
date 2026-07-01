package com.marmatsan.figmaDesignSync.domain.model.modules

/**
 * Directed dependency edge between two Gradle modules.
 *
 * [dependentModule] is the module declaring the dependency and
 * [dependencyModule] is the module it points to. These edges are serialized
 * into `design-model.json` so Figma can render module dependency diagrams.
 *
 * @sample com.marmatsan.figmaDesignSync.domain.samples.DomainKDocSamples.moduleDependencySample
 *
 * @property dependentModule Gradle module path declaring the dependency.
 * @property dependencyModule Gradle module path being depended on.
 */
data class ModuleDependency(
    val dependentModule: String,
    val dependencyModule: String
) : Comparable<ModuleDependency> {
    override fun compareTo(other: ModuleDependency): Int =
        compareBy<ModuleDependency>(
            ModuleDependency::dependentModule,
            ModuleDependency::dependencyModule
        ).compare(this, other)

    fun render(): String =
        "$dependentModule -> $dependencyModule"
}
