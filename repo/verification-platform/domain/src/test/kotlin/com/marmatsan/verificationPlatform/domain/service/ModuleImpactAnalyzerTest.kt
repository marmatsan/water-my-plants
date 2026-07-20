package com.marmatsan.verificationPlatform.domain.service

import com.marmatsan.verificationPlatform.domain.model.ModuleDependency
import com.marmatsan.verificationPlatform.domain.model.RepositoryModule
import com.marmatsan.verificationPlatform.domain.model.RepositoryModuleGraph
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ModuleImpactAnalyzerTest : FunSpec({
    test("walks transitive reverse dependencies") {
        val graph = RepositoryModuleGraph(
            modules = listOf(
                RepositoryModule(id = ":app", directory = "app"),
                RepositoryModule(id = ":core:domain", directory = "core/domain"),
                RepositoryModule(id = ":feature:plants:domain", directory = "feature/plants/domain")
            ),
            dependencies = listOf(
                ModuleDependency(
                    dependentModule = ":app",
                    dependencyModule = ":feature:plants:domain"
                ),
                ModuleDependency(
                    dependentModule = ":feature:plants:domain",
                    dependencyModule = ":core:domain"
                )
            )
        )

        val impact = ModuleImpactAnalyzer().analyze(
            changedFiles = listOf("core/domain/src/main/kotlin/Plant.kt"),
            graph = graph
        )

        impact.changedModules shouldBe listOf(":core:domain")
        impact.affectedModules shouldBe listOf(
            ":app",
            ":core:domain",
            ":feature:plants:domain"
        )
        impact.fallbackReason shouldBe null
    }
})
