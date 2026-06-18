package com.marmatsan.figmaCatalogChecks.data.gradle.catalog

import com.marmatsan.figmaCatalogChecks.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree
import java.io.File
import me.tatarka.inject.annotations.Inject

@Inject
class BuildLogicSettingsCatalogReader {
    fun readLibraryTree(settingsFile: File): LibraryCatalogTree {
        val content = settingsFile.readText()
        val libsBlock = content.extractCreateBlock("libs")

        return libsBlock
            .readLibraryDeclarations()
            .toLibraryCatalogTree()
    }

    fun readPluginTree(settingsFile: File): PluginCatalogTree {
        val content = settingsFile.readText()
        val pluginsBlock = content.extractCreateBlock("plugins")

        return pluginsBlock
            .readPluginDeclarations()
            .toPluginCatalogTree()
    }

    private fun String.readLibraryDeclarations(): List<LibraryDeclaration> =
        libraryDeclarationRegex
            .findAll(this)
            .map { match ->
                LibraryDeclaration(
                    group = match.groupValues[1],
                    artifact = match.groupValues[2],
                    version = match.groupValues[3].takeIf(String::isNotBlank)
                )
            }
            .toList()

    private fun String.readPluginDeclarations(): List<PluginDeclaration> =
        pluginDeclarationRegex
            .findAll(this)
            .map { match ->
                PluginDeclaration(
                    id = match.groupValues[1],
                    version = match.groupValues[2]
                )
            }
            .toList()

    private fun List<LibraryDeclaration>.toLibraryCatalogTree(): LibraryCatalogTree {
        val roots = mutableMapOf<String, MutableLibraryCatalogNode>()

        forEach { declaration ->
            val segments = declaration.group.split(".")
            val root = roots.getOrPut(segments.first()) {
                MutableLibraryCatalogNode(group = segments.first())
            }
            val leaf = segments
                .drop(1)
                .fold(root) { node, segment ->
                    node.children.getOrPut(segment) {
                        MutableLibraryCatalogNode(group = segment)
                    }
                }

            leaf.entries += LibraryCatalogEntry.Artifact(
                artifact = declaration.artifact,
                version = CatalogVersion(declaration.version)
            )
        }

        return LibraryCatalogTree(
            roots = roots.values
                .map(MutableLibraryCatalogNode::toCatalogNode)
                .sortedBy(LibraryCatalogNode::group)
        )
    }

    private fun List<PluginDeclaration>.toPluginCatalogTree(): PluginCatalogTree {
        val roots = mutableMapOf<String, MutablePluginCatalogNode>()

        forEach { declaration ->
            val segments = declaration.id.split(".")
            val root = roots.getOrPut(segments.first()) {
                MutablePluginCatalogNode(id = segments.first())
            }
            val leaf = segments
                .drop(1)
                .fold(root) { node, segment ->
                    node.children.getOrPut(segment) {
                        MutablePluginCatalogNode(id = segment)
                    }
                }

            leaf.version = CatalogVersion(declaration.version)
        }

        return PluginCatalogTree(
            roots = roots.values
                .map(MutablePluginCatalogNode::toCatalogNode)
                .sortedBy(PluginCatalogNode::id)
        )
    }

    private fun String.extractCreateBlock(catalogName: String): String {
        val createCall = """create("$catalogName")"""
        val createCallIndex = indexOf(createCall)

        require(createCallIndex >= 0) {
            "Catalog '$catalogName' not found in build-logic/settings.gradle.kts"
        }

        val blockStart = indexOf('{', startIndex = createCallIndex)

        require(blockStart >= 0) {
            "Catalog '$catalogName' has no body in build-logic/settings.gradle.kts"
        }

        var depth = 0

        for (index in blockStart until length) {
            when (this[index]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return substring(blockStart + 1, index)
                    }
                }
            }
        }

        error("Catalog '$catalogName' body is not closed in build-logic/settings.gradle.kts")
    }

    private data class LibraryDeclaration(
        val group: String,
        val artifact: String,
        val version: String?
    )

    private data class PluginDeclaration(
        val id: String,
        val version: String
    )

    private class MutableLibraryCatalogNode(
        val group: String
    ) {
        val entries: MutableList<LibraryCatalogEntry> = mutableListOf()
        val children: MutableMap<String, MutableLibraryCatalogNode> = mutableMapOf()

        fun toCatalogNode(): LibraryCatalogNode =
            LibraryCatalogNode(
                group = group,
                entries = entries,
                children = children.values
                    .map(MutableLibraryCatalogNode::toCatalogNode)
                    .sortedBy(LibraryCatalogNode::group)
            )
    }

    private class MutablePluginCatalogNode(
        val id: String
    ) {
        var version: CatalogVersion? = null
        val children: MutableMap<String, MutablePluginCatalogNode> = mutableMapOf()

        fun toCatalogNode(): PluginCatalogNode =
            PluginCatalogNode(
                id = id,
                version = version,
                children = children.values
                    .map(MutablePluginCatalogNode::toCatalogNode)
                    .sortedBy(PluginCatalogNode::id)
            )
    }

    private companion object {
        val libraryDeclarationRegex = Regex(
            """library\s*\(\s*alias\s*=\s*"[^"]+"\s*,\s*group\s*=\s*"([^"]+)"\s*,\s*artifact\s*=\s*"([^"]+)"\s*\)\s*\.\s*(?:version\s*\(\s*version\s*\(\s*"([^"]+)"\s*\)\s*\)|withoutVersion\s*\(\s*\))""",
            RegexOption.DOT_MATCHES_ALL
        )

        val pluginDeclarationRegex = Regex(
            """plugin\s*\(\s*alias\s*=\s*"[^"]+"\s*,\s*id\s*=\s*"([^"]+)"\s*\)\s*\.\s*version\s*\(\s*version\s*\(\s*"([^"]+)"\s*\)\s*\)""",
            RegexOption.DOT_MATCHES_ALL
        )
    }
}
