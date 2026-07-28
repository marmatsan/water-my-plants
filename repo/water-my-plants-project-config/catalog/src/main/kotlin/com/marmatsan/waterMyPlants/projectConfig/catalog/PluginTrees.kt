package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.dependencies.tree.dsl.plugin.pluginTree

internal fun pluginTrees(
    versions: Versions,
) = listOf(
    comPluginTree(
        versions = versions,
    ),
    dePluginTree(
        versions = versions,
    ),
    orgPluginTree(
        versions = versions,
    ),
)

private fun comPluginTree(
    versions: Versions,
) = pluginTree(
    rootId = "com",
) {
    plugin("android") {
        plugin(
            id = "application",
            version = versions.androidGradlePluginVersion,
        )
        plugin(
            id = "library",
            version = versions.androidGradlePluginVersion,
        )
    }
    plugin("figma.code") {
        plugin(
            id = "connect",
            version = versions.figmaCodeConnectPluginVersion,
        )
    }
    plugin("google") {
        plugin("devtools") {
            plugin(
                id = "ksp",
                version = versions.kspPluginVersion,
            )
        }
        plugin(
            id = "protobuf",
            version = versions.protobufPluginVersion,
        )
    }
}

private fun dePluginTree(
    versions: Versions,
) = pluginTree(
    rootId = "de",
) {
    plugin("mannodermaus") {
        plugin(
            id = "android-junit5",
            version = versions.junit5PluginVersion,
        )
    }
}

private fun orgPluginTree(
    versions: Versions,
) = pluginTree(
    rootId = "org",
) {
    plugin("jetbrains") {
        plugin(
            id = "dokka",
            version = versions.dokkaPluginVersion,
        )
        plugin("kotlin.plugin") {
            plugin(
                id = "compose",
                version = versions.kotlinVersion,
            )
        }
    }
}
