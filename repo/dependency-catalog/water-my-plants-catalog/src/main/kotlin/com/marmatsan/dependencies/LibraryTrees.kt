package com.marmatsan.dependencies

import com.marmatsan.dependencies.tree.dsl.library.libraryTree

internal fun libraryTrees(
    versions: Versions
) = listOf(
    androidxLibrariesTree(
        versions = versions
    ),
    comLibrariesTree(
        versions = versions
    ),
    ioLibrariesTree(
        versions = versions
    ),
    meLibrariesTree(
        versions = versions
    ),
    orgLibrariesTree(
        versions = versions
    )
)

private fun androidxLibrariesTree(
    versions: Versions
) = libraryTree("androidx") {
    library("activity") {
        artifact(
            "activity-compose",
            version = versions.activityComposeLibraryVersion
        )
    }
    library("compose") {
        artifact(
            "compose-bom",
            version = versions.composeBomLibraryVersion
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
                "material3"
            )
        }
        library("material") {
            artifact(
                "material-icons-core"
            )
        }
    }
    library("core") {
        artifact(
            "core-ktx",
            version = versions.coreKtxLibraryVersion
        )
    }
    library("lifecycle") {
        artifact(
            "lifecycle-runtime-ktx",
            version = versions.lifecycleLibraryVersion
        )
        artifact(
            "lifecycle-viewmodel-compose",
            version = versions.lifecycleLibraryVersion
        )
        artifact(
            "lifecycle-runtime-compose",
            version = versions.lifecycleLibraryVersion
        )
    }
    library("navigation") {
        artifact(
            "navigation-compose",
            version = versions.navigationComposeLibraryVersion
        )
    }
}

private fun comLibrariesTree(
    versions: Versions
) = libraryTree("com") {
    library("figma") {
        library("code") {
            library("connect") {
                artifact(
                    "code-connect-lib",
                    version = versions.figmaCodeConnectLibraryVersion
                )
            }
        }
    }
    library("google") {
        library("protobuf") {
            artifact(
                "protobuf-kotlin",
                version = versions.protobufLibraryVersion
            )
            artifact(
                "protoc",
                version = versions.protobufLibraryVersion
            )
        }
    }
}

private fun ioLibrariesTree(
    versions: Versions
) = libraryTree("io") {
    library("cucumber") {
        artifact(
            "cucumber-bom",
            version = versions.cucumberLibraryVersion
        )
        artifact(
            "cucumber-java8"
        )
        artifact(
            "cucumber-junit-platform-engine"
        )
    }
    library("kotest") {
        artifact(
            "kotest-runner-junit5",
            version = versions.kotestLibraryVersion
        )
        artifact(
            "kotest-assertions-core",
            version = versions.kotestLibraryVersion
        )
    }
    library("mockk") {
        artifact(
            "mockk",
            version = versions.mockkLibraryVersion
        )
    }
}

private fun meLibrariesTree(
    versions: Versions
) = libraryTree("me") {
    library("tatarka") {
        library("inject") {
            artifact(
                artifact = "kotlin-inject-compiler-ksp",
                version = versions.kotlinInjectLibraryVersion
            )
            artifact(
                artifact = "kotlin-inject-runtime",
                version = versions.kotlinInjectLibraryVersion
            )
        }
    }
}

private fun orgLibrariesTree(
    versions: Versions
) = libraryTree("org") {
    library("jetbrains") {
        library("kotlinx") {
            artifact(
                artifact = "kotlinx-coroutines-android",
                version = versions.androidCoroutinesLibraryVersion
            )
        }
    }
    library("junit") {
        library("platform") {
            artifact(
                "junit-platform-launcher"
            )
            artifact(
                "junit-platform-suite"
            )
        }
    }
}
