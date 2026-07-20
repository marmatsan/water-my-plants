package com.marmatsan.dependencies.gradle

import com.marmatsan.dependencies.catalog.DependencyCatalogTrees
import com.marmatsan.dependencies.tree.node.toDependencyLibraries
import com.marmatsan.dependencies.tree.node.toDependencyPlugins
import org.gradle.api.initialization.resolve.DependencyResolutionManagement

fun DependencyResolutionManagement.configureVersionCatalogs(
    catalog: DependencyCatalogTrees,
) {
    versionCatalogs {
        create("libs") {
            catalog.libraries.forEach {
                registerLibraries(
                    libraries = it.toDependencyLibraries(),
                )
            }
        }
        create("plugins") {
            catalog.plugins.forEach {
                registerPlugins(
                    plugins = it.toDependencyPlugins(),
                )
            }
        }
    }
}
