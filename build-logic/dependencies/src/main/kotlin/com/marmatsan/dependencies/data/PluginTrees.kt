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
        tree(Plugin(id = "protobuf", version = DependenciesPlugin.Versions.PROTOBUF_PLUGIN_VERSION))
    }
}

val dePluginTree = tree(Plugin(id = "de")) {
    tree(Plugin(id = "mannodermaus")) {
        tree(
            Plugin(
                id = "android-junit5",
                version = DependenciesPlugin.Versions.JUNIT5_PLUGIN_VERSION
            )
        )
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
    dePluginTree,
    orgPluginTree,
    ioPluginTree
)