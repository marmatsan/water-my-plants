package com.marmatsan.dependencies.data

import com.marmatsan.dependencies.plugin.DependenciesPlugin
import com.marmatsan.dependencies.data.NodeData.Plugin as Plugin

val comPluginTree = tree(Plugin(id = "com")) {
    tree(Plugin(id = "android")) {
        tree(Plugin(id = "application", version = DependenciesPlugin.Versions.applicationVersion))
        tree(Plugin(id = "library", version = DependenciesPlugin.Versions.applicationVersion))
    }
    tree(Plugin(id = "google")) {
        tree(Plugin(id = "devtools")) {
            tree(Plugin(id = "ksp", version = DependenciesPlugin.Versions.kspVersion))
        }
        tree(Plugin(id = "dagger")) {
            tree(Plugin(id = "hilt")) {
                tree(Plugin(id = "android", version = DependenciesPlugin.Versions.daggerHiltVersion))
            }
        }
        tree(Plugin(id = "relay", version = DependenciesPlugin.Versions.relayVersion))

        tree(Plugin(id = "protobuf", version = DependenciesPlugin.Versions.protobufVersion))
    }
}

val orgPluginTree = tree(Plugin(id = "org")) {
    tree(Plugin(id = "jetbrains")) {
        tree(Plugin(id = "kotlin")) {
            tree(Plugin(id = "android", version = DependenciesPlugin.Versions.kotlinVersion))
            tree(Plugin(id = "plugin")) {
                tree(Plugin(id = "serialization", version = DependenciesPlugin.Versions.kotlinVersion))
                tree(Plugin(id = "parcelize", version = DependenciesPlugin.Versions.kotlinVersion))
            }
        }
    }
}

val pluginTrees = listOf(comPluginTree, orgPluginTree)