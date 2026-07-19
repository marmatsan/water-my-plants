package com.marmatsan.ci

import com.marmatsan.ci.domain.model.ModuleDependency
import com.marmatsan.ci.domain.model.RepositoryModule
import com.marmatsan.ci.domain.model.RepositoryModuleGraph

fun testModuleGraph(): RepositoryModuleGraph = RepositoryModuleGraph(
    modules = listOf(
        RepositoryModule(id = ":app", directory = "app"),
        RepositoryModule(id = ":core:ui", directory = "core/ui"),
        RepositoryModule(id = ":onboarding:ui", directory = "onboarding/ui")
    ),
    dependencies = listOf(
        ModuleDependency(dependentModule = ":app", dependencyModule = ":core:ui"),
        ModuleDependency(dependentModule = ":onboarding:ui", dependencyModule = ":core:ui")
    )
)
