package com.marmatsan.ci.domain.model

/** Provider-neutral snapshot of the root Gradle project's module graph. */
data class RepositoryModuleGraph(
    val modules: List<RepositoryModule>,
    val dependencies: List<ModuleDependency>
)
