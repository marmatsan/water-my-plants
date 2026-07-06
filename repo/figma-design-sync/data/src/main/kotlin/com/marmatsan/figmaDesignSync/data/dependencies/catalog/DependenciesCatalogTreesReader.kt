package com.marmatsan.figmaDesignSync.data.dependencies.catalog

import com.marmatsan.dependencies.libraryTrees
import com.marmatsan.dependencies.pluginTrees
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.model.LibraryEntry
import com.marmatsan.dependencies.tree.node.Node
import com.marmatsan.dependencies.Versions
import com.marmatsan.figmaDesignSync.data.gradle.catalog.GradleCatalogUsageReader
import com.marmatsan.figmaDesignSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDesignSync.domain.port.gradle.IncludedBuildSource
import java.io.File
import me.tatarka.inject.annotations.Inject

/**
 * Reads the repository dependency DSL and converts it into domain catalog
 * trees.
 *
 * The dependency DSL already knows the logical library and plugin hierarchy.
 * This reader adapts that hierarchy to [LibraryCatalogTree] and
 * [PluginCatalogTree], then enriches entries with Gradle usage information
 * from [GradleCatalogUsageReader].
 */
@Inject
class DependenciesCatalogTreesReader(
    private val gradleCatalogUsageReader: GradleCatalogUsageReader
) {
    /**
     * Reads concrete library versions from `versions.properties`.
     */
    fun readLibraryTree(rootDir: File): LibraryCatalogTree =
        readLibraryTree(versions = Versions.load(rootDir))

    /**
     * Reads concrete plugin versions from `versions.properties`.
     */
    fun readPluginTree(rootDir: File): PluginCatalogTree =
        readPluginTree(versions = Versions.load(rootDir))

    /**
     * Reads a library tree using version aliases instead of resolved versions.
     *
     * This is the variant used by Figma documentation because it shows the
     * repository-owned version key that should be edited.
     */
    fun readLibraryTreeWithVersionAliases(
        rootDir: File,
        conventionPluginIncludedBuilds: List<IncludedBuildSource> = emptyList()
    ): LibraryCatalogTree =
        readLibraryTree(versions = CatalogVersionAliases)
            .withLibraryUsages(
                conventionPluginIncludedBuilds.fold(GradleCatalogUsageReader.LibraryUsages()) { usages, includedBuild ->
                    usages + gradleCatalogUsageReader.readConventionLibraryUsages(
                        rootDir = File(includedBuild.rootDirPath),
                        modulePathPrefix = includedBuild.modulePathPrefix
                    )
                }
            )

    /**
     * Reads a plugin tree using version aliases instead of resolved versions.
     */
    fun readPluginTreeWithVersionAliases(
        rootDir: File,
        conventionPluginIncludedBuilds: List<IncludedBuildSource> = emptyList()
    ): PluginCatalogTree =
        readPluginTree(versions = CatalogVersionAliases)
            .withPluginUsages(
                conventionPluginIncludedBuilds
                    .fold(emptyMap<String, Set<String>>()) { usages, includedBuild ->
                        usages.merge(
                            gradleCatalogUsageReader.readConventionPluginUsages(
                                rootDir = File(includedBuild.rootDirPath),
                                modulePathPrefix = includedBuild.modulePathPrefix
                            )
                        )
                    }
                    .merge(gradleCatalogUsageReader.readMainPluginUsages(rootDir))
            )

    /**
     * Converts dependency DSL library nodes to the domain tree.
     */
    fun readLibraryTree(versions: Versions): LibraryCatalogTree =
        readLibraryTree(libraryTrees(versions))

    /**
     * Converts dependency DSL plugin nodes to the domain tree.
     */
    fun readPluginTree(versions: Versions): PluginCatalogTree =
        readPluginTree(pluginTrees(versions))

    internal fun readLibraryTree(
        roots: List<Node<DependencyNode.Library>>
    ): LibraryCatalogTree = LibraryCatalogTree(
        roots = roots.map { root -> root.toLibraryCatalogNode() }
    )

    internal fun readPluginTree(
        roots: List<Node<DependencyNode.Plugin>>
    ): PluginCatalogTree = PluginCatalogTree(
        roots = roots.map { root -> root.toPluginCatalogNode() }
    )
}

private fun Node<DependencyNode.Library>.toLibraryCatalogNode(): LibraryCatalogNode {
    val entries = value.entries.orEmpty().map { entry -> entry.toLibraryCatalogEntry() }

    return LibraryCatalogNode(
        group = value.libraryGroup,
        entries = entries,
        children = children.map { child -> child.toLibraryCatalogNode() }
    )
}

