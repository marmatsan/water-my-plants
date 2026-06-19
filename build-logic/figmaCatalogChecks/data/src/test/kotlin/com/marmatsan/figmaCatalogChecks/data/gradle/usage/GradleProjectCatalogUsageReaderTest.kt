package com.marmatsan.figmaCatalogChecks.data.gradle.usage

import com.marmatsan.figmaCatalogChecks.domain.model.usage.LibraryCatalogUsageKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

internal class GradleProjectCatalogUsageReaderTest : FunSpec({

    test("read maps Gradle scripts and convention plugin sources to catalog usages") {
        // GIVEN
        val rootDir = Files.createTempDirectory("figma-catalog-usage").toFile()
        rootDir
            .resolve("app")
            .also { directory -> directory.mkdirs() }
            .resolve("build.gradle.kts")
            .writeText(
                """
                plugins {
                    alias(plugins.plugins.com.android.application)
                    id("com.marmatsan.compose")
                }

                dependencies {
                    implementation(libs.androidx.activity.compose)
                    implementation(libs.bundles.composeBundle)
                }
                """.trimIndent()
            )
        rootDir
            .resolve("build-logic/compose/src/main/kotlin")
            .also { directory -> directory.mkdirs() }
            .resolve("ComposePlugin.kt")
            .writeText(
                """
                project.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

                project.dependencies {
                    implementation(
                        libs = libs,
                        libraryGroup = "androidx.compose",
                        artifact = "compose-bom"
                    )
                    implementationBundle(
                        libs = libs,
                        bundle = "composeBundle"
                    )
                }
                """.trimIndent()
            )
        rootDir
            .resolve("build-logic/compose")
            .resolve("build.gradle.kts")
            .writeText("plugins { `kotlin-dsl` }")

        // WHEN
        val usage = GradleProjectCatalogUsageReader().read(rootDir)

        // THEN
        usage.pluginIdsByModule[":app"] shouldBe setOf(
            "com.android.application",
            "com.marmatsan.compose"
        )
        usage.pluginIdsByModule[":build-logic:compose"] shouldBe setOf(
            "org.jetbrains.kotlin.plugin.compose"
        )
        usage.libraryAccessorsByModule[":app"] shouldBe setOf(
            "androidx.activity.compose",
            "bundles.composeBundle"
        )
        usage.libraryKeysByModule[":build-logic:compose"] shouldBe setOf(
            LibraryCatalogUsageKey.Artifact(
                group = "androidx.compose",
                artifact = "compose-bom"
            ),
            LibraryCatalogUsageKey.Bundle(alias = "composeBundle")
        )
    }

    test("readBuildLogic maps build-logic Gradle scripts to catalog usages") {
        // GIVEN
        val rootDir = Files.createTempDirectory("figma-build-logic-catalog-usage").toFile()
        rootDir
            .resolve("settings.gradle.kts")
            .writeText(
                """
                dependencyResolutionManagement {
                    versionCatalogs {
                        create("libs") {
                            library(
                                alias = "io.kotest.runner.junit5",
                                group = "io.kotest",
                                artifact = "kotest-runner-junit5"
                            ).version("6.2.0")
                            library(
                                alias = "io.mockk",
                                group = "io.mockk",
                                artifact = "mockk"
                            ).version("1.14.11")
                            library(
                                alias = "org.junit.jupiter.platform.launcher",
                                group = "org.junit.platform",
                                artifact = "junit-platform-launcher"
                            ).withoutVersion()
                            plugin(
                                alias = "org.jetbrains.kotlin.plugin.serialization",
                                id = "org.jetbrains.kotlin.plugin.serialization"
                            ).version("2.4.0")
                        }
                    }
                }
                """.trimIndent()
            )
        rootDir
            .resolve("dependencies")
            .also { directory -> directory.mkdirs() }
            .resolve("build.gradle.kts")
            .writeText(
                """
                plugins {
                    `kotlin-dsl`
                }

                dependencies {
                    testImplementation(libs.io.kotest.runner.junit5)
                    testImplementation(libs.io.mockk)
                    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
                }
                """.trimIndent()
            )
        rootDir
            .resolve("figmaCatalogChecks/data")
            .also { directory -> directory.mkdirs() }
            .resolve("build.gradle.kts")
            .writeText(
                """
                plugins {
                    alias(plugins.plugins.org.jetbrains.kotlin.plugin.serialization)
                }

                dependencies {
                    implementation(platform(libs.io.ktor.bom))
                    implementation(libs.io.ktor.client.core)
                }
                """.trimIndent()
            )

        // WHEN
        val usage = GradleProjectCatalogUsageReader().readBuildLogic(rootDir)

        // THEN
        usage.libraryAccessorsByModule[":build-logic:dependencies"] shouldBe setOf(
            "io.kotest.runner.junit5",
            "io.mockk",
            "org.junit.jupiter.platform.launcher"
        )
        usage.libraryKeysByModule[":build-logic:dependencies"] shouldBe setOf(
            LibraryCatalogUsageKey.Artifact(
                group = "io.kotest",
                artifact = "kotest-runner-junit5"
            ),
            LibraryCatalogUsageKey.Artifact(
                group = "io.mockk",
                artifact = "mockk"
            ),
            LibraryCatalogUsageKey.Artifact(
                group = "org.junit.platform",
                artifact = "junit-platform-launcher"
            )
        )
        usage.libraryAccessorsByModule[":build-logic:figmaCatalogChecks:data"] shouldBe setOf(
            "io.ktor.bom",
            "io.ktor.client.core"
        )
        usage.pluginIdsByModule[":build-logic:figmaCatalogChecks:data"] shouldBe setOf(
            "org.jetbrains.kotlin.plugin.serialization"
        )
    }
})
