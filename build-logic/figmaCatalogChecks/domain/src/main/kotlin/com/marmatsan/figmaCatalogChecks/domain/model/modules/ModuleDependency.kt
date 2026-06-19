package com.marmatsan.figmaCatalogChecks.domain.model.modules

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
