package com.marmatsan.dependencies

import com.marmatsan.dependencies.tree.library.libraryTree
import com.marmatsan.dependencies.Versions

fun libraryTrees(
    versions: Versions
) = listOf(
    androidxLibrariesTree(versions),
    comLibrariesTree(versions),
    ioLibrariesTree(versions),
    meLibrariesTree(versions),
    orgLibrariesTree(versions)
)

private fun androidxLibrariesTree(
    versions: Versions
) = libraryTree("androidx") {
    library("activity") {
        artifact(
            "activity-compose",
            version = versions.activityComposeVersion
        )
    }
    library("compose") {
        artifact(
            "compose-bom",
            version = versions.composeBomVersion
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
    }
    library("core") {
        artifact(
            "core-ktx",
            version = versions.coreKtxVersion
        )
    }
    library("lifecycle") {
        artifact(
            "lifecycle-runtime-ktx",
            version = versions.lifecycleVersion
        )
        artifact(
            "lifecycle-viewmodel-compose",
            version = versions.lifecycleVersion
        )
        artifact(
            "lifecycle-runtime-compose",
            version = versions.lifecycleVersion
        )
    }
    library("navigation") {
        artifact(
            "navigation-compose",
            version = versions.navigationComposeVersion
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
    library("willowtreeapps") {
        library("assertk") {
            artifact(
                "assertk",
                version = versions.assertkVersion
            )
        }
    }
}

private fun ioLibrariesTree(
    versions: Versions
) = libraryTree("io") {
    library("mockk") {
        artifact(
            "mockk",
            version = versions.mockkVersion
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
                version = versions.kotlinInjectVersion
            )
            artifact(
                artifact = "kotlin-inject-runtime",
                version = versions.kotlinInjectVersion
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
                version = versions.androidCoroutinesVersion
            )
        }
    }
    library("junit") {
        artifact(
            "junit-bom",
            version = versions.junit5BomVersion
        )
        library("jupiter") {
            artifactsBundle(
                "junit-jupiter-api",
                "junit-jupiter-params",
                "junit-jupiter-engine",
                alias = "jupiterBundle"
            )
        }
        library("platform") {
            artifact(
                "junit-platform-launcher"
            )
        }
    }
}