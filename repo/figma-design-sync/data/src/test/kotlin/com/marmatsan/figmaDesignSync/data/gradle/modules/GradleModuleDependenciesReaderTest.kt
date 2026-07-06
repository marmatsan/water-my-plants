package com.marmatsan.figmaDesignSync.data.gradle.modules

import com.marmatsan.figmaDesignSync.domain.model.modules.ModuleDependency
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

internal class GradleModuleDependenciesReaderTest : FunSpec({

    test("readMain maps type-safe project accessors to module dependencies") {
        // GIVEN
        val rootDir = Files.createTempDirectory("main-module-dependencies").toFile()
        rootDir
            .resolve("app")
            .also { directory -> directory.mkdirs() }
            .resolve("build.gradle.kts")
            .writeText(
                """
                dependencies {
                    implementation(projects.core.ui)
                }
                """.trimIndent()
            )
        rootDir
            .resolve("onboarding/ui")
            .also { directory -> directory.mkdirs() }
            .resolve("build.gradle.kts")
            .writeText(
                """
                dependencies {
                    implementation(project(":core:ui"))
                }
                """.trimIndent()
            )

        // WHEN
        val dependencies = GradleModuleDependenciesReader().readMain(rootDir)

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
            .resolve("app")
            .also { directory -> directory.mkdirs() }
            .resolve("build.gradle.kts")
            .writeText(
                """
                val ignored = projects.feature.debug

                dependencies {
                    implementation(projects.core.ui)
                }
                """.trimIndent()
            )

        // WHEN
        val dependencies = GradleModuleDependenciesReader().readMain(rootDir)

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
            .resolve("android")
            .also { directory -> directory.mkdirs() }
            .resolve("build.gradle.kts")
            .writeText(
                """
                dependencies {
                    implementation(projects.dependencies)
                }
                """.trimIndent()
            )
        rootDir
            .resolve("analytics/plugin")
            .also { directory -> directory.mkdirs() }
            .resolve("build.gradle.kts")
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
})
