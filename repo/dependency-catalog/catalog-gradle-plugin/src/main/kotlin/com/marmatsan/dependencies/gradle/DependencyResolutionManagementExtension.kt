package com.marmatsan.dependencies.gradle

import com.marmatsan.dependencies.catalog.api.DependencyCatalog
import com.marmatsan.dependencies.catalog.api.LibraryCatalogNode
import com.marmatsan.dependencies.catalog.api.PluginCatalogNode
import org.gradle.api.initialization.resolve.DependencyResolutionManagement

/**
 * Registers [catalog] as separate library and plugin Gradle version catalogs.
 *
 * @param catalog Immutable hierarchical catalog to flatten and register.
 * @param librariesCatalogName Name of the generated library catalog.
 * @param pluginsCatalogName Name of the generated plugin catalog.
 */
fun DependencyResolutionManagement.configureVersionCatalogs(
    catalog: DependencyCatalog,
    librariesCatalogName: String = "libs",
    pluginsCatalogName: String = "plugins",
) {
    versionCatalogs.create(librariesCatalogName) { catalogBuilder ->
        catalogBuilder.registerLibraries(
            libraries = catalog.libraries.toLibraries(),
        )
    }
    versionCatalogs.create(pluginsCatalogName) { catalogBuilder ->
        catalogBuilder.registerPlugins(
            plugins = catalog.plugins.toPlugins(),
        )
    }
}

private fun List<LibraryCatalogNode>.toLibraries(
    parentGroup: String = "",
): List<ResolvedLibrary> =
    flatMap { node ->
        val group =
            listOf(
                parentGroup,
                node.group,
            ).filter(String::isNotBlank).joinToString(".")
        listOf(
            ResolvedLibrary(
                group = group,
                entries = node.entries,
            ),
        ) + node.children.toLibraries(group)
    }

private fun List<PluginCatalogNode>.toPlugins(
    parentId: String = "",
): List<ResolvedPlugin> =
    flatMap { node ->
        val id =
            listOf(
                parentId,
                node.id,
            ).filter(String::isNotBlank).joinToString(".")
        listOfNotNull(
            node.version?.let { version ->
                ResolvedPlugin(
                    id = id,
                    version = version,
                )
            },
        ) +
            node.children.toPlugins(id)
    }
