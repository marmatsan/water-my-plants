package com.marmatsan.dependencies.data

import com.marmatsan.dependencies.data.NodeData.ArtifactsGroup
import com.marmatsan.dependencies.data.NodeData.Library
import com.marmatsan.dependencies.plugin.DependenciesPlugin

val androidXLibraryTree = tree(Library(group = "androidx")) {
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

val androidXComposeLibraryTree = tree(Library(group = "androidx")) {
    tree(
        Library(
            group = "activity", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "activity",
                    artifacts = listOf("activity-compose"),
                    version = DependenciesPlugin.Versions.ACTIVITY_COMPOSE_VERSION
                )
            )
        )
    )
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
    ) {
        tree(
            Library(
                group = "compiler", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "compiler",
                        artifacts = listOf("compiler"),
                        version = DependenciesPlugin.Versions.COMPOSE_COMPILER_VERSION
                    )
                )
            )
        )
    }
    tree(
        Library(
            group = "lifecycle", artifactsGroups = listOf(
                ArtifactsGroup(
                    name = "lifecycle",
                    artifacts = listOf("lifecycle-viewmodel-compose", "lifecycle-runtime-compose"),
                    version = DependenciesPlugin.Versions.LIFECYCLE_VERSION
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
                    version = DependenciesPlugin.Versions.NAVIGATION_COMPOSE_VERSION
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
    tree(Library(group = "github")) {
        tree(
            Library(
                group = "skydoves", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "landscapist",
                        artifacts = listOf("landscapist-coil"),
                        version = DependenciesPlugin.Versions.LANDSCAPIST_VERSION
                    )
                )
            )
        )
    }
    tree(Library(group = "google")) {
        tree(
            Library(
                group = "protobuf", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "protobuf",
                        artifacts = listOf("protobuf-java"),
                        version = DependenciesPlugin.Versions.PROTOBUF_LIBRARY_VERSION
                    )
                )
            )
        )
    }
}

val meLibraryTree = tree(Library(group = "me")) {
    tree(Library(group = "tatarka")) {
        tree(
            Library(
                group = "inject", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "inject",
                        artifacts = listOf("kotlin-inject-compiler-ksp", "kotlin-inject-runtime"),
                        version = DependenciesPlugin.Versions.KOTLIN_INJECT_VERSION
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
                        name = "gradle-configuration",
                        artifacts = listOf(
                            "kotlin-gradle-plugin",
                            "kotlin-android-extensions"
                        ),
                        version = DependenciesPlugin.Versions.KOTLIN_VERSION
                    ),
                    ArtifactsGroup(
                        name = "coroutines",
                        artifacts = listOf("kotlinx-coroutines-android"),
                        version = DependenciesPlugin.Versions.ANDROID_COROUTINES_VERSION
                    ),
                    ArtifactsGroup(
                        name = "serialization",
                        artifacts = listOf("kotlinx-serialization-json"),
                        version = DependenciesPlugin.Versions.SERIALIZATION_VERSION
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
    tree(Library(group = "realm")) {
        tree(
            Library(
                group = "kotlin", artifactsGroups = listOf(
                    ArtifactsGroup(
                        name = "realm",
                        artifacts = listOf("library-base"),
                        version = DependenciesPlugin.Versions.REALM_VERSION
                    )
                )
            )
        )
    }
}

val nonComposeLibraryTrees = listOf(
    androidXLibraryTree,
    comLibraryTree,
    meLibraryTree,
    orgLibraryTree,
    ioLibraryTree
)
val composeLibraryTrees = listOf(
    androidXComposeLibraryTree
)