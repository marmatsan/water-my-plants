package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import java.io.File
import me.tatarka.inject.annotations.Inject

/**
 * Discovers Gradle convention plugin ids implemented by an included build.
 *
 * Convention plugins are repository-owned build APIs. Including them in
 * `design-model.json` lets Figma document which project modules use those
 * conventions.
 */
@Inject
class GradleConventionPluginTreeReader {
    fun readPluginTree(
        rootDir: File,
        usageByPluginId: Map<String, Set<String>> = emptyMap()
    ): PluginCatalogTree {
        val pluginIds = rootDir
            .walkTopDown()
            .filter { file -> file.isFile && file.name == BUILD_FILE_NAME }
            .map { file -> file.readText() }
            .filter { content -> content.hasGradleConventionPluginImplementation() }
            .map { content -> content.pluginIds() }
            .flatten()
            .toSet()

        return PluginCatalogTree(
            roots = pluginIds.toPluginCatalogNodes(usageByPluginId)
        )
    }

    private fun String.pluginIds(): Sequence<String> =
        sequenceOf(PluginNameRegex, PluginIdRegex)
            .flatMap { regex -> regex.findAll(this) }
            .map { match -> match.groupValues[1] }

    private fun String.hasGradleConventionPluginImplementation(): Boolean =
        GradleConventionPluginImplementationRegex.containsMatchIn(this)

    private fun Set<String>.toPluginCatalogNodes(
        usageByPluginId: Map<String, Set<String>>
    ): List<PluginCatalogNode> =
        map { pluginId -> pluginId.split(".") }
            .fold(emptyList<PluginCatalogNode>()) { nodes, segments -> nodes.withPath(segments, usageByPluginId) }
            .sortedBy(PluginCatalogNode::id)

    private fun List<PluginCatalogNode>.withPath(
        segments: List<String>,
        usageByPluginId: Map<String, Set<String>>,
        parentId: String = ""
    ): List<PluginCatalogNode> {
        if (segments.isEmpty()) {
            return this
        }

        val head = segments.first()
        val tail = segments.drop(1)
        val pluginId = listOf(parentId, head)
            .filter(String::isNotBlank)
            .joinToString(".")
        val existingNode = firstOrNull { node -> node.id == head }
        val updatedNode = existingNode
            ?.copy(
                appliedToModules = usageByPluginId[pluginId].orEmpty().sorted(),
                children = existingNode.children.withPath(tail, usageByPluginId, pluginId)
            )
            ?: PluginCatalogNode(
                id = head,
                appliedToModules = usageByPluginId[pluginId].orEmpty().sorted(),
                children = emptyList<PluginCatalogNode>().withPath(tail, usageByPluginId, pluginId)
            )

        return filterNot { node -> node.id == head }
            .plus(updatedNode)
            .sortedBy(PluginCatalogNode::id)
    }

    private companion object {
        const val BUILD_FILE_NAME = "build.gradle.kts"

        val PluginNameRegex = Regex("val\\s+pluginName\\s*=\\s*\"([^\"]+)\"")
        val PluginIdRegex = Regex("id\\s*=\\s*\"([^\"]+)\"")
        val GradleConventionPluginImplementationRegex = Regex(
            "implementationClass\\s*=\\s*\"[^\"]*GradleConventionPlugin\""
        )
    }
}
