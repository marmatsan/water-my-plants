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

    test("readBuildLogic maps type-safe project accessors to build-logic module dependencies") {
        // GIVEN
        val rootDir = Files.createTempDirectory("build-logic-module-dependencies").toFile()
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
            .resolve("figmaDesignSync/plugin")
            .also { directory -> directory.mkdirs() }
            .resolve("build.gradle.kts")
            .writeText(
                """
                dependencies {
                    implementation(projects.figmaDesignSync.domain)
                    implementation(projects.figmaDesignSync.data)
                }
                """.trimIndent()
            )

        // WHEN
        val dependencies = GradleModuleDependenciesReader().readBuildLogic(rootDir)

        // THEN
        dependencies shouldBe setOf(
            ModuleDependency(
                dependentModule = ":build-logic:android",
                dependencyModule = ":build-logic:dependencies"
            ),
            ModuleDependency(
                dependentModule = ":build-logic:figmaDesignSync:plugin",
                dependencyModule = ":build-logic:figmaDesignSync:data"
            ),
            ModuleDependency(
                dependentModule = ":build-logic:figmaDesignSync:plugin",
                dependencyModule = ":build-logic:figmaDesignSync:domain"
            )
        )
    }
})
