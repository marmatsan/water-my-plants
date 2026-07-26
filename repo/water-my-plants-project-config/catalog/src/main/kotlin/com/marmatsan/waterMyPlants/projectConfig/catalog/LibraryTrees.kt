package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.dependencies.tree.dsl.library.libraryTree

internal fun libraryTrees(
    versions: Versions,
) = listOf(
    androidxLibrariesTree(
        versions = versions,
    ),
    comLibrariesTree(
        versions = versions,
    ),
    ioLibrariesTree(
        versions = versions,
    ),
    meLibrariesTree(
        versions = versions,
    ),
    orgLibrariesTree(
        versions = versions,
    ),
)

private fun androidxLibrariesTree(
    versions: Versions,
) = libraryTree(
    rootGroup = "androidx",
) {
    library("activity") {
        artifact(
            artifact = "activity-compose",
            version = versions.activityComposeLibraryVersion,
        )
    }
    library("compose") {
        artifact(
            artifact = "compose-bom",
            version = versions.composeBomLibraryVersion,
        )
        library("ui") {
            artifactsBundle(
                "ui",
                "ui-graphics",
                "ui-tooling",
                "ui-tooling-preview",
                alias = "composeBundle",
            )
        }
        library("material3") {
            artifact(
                artifact = "material3",
            )
        }
        library("material") {
            artifact(
                artifact = "material-icons-core",
            )
        }
    }
    library("core") {
        artifact(
            artifact = "core-ktx",
            version = versions.coreKtxLibraryVersion,
        )
    }
    library("lifecycle") {
        artifact(
            artifact = "lifecycle-runtime-ktx",
            version = versions.lifecycleLibraryVersion,
        )
        artifact(
            artifact = "lifecycle-viewmodel-compose",
            version = versions.lifecycleLibraryVersion,
        )
        artifact(
            artifact = "lifecycle-runtime-compose",
            version = versions.lifecycleLibraryVersion,
        )
    }
    library("navigation") {
        artifact(
            artifact = "navigation-compose",
            version = versions.navigationComposeLibraryVersion,
        )
    }
}

private fun comLibrariesTree(
    versions: Versions,
) = libraryTree(
    rootGroup = "com",
) {
    library("figma") {
        library("code") {
            library("connect") {
                artifact(
                    artifact = "code-connect-lib",
                    version = versions.figmaCodeConnectLibraryVersion,
                )
            }
        }
    }
    library("google") {
        library("protobuf") {
            artifact(
                artifact = "protobuf-kotlin",
                version = versions.protobufLibraryVersion,
            )
            artifact(
                artifact = "protoc",
                version = versions.protobufLibraryVersion,
            )
        }
    }
}

private fun ioLibrariesTree(
    versions: Versions,
) = libraryTree(
    rootGroup = "io",
) {
    library("cucumber") {
        artifact(
            artifact = "cucumber-bom",
            version = versions.cucumberLibraryVersion,
        )
        artifact(
            artifact = "cucumber-java8",
        )
        artifact(
            artifact = "cucumber-junit-platform-engine",
        )
    }
    library("kotest") {
        artifact(
            artifact = "kotest-runner-junit5",
            version = versions.kotestLibraryVersion,
        )
        artifact(
            artifact = "kotest-assertions-core",
            version = versions.kotestLibraryVersion,
        )
    }
    library("mockk") {
        artifact(
            artifact = "mockk",
            version = versions.mockkLibraryVersion,
        )
    }
}

private fun meLibrariesTree(
    versions: Versions,
) = libraryTree(
    rootGroup = "me",
) {
    library("tatarka") {
        library("inject") {
            artifact(
                artifact = "kotlin-inject-compiler-ksp",
                version = versions.kotlinInjectLibraryVersion,
            )
            artifact(
                artifact = "kotlin-inject-runtime",
                version = versions.kotlinInjectLibraryVersion,
            )
        }
    }
}

private fun orgLibrariesTree(
    versions: Versions,
) = libraryTree(
    rootGroup = "org",
) {
    library("jetbrains") {
        library("kotlinx") {
            artifact(
                artifact = "kotlinx-coroutines-android",
                version = versions.androidCoroutinesLibraryVersion,
            )
        }
    }
    library("junit") {
        library("platform") {
            artifact(
                artifact = "junit-platform-launcher",
            )
            artifact(
                artifact = "junit-platform-suite",
            )
        }
    }
}
