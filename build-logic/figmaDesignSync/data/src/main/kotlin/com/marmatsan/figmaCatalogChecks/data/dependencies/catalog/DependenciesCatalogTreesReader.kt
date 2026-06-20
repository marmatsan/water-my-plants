package com.marmatsan.figmaDesignSync.data.dependencies.catalog

import com.marmatsan.dependencies.libraryTrees
import com.marmatsan.dependencies.pluginTrees
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.model.LibraryEntry
import com.marmatsan.dependencies.tree.node.Node
import com.marmatsan.dependencies.Versions
import com.marmatsan.figmaDesignSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import java.io.File
import me.tatarka.inject.annotations.Inject

@Inject
class DependenciesCatalogTreesReader {
    fun readLibraryTree(rootDir: File): LibraryCatalogTree =
        readLibraryTree(versions = Versions.load(rootDir))

    fun readPluginTree(rootDir: File): PluginCatalogTree =
        readPluginTree(versions = Versions.load(rootDir))

    fun readLibraryTreeWithVersionAliases(): LibraryCatalogTree =
        readLibraryTree(versions = CatalogVersionAliases)

    fun readPluginTreeWithVersionAliases(): PluginCatalogTree =
        readPluginTree(versions = CatalogVersionAliases)

    fun readLibraryTree(versions: Versions): LibraryCatalogTree =
        readLibraryTree(libraryTrees(versions))

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

private val CatalogVersionAliases = Versions(
    activityComposeVersion = "activityComposeVersion",
    androidCoroutinesVersion = "androidCoroutinesVersion",
    androidGradlePlugin = "androidGradlePlugin",
    composeBomVersion = "composeBomVersion",
    coreKtxVersion = "coreKtxVersion",
    coreSplashscreenVersion = "coreSplashscreenVersion",
    cucumberVersion = "cucumberVersion",
    datastoreVersion = "datastoreVersion",
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
