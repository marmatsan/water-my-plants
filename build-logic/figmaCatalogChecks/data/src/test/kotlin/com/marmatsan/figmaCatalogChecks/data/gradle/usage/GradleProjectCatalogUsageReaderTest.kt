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
})
