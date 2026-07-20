package com.marmatsan.verificationPlatform.domain.model

/**
 * Provider-neutral snapshot of the root Gradle project's module graph.
 *
 * @property modules modules eligible for change-impact analysis.
 * @property dependencies directed project-dependency edges between [modules].
 */
data class RepositoryModuleGraph(
    val modules: List<RepositoryModule>,
    val dependencies: List<ModuleDependency>,
)
