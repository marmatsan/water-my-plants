package com.marmatsan.ci.domain.model

/** Directed edge from the consuming module to the module it depends on. */
data class ModuleDependency(
    val dependentModule: String,
    val dependencyModule: String
)
