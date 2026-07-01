package com.marmatsan.figmaDesignSync.data.gradle.catalog

import com.marmatsan.figmaDesignSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import java.io.File
import me.tatarka.inject.annotations.Inject

/**
 * Parses catalogs declared inside `build-logic/settings.gradle.kts`.
 *
 * This reader handles the `versionCatalogs.create("libs")` and
 * `versionCatalogs.create("plugins")` blocks used by the included build, then
 * adapts their declarations into domain catalog trees.
 */
@Inject
class BuildLogicSettingsCatalogReader {
    /**
     * Reads the `libs` catalog and attaches module usage by catalog alias.
     */
    fun readLibraryTree(
        settingsFile: File,
        usageByAlias: Map<String, Set<String>> = emptyMap()
    ): LibraryCatalogTree {
        val content = settingsFile.readText()
        val libsBlock = content.extractCreateBlock("libs")

        return libsBlock
            .readLibraryDeclarations()
            .toLibraryCatalogTree(usageByAlias)
    }

    /**
     * Reads the `plugins` catalog and attaches module usage by plugin alias.
     */
    fun readPluginTree(
        settingsFile: File,
        usageByAlias: Map<String, Set<String>> = emptyMap()
    ): PluginCatalogTree {
        val content = settingsFile.readText()
        val pluginsBlock = content.extractCreateBlock("plugins")

        return pluginsBlock
            .readPluginDeclarations()
            .toPluginCatalogTree(usageByAlias)
    }

    private fun String.readLibraryDeclarations(): List<LibraryDeclaration> =
        libraryDeclarationRegex
            .findAll(this)
            .map { match ->
                LibraryDeclaration(
                    alias = match.groupValues[1],
                    group = match.groupValues[2],
                    artifact = match.groupValues[3],
                    version = match.groupValues[4].takeIf(String::isNotBlank)
                )
            }
            .toList()

    private fun String.readPluginDeclarations(): List<PluginDeclaration> =
        pluginDeclarationRegex
            .findAll(this)
            .map { match ->
                PluginDeclaration(
                    alias = match.groupValues[1],
                    id = match.groupValues[2],
                    version = match.groupValues[3]
                )
            }
            .toList()

    private fun List<LibraryDeclaration>.toLibraryCatalogTree(
        usageByAlias: Map<String, Set<String>>
    ): LibraryCatalogTree {
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
                version = CatalogVersion(declaration.version),
                requiredByModules = usageByAlias[declaration.alias].orEmpty().sorted()
            )
        }

        return LibraryCatalogTree(
            roots = roots.values
                .map(MutableLibraryCatalogNode::toCatalogNode)
                .sortedBy(LibraryCatalogNode::group)
        )
    }

    private fun List<PluginDeclaration>.toPluginCatalogTree(
        usageByAlias: Map<String, Set<String>>
    ): PluginCatalogTree {
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
            leaf.appliedToModules += usageByAlias[declaration.alias].orEmpty()
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
        val alias: String,
        val group: String,
        val artifact: String,
        val version: String?
    )

    private data class PluginDeclaration(
        val alias: String,
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
        val appliedToModules: MutableSet<String> = mutableSetOf()
        val children: MutableMap<String, MutablePluginCatalogNode> = mutableMapOf()

        fun toCatalogNode(): PluginCatalogNode =
            PluginCatalogNode(
                id = id,
                version = version,
                appliedToModules = appliedToModules.sorted(),
                children = children.values
                    .map(MutablePluginCatalogNode::toCatalogNode)
                    .sortedBy(PluginCatalogNode::id)
            )
    }

    private companion object {
        val libraryDeclarationRegex = Regex(
            """library\s*\(\s*alias\s*=\s*"([^"]+)"\s*,\s*group\s*=\s*"([^"]+)"\s*,\s*artifact\s*=\s*"([^"]+)"\s*\)\s*\.\s*(?:version\s*\(\s*version\s*\(\s*"([^"]+)"\s*\)\s*\)|withoutVersion\s*\(\s*\))""",
            RegexOption.DOT_MATCHES_ALL
        )

        val pluginDeclarationRegex = Regex(
            """plugin\s*\(\s*alias\s*=\s*"([^"]+)"\s*,\s*id\s*=\s*"([^"]+)"\s*\)\s*\.\s*version\s*\(\s*version\s*\(\s*"([^"]+)"\s*\)\s*\)""",
            RegexOption.DOT_MATCHES_ALL
        )
    }
}
