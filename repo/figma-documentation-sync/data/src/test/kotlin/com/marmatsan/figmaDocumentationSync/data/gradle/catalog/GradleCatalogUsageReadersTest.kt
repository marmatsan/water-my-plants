package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import java.io.File

internal class GradleCatalogUsageReadersTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "gradle-catalog-usage-readers",
                )

            test("readConventionLibraryUsages maps wrapper catalog dependencies to convention modules") {
                given {
                    val rootDir = temporaryDirectory.resolve("convention-library-usages").apply { mkdirs() }
                    val includedBuildRootDir =
                        rootDir.resolve(
                            relative = "repo/gradle-plugins",
                        )
                    includedBuildRootDir.writeBuildFile(
                        path = "android",
                        content =
                            """
                            gradlePlugin {
                                plugins.register("android") {
                                    implementationClass = "com.marmatsan.android.plugin.AndroidGradleConventionPlugin"
                                }
                            }
                            """.trimIndent(),
                    )
                    includedBuildRootDir.writeKotlinFile(
                        path = "android/src/main/kotlin/com/marmatsan/android/plugin",
                        fileName = "AndroidGradleConventionPlugin.kt",
                        content =
                            """
                            package com.marmatsan.android.plugin

                            fun configure() {
                                val libs = withVersionCatalog(libs)
                                libs.implementation(
                                    libraryGroup = "androidx.core",
                                    artifact = "core-ktx"
                                )
                                libs.implementationPlatform(
                                    libraryGroup = "androidx.compose",
                                    artifact = "compose-bom"
                                )
                                libs.implementationBundle(
                                    bundle = "composeBundle"
                                )
                                libs.testImplementationBundle(
                                    bundle = "kotest"
                                )
                            }
                            """.trimIndent(),
                    )
                    includedBuildRootDir
                }.whenever { includedBuildRootDir ->
                    GradleConventionCatalogUsageReader().readLibraryUsages(
                        rootDir = includedBuildRootDir,
                        modulePathPrefix = ":gradle-plugins",
                    )
                }.then { usages ->
                    usages shouldBe
                        LibraryUsages(
                            coordinates =
                                mapOf(
                                    "androidx.core:core-ktx" to setOf(":gradle-plugins:android"),
                                    "androidx.compose:compose-bom" to setOf(":gradle-plugins:android"),
                                ),
                            bundles =
                                mapOf(
                                    "composeBundle" to setOf(":gradle-plugins:android"),
                                    "kotest" to setOf(":gradle-plugins:android"),
                                ),
                        )
                }
            }

            test("readConventionPluginIdsByModule maps convention plugin ids to their implementing modules") {
                given {
                    val rootDir = temporaryDirectory.resolve("convention-plugin-ids").apply { mkdirs() }
                    val includedBuildRootDir =
                        rootDir.resolve(
                            relative = "repo/gradle-plugins",
                        )
                    includedBuildRootDir.writeBuildFile(
                        path = "compose",
                        content =
                            """
                            gradlePlugin {
                                val pluginName = "com.marmatsan.compose"
                                plugins.register(pluginName) {
                                    id = pluginName
                                    implementationClass = "com.marmatsan.compose.plugin.ComposeGradleConventionPlugin"
                                }
                            }
                            """.trimIndent(),
                    )
                    includedBuildRootDir.writeBuildFile(
                        path = "figma-documentation-sync",
                        content =
                            """
                            gradlePlugin {
                                val pluginName = "com.marmatsan.figmaDocumentationSync"
                                plugins.register(pluginName) {
                                    id = pluginName
                                    implementationClass = "com.marmatsan.figmaDocumentationSync.plugin.FigmaDocumentationSyncGradlePlugin"
                                }
                            }
                            """.trimIndent(),
                    )
                    includedBuildRootDir
                }.whenever { includedBuildRootDir ->
                    GradleConventionCatalogUsageReader().readPluginIdsByModule(
                        rootDir = includedBuildRootDir,
                        modulePathPrefix = ":gradle-plugins",
                    )
                }.then { pluginIdsByModule ->
                    pluginIdsByModule shouldBe
                        mapOf(
                            ":gradle-plugins:compose" to setOf("com.marmatsan.compose"),
                        )
                }
            }

            test(
                "readConventionLibraryConfigurationUsages maps requireDependencyNotation calls to convention modules",
            ) {
                given {
                    val rootDir =
                        temporaryDirectory.resolve("convention-library-configuration-usages").apply { mkdirs() }
                    val includedBuildRootDir =
                        rootDir.resolve(
                            relative = "repo/gradle-plugins",
                        )
                    includedBuildRootDir.writeBuildFile(
                        path = "protobuf",
                        content =
                            """
                            gradlePlugin {
                                plugins.register("protobuf") {
                                    id = "com.marmatsan.protobuf"
                                    implementationClass = "com.marmatsan.protobuf.plugin.ProtobufGradleConventionPlugin"
                                }
                            }
                            """.trimIndent(),
                    )
                    includedBuildRootDir.writeKotlinFile(
                        path = "protobuf/src/main/kotlin/com/marmatsan/protobuf/plugin",
                        fileName = "ProtobufGradleConventionPlugin.kt",
                        content =
                            """
                            package com.marmatsan.protobuf.plugin

                            fun configureProtobuf() {
                                project.extensions.configure<ProtobufExtension>("protobuf") {
                                    protoc {
                                        artifact = libs.requireDependencyNotation(
                                            libraryGroup = "com.google.protobuf",
                                            artifact = "protoc"
                                        )
                                    }
                                }
                            }
                            """.trimIndent(),
                    )
                    includedBuildRootDir
                }.whenever { includedBuildRootDir ->
                    GradleConventionCatalogUsageReader().readLibraryConfigurationUsages(
                        rootDir = includedBuildRootDir,
                        modulePathPrefix = ":gradle-plugins",
                    )
                }.then { usages ->
                    usages shouldBe
                        LibraryConfigurationUsages(
                            coordinates =
                                mapOf(
                                    "com.google.protobuf:protoc" to
                                        setOf(
                                            LibraryConfigurationUsage(
                                                pluginModule = ":gradle-plugins:protobuf",
                                                target = "protobuf.protoc.artifact",
                                            ),
                                        ),
                                ),
                        )
                }
            }

            test("readMainLiteralPluginUsages maps literal plugin ids to main modules") {
                given {
                    val rootDir = temporaryDirectory.resolve("main-literal-plugin-usages").apply { mkdirs() }
                    rootDir.writeBuildFile(
                        path = "",
                        content =
                            """
                            plugins {
                                id("com.marmatsan.android") apply false
                            }
                            """.trimIndent(),
                    )
                    rootDir.writeBuildFile(
                        path = "app",
                        content =
                            """
                            plugins {
                                id("com.marmatsan.android")
                                id("com.marmatsan.compose")
                            }
                            """.trimIndent(),
                    )
                    rootDir.writeBuildFile(
                        path = "core/ui",
                        content =
                            """
                            plugins {
                                id("com.marmatsan.android")
                            }
                            """.trimIndent(),
                    )
                    rootDir
                }.whenever { rootDir ->
                    GradleMainCatalogUsageReader().readLiteralPluginUsages(
                        rootDir = rootDir,
                    )
                }.then { usages ->
                    usages shouldBe
                        mapOf(
                            "com.marmatsan.android" to
                                setOf(
                                    ":app",
                                    ":core:ui",
                                ),
                            "com.marmatsan.compose" to setOf(":app"),
                        )
                }
            }

            test("readMainAppliedLiteralPluginUsages maps applied literal plugin ids to main modules") {
                given {
                    val rootDir = temporaryDirectory.resolve("main-applied-literal-plugin-usages").apply { mkdirs() }
                    rootDir.writeBuildFile(
                        path = "",
                        content =
                            """
                            plugins {
                                id("com.marmatsan.android") apply false
                                id("com.marmatsan.figmaDocumentationSync") apply true
                            }
                            """.trimIndent(),
                    )
                    rootDir.writeBuildFile(
                        path = "app",
                        content =
                            """
                            plugins {
                                id("com.marmatsan.android")
                            }
                            """.trimIndent(),
                    )
                    rootDir
                }.whenever { rootDir ->
                    GradleMainCatalogUsageReader().readAppliedLiteralPluginUsages(
                        rootDir = rootDir,
                    )
                }.then { usages ->
                    usages shouldBe
                        mapOf(
                            "com.marmatsan.android" to setOf(":app"),
                        )
                }
            }

            test("readMainAppliedLiteralPluginIds includes applied root plugin ids") {
                given {
                    val rootDir = temporaryDirectory.resolve("main-applied-literal-plugin-ids").apply { mkdirs() }
                    rootDir.writeBuildFile(
                        path = "",
                        content =
                            """
                            plugins {
                                id("com.marmatsan.android") apply false
                                id("com.marmatsan.figmaDocumentationSync") apply true
                            }
                            """.trimIndent(),
                    )
                    rootDir
                }.whenever { rootDir ->
                    GradleMainCatalogUsageReader().readAppliedLiteralPluginIds(
                        rootDir = rootDir,
                    )
                }.then { pluginIds ->
                    pluginIds shouldBe setOf("com.marmatsan.figmaDocumentationSync")
                }
            }
        },
    )

private fun File.writeKotlinFile(
    path: String,
    fileName: String,
    content: String,
) {
    val directory =
        resolve(
            relative = path,
        )
    directory.mkdirs()
    directory
        .resolve(
            relative = fileName,
        ).writeText(content)
}

private fun File.writeBuildFile(
    path: String,
    content: String,
) {
    val directory =
        resolve(
            relative = path,
        )
    directory.mkdirs()
    directory
        .resolve(
            relative = "build.gradle.kts",
        ).writeText(content)
}
