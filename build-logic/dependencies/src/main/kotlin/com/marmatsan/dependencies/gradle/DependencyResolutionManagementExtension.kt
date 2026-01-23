package com.marmatsan.dependencies.gradle

import com.marmatsan.dependencies.tree.tree.getLibraries
import com.marmatsan.dependencies.tree.tree.getPlugins
import com.marmatsan.dependencies.version.Versions
import org.gradle.api.initialization.resolve.DependencyResolutionManagement

fun DependencyResolutionManagement.configureVersionCatalogs(
    versions: Versions
) {
    val libraryTrees = com.marmatsan.dependencies.plugin.libraryTrees(versions)
    val pluginTrees = com.marmatsan.dependencies.plugin.pluginTrees(versions)

    versionCatalogs {
        create("libs") {
            libraryTrees.forEach { registerLibraries(it.getLibraries()) }
        }
        create("plugins") {
            pluginTrees.forEach { registerPlugins(it.getPlugins()) }
        }
    }
}