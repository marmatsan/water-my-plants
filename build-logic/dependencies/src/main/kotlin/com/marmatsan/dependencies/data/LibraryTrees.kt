package com.marmatsan.dependencies.data

import com.marmatsan.dependencies.plugin.DependenciesPlugin
import com.marmatsan.dependencies.data.NodeData.Library as Library
import com.marmatsan.dependencies.data.NodeData.ArtifactsGroup as ArtifactsGroup

val androidXLibraryTree = tree(Library(group = "androidx")) {
    tree(
        Library(
            group = "core", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "core-ktx",
                    artifacts = listOf("core-ktx"),
                    version = DependenciesPlugin.Versions.coreKtxVersion
                ),
                ArtifactsGroup(
                    name = "core-splashscreen",
                    artifacts = listOf("core-splashscreen"),
                    version = DependenciesPlugin.Versions.coreSplashscreenVersion
                )
            )
        )
    )
    tree(
        Library(
            group = "datastore", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "datastore",
                    artifacts = listOf("datastore", "datastore-core"),
                    version = DependenciesPlugin.Versions.datastoreVersion
                )
            )
        )
    )
    tree(
        Library(
            group = "lifecycle", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "lifecycle",
                    artifacts = listOf("lifecycle-runtime-ktx"),
                    version = DependenciesPlugin.Versions.lifecycleVersion
                )
            )
        )
    )
    tree(
        Library(
            group = "room", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "room",
                    artifacts = listOf("room-runtime", "room-compiler", "room-ktx"),
                    version = DependenciesPlugin.Versions.roomVersion
                )
            )
        )
    )
}

val androidXComposeLibraryTree = tree(Library(group = "androidx")) {
    tree(
        Library(
            group = "activity", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "activity",
                    artifacts = listOf("activity-compose"),
                    version = DependenciesPlugin.Versions.activityComposeVersion
                )
            )
        )
    )
    tree(
        Library(
            group = "compose"
        )
    ) {
        tree(
            Library(
                group = "animation", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "animation",
                        artifacts = listOf("animation"),
                        version = DependenciesPlugin.Versions.composeVersion
                    )
                )
            )
        )
        tree(
            Library(
                group = "compiler", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "compiler",
                        artifacts = listOf("compiler"),
                        version = DependenciesPlugin.Versions.composeCompilerVersion
                    )
                )
            )
        )
        tree(
            Library(
                group = "foundation", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "foundation",
                        artifacts = listOf("foundation"),
                        version = DependenciesPlugin.Versions.composeVersion
                    )
                )
            )
        )
        tree(
            Library(
                group = "material3", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "material3",
                        artifacts = listOf("material3"),
                        version = DependenciesPlugin.Versions.material3Version
                    )
                )
            )
        )
        tree(
            Library(
                group = "runtime", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "runtime",
                        artifacts = listOf("runtime"),
                        version = DependenciesPlugin.Versions.composeVersion
                    )
                )
            )
        )
        tree(
            Library(
                group = "ui", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "ui",
                        artifacts = listOf(
                            "ui",
                            "ui-tooling",
                            "ui-tooling-preview",
                            "ui-geometry",
                            "ui-graphics",
                            "ui-unit",
                            "ui-text-google-fonts"
                        ),
                        version = DependenciesPlugin.Versions.composeVersion
                    )
                )
            )
        )
    }
    tree(
        Library(
            group = "hilt", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "hilt",
                    artifacts = listOf("hilt-navigation-compose"),
                    version = DependenciesPlugin.Versions.hiltNavigationComposeVersion
                )
            )
        )
    )
    tree(
        Library(
            group = "lifecycle", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "lifecycle",
                    artifacts = listOf("lifecycle-viewmodel-compose", "lifecycle-runtime-compose"),
                    version = DependenciesPlugin.Versions.lifecycleVersion
                )
            )
        )
    )
    tree(
        Library(
            group = "navigation", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "navigation",
                    artifacts = listOf("navigation-compose"),
                    version = DependenciesPlugin.Versions.navigationComposeVersion
                )
            )
        )
    )
}

val comLibraryTree = tree(Library(group = "com")) {
    tree(Library(group = "android")) {
        tree(Library(group = "tools")) {
            tree(
                Library(
                    group = "build",
                    artifactsGroups = listOf(
                        ArtifactsGroup(
                            name = "build",
                            artifacts = listOf("gradle"),
                            version = DependenciesPlugin.Versions.applicationVersion
                        )
                    )
                )
            )
        }
    }
    tree(Library(group = "google")) {
        tree(
            Library(
                group = "dagger", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "dagger",
                        artifacts = listOf("hilt-android", "hilt-android-compiler"),
                        version = DependenciesPlugin.Versions.daggerHiltVersion
                    )
                )
            )
        )
        tree(
            Library(
                group = "protobuf", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "protobuf",
                        artifacts = listOf("protobuf-javalite"),
                        version = DependenciesPlugin.Versions.protobufJavaliteVersion
                    )
                )
            )
        )
    }
    tree(Library(group = "willowtreeapps")) {
        tree(
            Library(
                group = "assertk", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "assertk",
                        artifacts = listOf("assertk"),
                        version = DependenciesPlugin.Versions.assertkVersion
                    )
                )
            )
        )
    }
}

val orgLibraryTree = tree(Library(group = "org")) {
    tree(Library(group = "jetbrains")) {
        tree(
            Library(
                group = "kotlinx", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "serialization",
                        artifacts = listOf("kotlinx-serialization-json"),
                        version = DependenciesPlugin.Versions.kotlinxSerializationJsonVersion
                    ),
                    ArtifactsGroup(
                        name = "gradle-configuration",
                        artifacts = listOf("kotlin-gradle-plugin", "kotlin-android-extensions"),
                        version = DependenciesPlugin.Versions.kotlinVersion
                    )
                )
            )
        )
    }
    tree(Library(group = "junit")) {
        tree(
            Library(
                group = "jupiter", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "jupiter",
                        artifacts = listOf("junit-jupiter"),
                        version = DependenciesPlugin.Versions.junit5Version
                    )
                )
            )
        )
    }
}

val ioLibraryTree = tree(Library(group = "io")) {
    tree(
        Library(
            group = "mockk", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "mockk",
                    artifacts = listOf("mockk"),
                    version = DependenciesPlugin.Versions.mockkVersion
                )
            )
        )
    )
    tree(
        Library(
            group = "coil-kt", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "coil-compose",
                    artifacts = listOf("coil-compose"),
                    version = DependenciesPlugin.Versions.coilVersion
                )
            )
        )
    )
    tree(
        Library(
            group = "ktor", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "ktor",
                    artifacts = listOf(
                        "ktor-client-core",
                        "ktor-client-android",
                        "ktor-client-serialization",
                        "ktor-client-content-negotiation",
                        "ktor-serialization-kotlinx-json",
                        "ktor-client-logging"
                    ),
                    version = DependenciesPlugin.Versions.ktorVersion
                )
            )
        )
    )
}

val nonComposeLibraryTrees =
    listOf(androidXLibraryTree, comLibraryTree, orgLibraryTree, ioLibraryTree)
val composeLibraryTrees = listOf(androidXComposeLibraryTree)