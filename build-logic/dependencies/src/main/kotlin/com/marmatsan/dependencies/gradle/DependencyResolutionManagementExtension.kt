package com.marmatsan.dependencies.gradle

import com.marmatsan.dependencies.libraryTrees
import com.marmatsan.dependencies.pluginTrees
import com.marmatsan.dependencies.tree.tree.getLibraries
import com.marmatsan.dependencies.tree.tree.getPlugins
import com.marmatsan.dependencies.version.Versions
import org.gradle.api.initialization.resolve.DependencyResolutionManagement

fun DependencyResolutionManagement.configureVersionCatalogs(
    versions: Versions
) {
    val libraryTrees = libraryTrees(versions)
    val pluginTrees = pluginTrees(versions)

    versionCatalogs {
        create("libs") {
            libraryTrees.forEach { registerLibraries(it.getLibraries()) }
        }
        create("plugins") {
            pluginTrees.forEach { registerPlugins(it.getPlugins()) }
        }
    }
}