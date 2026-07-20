package com.marmatsan.figmaDocumentationSync.data.gradle.modules

import com.marmatsan.figmaDocumentationSync.domain.model.modules.ModuleDependency
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

internal class GradleModuleDependenciesReaderTest : FunSpec(
    {

    test("readMain maps type-safe project accessors to module dependencies") {
        // GIVEN
        val rootDir = Files.createTempDirectory("main-module-dependencies").toFile()
        rootDir
            .resolve(
                relative = "app"
            )
            .also { directory -> directory.mkdirs() }
            .resolve(
                relative = "build.gradle.kts"
            )
            .writeText(
                """
                dependencies {
                    implementation(projects.core.ui)
                }
                """.trimIndent()
            )
        rootDir
            .resolve(
                relative = "onboarding/ui"
            )
            .also { directory -> directory.mkdirs() }
            .resolve(
                relative = "build.gradle.kts"
            )
            .writeText(
                """
                dependencies {
                    implementation(project(":core:ui"))
                }
                """.trimIndent()
            )

        // WHEN
        val dependencies = GradleModuleDependenciesReader().readMain(
            rootDir = rootDir
        )

        // THEN
        dependencies shouldBe setOf(
            ModuleDependency(
                dependentModule = ":app",
                dependencyModule = ":core:ui"
            ),
            ModuleDependency(
                dependentModule = ":onboarding:ui",
                dependencyModule = ":core:ui"
            )
        )
    }

    test("readMain ignores project accessors outside dependencies blocks") {
        // GIVEN
        val rootDir = Files.createTempDirectory("main-module-dependencies-outside-block").toFile()
        rootDir
            .resolve(
                relative = "app"
            )
            .also { directory -> directory.mkdirs() }
            .resolve(
                relative = "build.gradle.kts"
            )
            .writeText(
                """
                val ignored = projects.feature.debug

                dependencies {
                    implementation(projects.core.ui)
                }
                """.trimIndent()
            )

        // WHEN
        val dependencies = GradleModuleDependenciesReader().readMain(
            rootDir = rootDir
        )

        // THEN
        dependencies shouldBe setOf(
            ModuleDependency(
                dependentModule = ":app",
                dependencyModule = ":core:ui"
            )
        )
    }

    test("readIncludedBuild maps type-safe project accessors to included build module dependencies") {
        // GIVEN
        val rootDir = Files.createTempDirectory("gradle-plugins-module-dependencies").toFile()
        rootDir
            .resolve(
                relative = "android"
            )
            .also { directory -> directory.mkdirs() }
            .resolve(
                relative = "build.gradle.kts"
            )
            .writeText(
                """
                dependencies {
                    implementation(projects.dependencies)
                }
                """.trimIndent()
            )
        rootDir
            .resolve(
                relative = "analytics/plugin"
            )
            .also { directory -> directory.mkdirs() }
            .resolve(
                relative = "build.gradle.kts"
            )
            .writeText(
                """
                dependencies {
                    implementation(projects.analytics.domain)
                    implementation(projects.analytics.data)
                }
                """.trimIndent()
            )

        // WHEN
        val dependencies = GradleModuleDependenciesReader().readIncludedBuild(
            rootDir = rootDir,
            modulePathPrefix = ":gradle-plugins"
        )

        // THEN
        dependencies shouldBe setOf(
            ModuleDependency(
                dependentModule = ":gradle-plugins:android",
                dependencyModule = ":gradle-plugins:dependencies"
            ),
            ModuleDependency(
                dependentModule = ":gradle-plugins:analytics:plugin",
                dependencyModule = ":gradle-plugins:analytics:data"
            ),
            ModuleDependency(
                dependentModule = ":gradle-plugins:analytics:plugin",
                dependencyModule = ":gradle-plugins:analytics:domain"
            )
        )
    }

    test("readIncludedBuild maps camel case accessors to kebab case module paths") {
        // GIVEN
        val rootDir = Files.createTempDirectory("dependency-catalog-module-dependencies").toFile()
        rootDir
            .resolve(
                relative = "catalog-core"
            )
            .also { directory -> directory.mkdirs() }
            .resolve(
                relative = "build.gradle.kts"
            )
            .writeText("")
        rootDir
            .resolve(
                relative = "water-my-plants-catalog"
            )
            .also { directory -> directory.mkdirs() }
            .resolve(
                relative = "build.gradle.kts"
            )
            .writeText(
                """
                dependencies {
                    implementation(projects.catalogCore)
                }
                """.trimIndent()
            )

        // WHEN
        val dependencies = GradleModuleDependenciesReader().readIncludedBuild(
            rootDir = rootDir,
            modulePathPrefix = ":dependency-catalog"
        )

        // THEN
        dependencies shouldBe setOf(
            ModuleDependency(
                dependentModule = ":dependency-catalog:water-my-plants-catalog",
                dependencyModule = ":dependency-catalog:catalog-core"
            )
        )
    }
}
)
