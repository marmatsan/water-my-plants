package com.marmatsan.verificationPlatform.domain.model.modules

/**
 * Directed edge from the consuming module to the module it depends on.
 *
 * @property dependentModule Gradle path of the module declaring the dependency.
 * @property dependencyModule Gradle path of the consumed module.
 */
data class ModuleDependency(
    val dependentModule: String,
    val dependencyModule: String,
)
