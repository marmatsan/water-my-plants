package com.marmatsan.dependencies

import com.marmatsan.dependencies.tree.dsl.plugin.pluginTree

fun pluginTrees(
    versions: Versions
) = listOf(
    comPluginTree(versions),
    dePluginTree(versions),
    orgPluginTree(versions)
)

private fun comPluginTree(
    versions: Versions
) = pluginTree("com") {
    plugin("android") {
        plugin(
            id = "application",
            version = versions.androidGradlePlugin
        )
        plugin(
            id = "library",
            version = versions.androidGradlePlugin
        )
    }
    plugin("figma") {
        plugin("code") {
            plugin(
                id = "connect",
                version = versions.figmaCodeConnectPluginVersion
            )
        }
    }
    plugin("google") {
        plugin("devtools") {
            plugin(
                id = "ksp",
                version = versions.kspVersion
            )
        }
        plugin(
            id = "protobuf",
            version = versions.protobufPluginVersion
        )
    }
}

private fun dePluginTree(
    versions: Versions
) = pluginTree("de") {
    plugin("mannodermaus") {
        plugin(
            id = "android-junit5",
            version = versions.junit5PluginVersion
        )
    }
}

private fun orgPluginTree(
    versions: Versions
) = pluginTree("org") {
    plugin("jetbrains") {
        plugin("kotlin") {
            plugin(
                id = "android",
                version = versions.kotlinVersion
            )
            plugin("plugin") {
                plugin(
                    id = "compose",
                    version = versions.kotlinVersion
                )
            }
        }
        plugin("kotlinx") {
            plugin(
                id = "kover",
                version = versions.kotlinxCoverVersion
            )
        }
    }
}
