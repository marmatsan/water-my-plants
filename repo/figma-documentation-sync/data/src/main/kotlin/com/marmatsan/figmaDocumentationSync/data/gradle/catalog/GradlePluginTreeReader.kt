package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import com.marmatsan.figmaDocumentationSync.data.gradle.nestedGradleBuildRoots
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import me.tatarka.inject.annotations.Inject
import java.io.File

/**
 * Discovers regular Gradle plugin ids implemented by repository-owned included
 * builds.
 *
 * Unlike [GradleConventionPluginTreeReader], this reader excludes implementation
 * classes whose name marks them as convention plugins.
 */
@Inject
class GradlePluginTreeReader {
    /** Builds a plugin tree from ids and their consuming module identities. */
    fun readPluginTree(
        rootDir: File,
        includedPluginIds: Set<String> = emptySet(),
        usageByPluginId: Map<String, Set<String>> = emptyMap()
    ): PluginCatalogTree {
        val pluginIds =
            rootDir
                .nestedGradleBuildRoots()
                .flatMap(File::walkTopDown)
                .filter { file -> file.isFile && file.name == BUILD_FILE_NAME }
                .map { file -> file.readText() }
                .filter(GradlePluginDeclarationParser::hasRegularPluginImplementation)
                .map(GradlePluginDeclarationParser::pluginIds)
                .flatten()
                .filter { pluginId -> includedPluginIds.isEmpty() || pluginId in includedPluginIds }
                .toSet()

        return PluginCatalogTree(
            roots =
                PluginCatalogTreeBuilder.build(
                    pluginIds = pluginIds,
                    usageByPluginId = usageByPluginId
                )
        )
    }

    private companion object {
        const val BUILD_FILE_NAME = "build.gradle.kts"
    }
}
