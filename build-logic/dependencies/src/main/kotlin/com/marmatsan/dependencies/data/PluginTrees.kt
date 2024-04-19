package com.marmatsan.dependencies.data

import com.marmatsan.dependencies.data.NodeData.Plugin
import com.marmatsan.dependencies.plugin.DependenciesPlugin

val comPluginTree = tree(Plugin(id = "com")) {
    tree(Plugin(id = "android")) {
        tree(Plugin(id = "application", version = DependenciesPlugin.Versions.APPLICATION_VERSION))
        tree(Plugin(id = "library", version = DependenciesPlugin.Versions.APPLICATION_VERSION))
    }
    tree(Plugin(id = "google")) {
        tree(Plugin(id = "devtools")) {
            tree(Plugin(id = "ksp", version = DependenciesPlugin.Versions.KSP_VERSION))
        }
    }
}

val orgPluginTree = tree(Plugin(id = "org")) {
    tree(Plugin(id = "jetbrains")) {
        tree(Plugin(id = "kotlin")) {
            tree(Plugin(id = "android", version = DependenciesPlugin.Versions.KOTLIN_VERSION))
            tree(Plugin(id = "plugin")) {
                tree(
                    Plugin(
                        id = "serialization",
                        version = DependenciesPlugin.Versions.KOTLIN_VERSION
                    )
                )
                tree(Plugin(id = "parcelize", version = DependenciesPlugin.Versions.KOTLIN_VERSION))
            }
        }
    }
}

val ioPluginTree = tree(Plugin(id = "io")) {
    tree(Plugin(id = "realm")) {
        tree(Plugin(id = "kotlin", version = DependenciesPlugin.Versions.REALM_VERSION))
    }
}

val pluginTrees = listOf(
    comPluginTree,
    orgPluginTree,
    ioPluginTree
)