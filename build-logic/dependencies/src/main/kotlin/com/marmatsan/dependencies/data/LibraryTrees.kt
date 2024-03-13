package com.marmatsan.dependencies.data

import com.marmatsan.dependencies.data.NodeData.ArtifactsGroup
import com.marmatsan.dependencies.data.NodeData.Library
import com.marmatsan.dependencies.plugin.DependenciesPlugin

val androidXLibraryTree = tree(Library(group = "androidx")) {
    tree(
        Library(
            group = "compose", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "compose-bom",
                    artifacts = listOf("compose-bom"),
                    version = DependenciesPlugin.Versions.COMPOSE_BOM_VERSION
                )
            )
        )
    )
    tree(
        Library(
            group = "core", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "core-ktx",
                    artifacts = listOf("core-ktx"),
                    version = DependenciesPlugin.Versions.CORE_KTX_VERSION
                ),
                ArtifactsGroup(
                    name = "core-splashscreen",
                    artifacts = listOf("core-splashscreen"),
                    version = DependenciesPlugin.Versions.CORE_SPLASHSCREEN_VERSION
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
                    version = DependenciesPlugin.Versions.DATASTORE_VERSION
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
                    version = DependenciesPlugin.Versions.LIFECYCLE_VERSION
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
                            version = DependenciesPlugin.Versions.APPLICATION_VERSION
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
                        version = DependenciesPlugin.Versions.DAGGER_HILT_VERSION
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
                        version = DependenciesPlugin.Versions.PROTOBUF_JAVALITE_VERSION
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
                        version = DependenciesPlugin.Versions.ASSERTK_VERSION
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
                        version = DependenciesPlugin.Versions.KOTLINX_SERIALIZATION_JSON_VERSION
                    ),
                    ArtifactsGroup(
                        name = "gradle-configuration",
                        artifacts = listOf("kotlin-gradle-plugin", "kotlin-android-extensions"),
                        version = DependenciesPlugin.Versions.KOTLIN_VERSION
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
                        version = DependenciesPlugin.Versions.JUNIT5_VERSION
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
                    version = DependenciesPlugin.Versions.MOCKK_VERSION
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
                    version = DependenciesPlugin.Versions.COIL_VERSION
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
                    version = DependenciesPlugin.Versions.KTOR_VERSION
                )
            )
        )
    )
}

val libraryTrees = listOf(androidXLibraryTree, comLibraryTree, orgLibraryTree, ioLibraryTree)
