package com.marmatsan.dependencies.gradle

import com.marmatsan.dependencies.libraryTrees
import com.marmatsan.dependencies.pluginTrees
import com.marmatsan.dependencies.tree.node.toDependencyLibraries
import com.marmatsan.dependencies.tree.node.toDependencyPlugins
import com.marmatsan.dependencies.Versions
import org.gradle.api.initialization.resolve.DependencyResolutionManagement

fun DependencyResolutionManagement.configureVersionCatalogs(
    versions: Versions
) {
    val libraryTrees = libraryTrees(versions)
    val pluginTrees = pluginTrees(versions)

    versionCatalogs {
        create("libs") {
            libraryTrees.forEach { registerLibraries(it.toDependencyLibraries()) }
        }
        create("plugins") {
            pluginTrees.forEach { registerPlugins(it.toDependencyPlugins()) }
        }
    }
}