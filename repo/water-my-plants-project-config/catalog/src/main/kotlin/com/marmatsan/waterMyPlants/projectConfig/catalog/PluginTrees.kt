package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.dependencies.tree.dsl.plugin.pluginTree

internal fun pluginTrees(
    versions: Versions
) = listOf(
    comPluginTree(
        versions = versions
    ),
    dePluginTree(
        versions = versions
    ),
    orgPluginTree(
        versions = versions
    )
)

private fun comPluginTree(
    versions: Versions
) = pluginTree(
    rootId = "com"
) {
    plugin("android") {
        plugin(
            id = "application",
            version = versions.androidGradlePluginVersion
        )
        plugin(
            id = "library",
            version = versions.androidGradlePluginVersion
        )
    }
    plugin(
        id = "figma.code.connect",
        version = versions.figmaCodeConnectPluginVersion
    )
    plugin("google") {
        plugin(
            id = "devtools.ksp",
            version = versions.kspPluginVersion
        )
        plugin(
            id = "protobuf",
            version = versions.protobufPluginVersion
        )
    }
    plugin("marmatsan") {
        plugin(
            id = "android",
            version = versions.gradleConventionPluginVersion
        )
        plugin(
            id = "bddTest",
            version = versions.gradleConventionPluginVersion
        )
        plugin(
            id = "compose",
            version = versions.gradleConventionPluginVersion
        )
        plugin(
            id = "unitTest",
            version = versions.gradleConventionPluginVersion
        )
    }
}

private fun dePluginTree(
    versions: Versions
) = pluginTree(
    rootId = "de"
) {
    plugin(
        id = "mannodermaus.android-junit5",
        version = versions.junit5PluginVersion
    )
}

private fun orgPluginTree(
    versions: Versions
) = pluginTree(
    rootId = "org"
) {
    plugin("jetbrains") {
        plugin(
            id = "dokka",
            version = versions.dokkaPluginVersion
        )
        plugin(
            id = "kotlin.plugin.compose",
            version = versions.kotlinVersion
        )
    }
}