private fun LibraryEntry.toLibraryCatalogEntry(): LibraryCatalogEntry =
    when (this) {
        is LibraryEntry.Single -> LibraryCatalogEntry.Artifact(
            artifact = artifact.artifact,
            version = CatalogVersion(artifact.version)
        )

        is LibraryEntry.Bundle -> LibraryCatalogEntry.ArtifactsBundle(
            alias = artifactsBundle.alias,
            artifacts = artifactsBundle.artifacts.map { artifact -> artifact.artifact },
            version = CatalogVersion(artifactsBundle.version)
        )
    }

private fun Node<DependencyNode.Plugin>.toPluginCatalogNode(): PluginCatalogNode =
    PluginCatalogNode(
        id = value.pluginId,
        version = value.version?.let(::CatalogVersion),
        children = children.map { child -> child.toPluginCatalogNode() }
    )

private fun LibraryCatalogTree.withLibraryUsages(
    usages: GradleCatalogUsageReader.LibraryUsages
): LibraryCatalogTree =
    copy(
        roots = roots.map { node -> node.withLibraryUsages(usages) }
    )

private fun LibraryCatalogNode.withLibraryUsages(
    usages: GradleCatalogUsageReader.LibraryUsages,
    parentGroup: String = ""
): LibraryCatalogNode {
    val groupPath = listOf(parentGroup, group)
        .filter(String::isNotBlank)
        .joinToString(".")

    return copy(
        entries = entries.map { entry -> entry.withLibraryUsages(groupPath, usages) },
        children = children.map { child -> child.withLibraryUsages(usages, groupPath) }
    )
}

private fun LibraryCatalogEntry.withLibraryUsages(
    group: String,
    usages: GradleCatalogUsageReader.LibraryUsages
): LibraryCatalogEntry =
    when (this) {
        is LibraryCatalogEntry.Artifact -> copy(
            requiredByModules = usages.coordinates["$group:$artifact"].orEmpty().sorted()
        )

        is LibraryCatalogEntry.ArtifactsBundle -> copy(
            requiredByModules = usages.bundles[alias].orEmpty().sorted()
        )
    }

private fun PluginCatalogTree.withPluginUsages(
    usages: Map<String, Set<String>>
): PluginCatalogTree =
    copy(
        roots = roots.map { node -> node.withPluginUsages(usages) }
    )

private fun PluginCatalogNode.withPluginUsages(
    usages: Map<String, Set<String>>,
    parentId: String = ""
): PluginCatalogNode {
    val pluginId = listOf(parentId, id)
        .filter(String::isNotBlank)
        .joinToString(".")

    return copy(
        appliedToModules = usages[pluginId].orEmpty().sorted(),
        children = children.map { child -> child.withPluginUsages(usages, pluginId) }
    )
}

private fun Map<String, Set<String>>.merge(other: Map<String, Set<String>>): Map<String, Set<String>> =
    (keys + other.keys).associateWith { key ->
        (this[key].orEmpty() + other[key].orEmpty()).toSortedSet()
    }

private operator fun GradleCatalogUsageReader.LibraryUsages.plus(
    other: GradleCatalogUsageReader.LibraryUsages
): GradleCatalogUsageReader.LibraryUsages =
    GradleCatalogUsageReader.LibraryUsages(
        coordinates = coordinates.merge(other.coordinates),
        bundles = bundles.merge(other.bundles)
    )

private val CatalogVersionAliases = Versions(
    activityComposeVersion = "activityComposeVersion",
    androidCoroutinesVersion = "androidCoroutinesVersion",
    androidGradlePlugin = "androidGradlePlugin",
    composeBomVersion = "composeBomVersion",
    coreKtxVersion = "coreKtxVersion",
    coreSplashscreenVersion = "coreSplashscreenVersion",
    cucumberVersion = "cucumberVersion",
    datastoreVersion = "datastoreVersion",
    dokkaVersion = "dokkaVersion",
    figmaCodeConnectLibraryVersion = "figmaCodeConnectLibraryVersion",
    figmaCodeConnectPluginVersion = "figmaCodeConnectPluginVersion",
    junit5PluginVersion = "junit5PluginVersion",
    kotestVersion = "kotestVersion",
    kotlinInjectVersion = "kotlinInjectVersion",
    kotlinVersion = "kotlinVersion",
    ktorVersion = "ktorVersion",
    kspVersion = "kspVersion",
    landscapistVersion = "landscapistVersion",
    lifecycleVersion = "lifecycleVersion",
    mockkVersion = "mockkVersion",
    navigationComposeVersion = "navigationComposeVersion",
    protobufLibraryVersion = "protobufLibraryVersion",
    protobufPluginVersion = "protobufPluginVersion",
    serializationVersion = "serializationVersion"
)
