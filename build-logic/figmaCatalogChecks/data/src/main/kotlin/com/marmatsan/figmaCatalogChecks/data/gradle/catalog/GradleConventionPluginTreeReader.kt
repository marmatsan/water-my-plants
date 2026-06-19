package com.marmatsan.figmaCatalogChecks.data.gradle.catalog

import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree
import java.io.File
import me.tatarka.inject.annotations.Inject

@Inject
class GradleConventionPluginTreeReader {
    fun readPluginTree(rootDir: File): PluginCatalogTree {
        val pluginIds = rootDir
            .resolve(BUILD_LOGIC_DIR)
            .walkTopDown()
            .filter { file -> file.isFile && file.name == BUILD_FILE_NAME }
            .filter { file -> file.parentFile.name in ConventionPluginModuleNames }
            .map { file -> file.readText().pluginIds() }
            .flatten()
            .toSet()

        return PluginCatalogTree(
            roots = pluginIds.toPluginCatalogNodes()
        )
    }

    private fun String.pluginIds(): Sequence<String> =
        PluginNameRegex
            .findAll(this)
            .map { match -> match.groupValues[1] }

    private fun Set<String>.toPluginCatalogNodes(): List<PluginCatalogNode> =
        map { pluginId -> pluginId.split(".") }
            .fold(emptyList<PluginCatalogNode>()) { nodes, segments -> nodes.withPath(segments) }
            .sortedBy(PluginCatalogNode::id)

    private fun List<PluginCatalogNode>.withPath(
        segments: List<String>
    ): List<PluginCatalogNode> {
        if (segments.isEmpty()) {
            return this
        }

        val head = segments.first()
        val tail = segments.drop(1)
        val existingNode = firstOrNull { node -> node.id == head }
        val updatedNode = existingNode
            ?.copy(children = existingNode.children.withPath(tail))
            ?: PluginCatalogNode(
                id = head,
                children = emptyList<PluginCatalogNode>().withPath(tail)
            )

        return filterNot { node -> node.id == head }
            .plus(updatedNode)
            .sortedBy(PluginCatalogNode::id)
    }

    private companion object {
        const val BUILD_LOGIC_DIR = "build-logic"
        const val BUILD_FILE_NAME = "build.gradle.kts"

        val ConventionPluginModuleNames = setOf(
            "android",
            "bddTest",
            "compose",
            "protobuf",
            "unitTest"
        )

        val PluginNameRegex = Regex("val\\s+pluginName\\s*=\\s*\"([^\"]+)\"")
    }
}
