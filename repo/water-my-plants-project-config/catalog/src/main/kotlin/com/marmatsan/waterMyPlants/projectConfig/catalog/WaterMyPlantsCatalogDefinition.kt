package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.dependencies.catalog.DependencyCatalogTrees
import com.marmatsan.dependencies.catalog.dsl.dependencyCatalogTrees
import com.marmatsan.dependencies.catalog.version.DependencyVersionResolver

/** Builds the complete Water My Plants catalog from one version-resolution strategy. */
internal fun waterMyPlantsCatalogTrees(
    versionResolver: DependencyVersionResolver
): DependencyCatalogTrees =
    dependencyCatalogTrees(
        versionResolver = versionResolver
    ) {
        libraries {
            root("androidx") {
                library("activity") {
                    artifact(
                        artifact = "activity-compose",
                        version = version("activityComposeLibraryVersion")
                    )
                }
                library("compose") {
                    artifact(
                        artifact = "compose-bom",
                        version = version("composeBomLibraryVersion")
                    )
                    library("ui") {
                        artifactsBundle(
                            "ui",
                            "ui-graphics",
                            "ui-tooling",
                            "ui-tooling-preview",
                            alias = "composeBundle"
                        )
                    }
                    library("material3") {
                        artifact(
                            artifact = "material3"
                        )
                    }
                    library("material") {
                        artifact(
                            artifact = "material-icons-core"
                        )
                    }
                }
                library("core") {
                    artifact(
                        artifact = "core-ktx",
                        version = version("coreKtxLibraryVersion")
                    )
                }
                library("lifecycle") {
                    artifact(
                        artifact = "lifecycle-runtime-ktx",
                        version = version("lifecycleLibraryVersion")
                    )
                    artifactsBundle(
                        "lifecycle-viewmodel-compose",
                        "lifecycle-runtime-compose",
                        alias = "lifecycleComposeBundle",
                        version = version("lifecycleLibraryVersion")
                    )
                }
                library("navigation") {
                    artifact(
                        artifact = "navigation-compose",
                        version = version("navigationComposeLibraryVersion")
                    )
                }
            }
            root("com") {
                library("figma.code.connect") {
                    artifact(
                        artifact = "code-connect-lib",
                        version = version("figmaCodeConnectLibraryVersion")
                    )
                }
                library("google.protobuf") {
                    artifact(
                        artifact = "protobuf-kotlin",
                        version = version("protobufLibraryVersion")
                    )
                    artifact(
                        artifact = "protoc",
                        version = version("protobufLibraryVersion")
                    )
                }
            }
            root("io") {
                library("cucumber") {
                    artifact(
                        artifact = "cucumber-bom",
                        version = version("cucumberLibraryVersion")
                    )
                    artifactsBundle(
                        "cucumber-java8",
                        "cucumber-junit-platform-engine",
                        alias = "cucumberBundle"
                    )
                }
                library("kotest") {
                    artifactsBundle(
                        "kotest-runner-junit5",
                        "kotest-assertions-core",
                        alias = "kotestBundle",
                        version = version("kotestLibraryVersion")
                    )
                }
                library("mockk") {
                    artifact(
                        artifact = "mockk",
                        version = version("mockkLibraryVersion")
                    )
                }
            }
            root("me") {
                library("tatarka.inject") {
                    artifact(
                        artifact = "kotlin-inject-compiler-ksp",
                        version = version("kotlinInjectLibraryVersion")
                    )
                    artifact(
                        artifact = "kotlin-inject-runtime",
                        version = version("kotlinInjectLibraryVersion")
                    )
                }
            }
            root("org") {
                library("jetbrains.kotlinx") {
                    artifact(
                        artifact = "kotlinx-coroutines-android",
                        version = version("androidCoroutinesLibraryVersion")
                    )
                }
                library("junit.platform") {
                    artifact(
                        artifact = "junit-platform-launcher"
                    )
                    artifact(
                        artifact = "junit-platform-suite"
                    )
                }
            }
        }

        plugins {
            root("com") {
                plugin("android") {
                    plugin(
                        id = "application",
                        version = version("androidGradlePluginVersion")
                    )
                    plugin(
                        id = "library",
                        version = version("androidGradlePluginVersion")
                    )
                }
                plugin(
                    id = "figma.code.connect",
                    version = version("figmaCodeConnectPluginVersion")
                )
                plugin("google") {
                    plugin(
                        id = "devtools.ksp",
                        version = version("kspPluginVersion")
                    )
                    plugin(
                        id = "protobuf",
                        version = version("protobufPluginVersion")
                    )
                }
                plugin("marmatsan") {
                    plugin(
                        id = "android",
                        version = version("gradleConventionPluginVersion")
                    )
                    plugin(
                        id = "bddTest",
                        version = version("gradleConventionPluginVersion")
                    )
                    plugin(
                        id = "compose",
                        version = version("gradleConventionPluginVersion")
                    )
                    plugin(
                        id = "unitTest",
                        version = version("gradleConventionPluginVersion")
                    )
                }
            }
            root("de") {
                plugin(
                    id = "mannodermaus.android-junit5",
                    version = version("junit5PluginVersion")
                )
            }
            root("org") {
                plugin("jetbrains") {
                    plugin(
                        id = "dokka",
                        version = version("dokkaPluginVersion")
                    )
                    plugin(
                        id = "kotlin.plugin.compose",
                        version = version("kotlinVersion")
                    )
                }
            }
        }
    }
