package com.marmatsan.figmaDesignSync.plugin.generator

import com.marmatsan.figmaDesignSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.modules.ModuleDependency
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal fun Map<String, String>.toVersionsJson(): JsonObject =
    buildJsonObject {
        toSortedMap().forEach { (name, version) ->
            put(name, version)
        }
    }

internal fun LibraryCatalogTree.toDesignJson(): JsonArray =
    roots
        .sortedWith(compareBy(LibraryCatalogNode::group))
        .map(LibraryCatalogNode::toDesignJson)
        .let(::JsonArray)

private fun LibraryCatalogNode.toDesignJson(): JsonObject =
    buildJsonObject {
        put("group", group)
        put("artifactsVisible", artifactsVisible)
        put("entries", entries.toEntriesJson())
        put(
            "children",
            children
                .sortedWith(compareBy(LibraryCatalogNode::group))
                .map(LibraryCatalogNode::toDesignJson)
                .let(::JsonArray)
        )
    }

private fun List<LibraryCatalogEntry>.toEntriesJson(): JsonArray =
    sortedWith(
        compareBy<LibraryCatalogEntry>(
            { entry -> entry.sortKind },
            { entry -> entry.sortKey }
        )
    )
        .map(LibraryCatalogEntry::toDesignJson)
        .let(::JsonArray)

private val LibraryCatalogEntry.sortKind: String
    get() = when (this) {
        is LibraryCatalogEntry.Artifact -> "artifact"
        is LibraryCatalogEntry.ArtifactsBundle -> "bundle"
    }

private val LibraryCatalogEntry.sortKey: String
    get() = when (this) {
        is LibraryCatalogEntry.Artifact -> artifact
        is LibraryCatalogEntry.ArtifactsBundle -> alias
    }

private fun LibraryCatalogEntry.toDesignJson(): JsonObject =
    when (this) {
        is LibraryCatalogEntry.Artifact -> buildJsonObject {
            put("type", "artifact")
            put("artifact", artifact)
            put("version", version.toDesignJson())
            put("requiredByModules", requiredByModules.toSortedJsonArray())
        }

        is LibraryCatalogEntry.ArtifactsBundle -> buildJsonObject {
            put("type", "bundle")
            put("alias", alias)
            put("artifacts", artifacts.sorted().toJsonArray())
            put("version", version.toDesignJson())
            put("requiredByModules", requiredByModules.toSortedJsonArray())
        }
    }

internal fun PluginCatalogTree.toDesignJson(): JsonArray =
    roots
        .sortedWith(compareBy(PluginCatalogNode::id))
        .map(PluginCatalogNode::toDesignJson)
        .let(::JsonArray)

private fun PluginCatalogNode.toDesignJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put("version", version?.toDesignJson() ?: JsonNull)
        put("appliedToModules", appliedToModules.toSortedJsonArray())
        put(
            "children",
            children
                .sortedWith(compareBy(PluginCatalogNode::id))
                .map(PluginCatalogNode::toDesignJson)
                .let(::JsonArray)
        )
    }

private fun CatalogVersion.toDesignJson(): JsonObject =
    buildJsonObject {
        put("value", value?.let(::JsonPrimitive) ?: JsonNull)
        put("visible", visible)
    }

internal fun Collection<String>.toSortedJsonArray(): JsonArray =
    sorted().toJsonArray()

private fun Collection<String>.toJsonArray(): JsonArray =
    map(::JsonPrimitive).let(::JsonArray)

internal fun Collection<ModuleDependency>.toModuleDependenciesJson(): JsonArray =
    sorted()
        .map { dependency ->
            buildJsonObject {
                put("dependentModule", dependency.dependentModule)
                put("dependencyModule", dependency.dependencyModule)
            }
        }
        .let(::JsonArray)
