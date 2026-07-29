package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import me.tatarka.inject.annotations.Inject
import java.io.File

/**
 * Discovers Gradle convention plugin ids implemented by an included build.
 *
 * Convention plugins are repository-owned build APIs. Including them in
 * `design-model.json` lets Figma document which project modules use those
 * conventions.
 */
@Inject
class GradleConventionPluginTreeReader {
    /** Builds the convention-plugin tree and attaches applying module identities. */
    fun readPluginTree(
        rootDir: File,
        usageByPluginId: Map<String, Set<String>> = emptyMap()
    ): PluginCatalogTree {
        val pluginIds =
            rootDir
                .walkTopDown()
                .filter { file -> file.isFile && file.name == BUILD_FILE_NAME }
                .map { file -> file.readText() }
                .filter(GradlePluginDeclarationParser::hasConventionPluginImplementation)
                .map(GradlePluginDeclarationParser::pluginIds)
                .flatten()
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
