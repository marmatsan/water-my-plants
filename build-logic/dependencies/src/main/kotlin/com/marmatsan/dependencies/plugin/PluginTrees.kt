package com.marmatsan.dependencies.plugin

import com.marmatsan.dependencies.tree.plugin.pluginTree
import com.marmatsan.dependencies.version.Versions

fun pluginTrees(versions: Versions) = listOf(
    comPluginTree(versions),
    dePluginTree(versions),
    orgPluginTree(versions)
)

private fun comPluginTree(versions: Versions) = pluginTree("com") {
    plugin("android") {
        plugin(
            id = "application",
            version = versions.applicationVersion
        )
        plugin(
            id = "library",
            version = versions.applicationVersion
        )
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

private fun dePluginTree(versions: Versions) = pluginTree("de") {
    plugin("mannodermaus") {
        plugin(
            id = "android-junit5",
            version = versions.junit5PluginVersion
        )
    }
}

private fun orgPluginTree(versions: Versions) = pluginTree("org") {
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
    }
}
