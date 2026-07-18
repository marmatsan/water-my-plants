package com.marmatsan.figmaDesignSync.plugin.generator

import com.marmatsan.figmaDesignSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginConfigurationUsage
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginUsage
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.ci.CiExternalTopology
import com.marmatsan.figmaDesignSync.domain.model.ci.CiWindowsRuntime
import com.marmatsan.figmaDesignSync.domain.model.ci.CiNode
import com.marmatsan.figmaDesignSync.domain.model.ci.CiConfiguration
import com.marmatsan.figmaDesignSync.domain.model.ci.CiJob
import com.marmatsan.figmaDesignSync.domain.model.ci.CiPipeline
import com.marmatsan.figmaDesignSync.domain.model.ci.CiVcsRoot
import com.marmatsan.figmaDesignSync.domain.model.modules.ModuleDependency
import com.marmatsan.figmaDesignSync.domain.model.versions.RepositoryVersionSection
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Converts repository versions to the stable JSON object used by
 * `content.versions`.
 */
internal fun Map<String, String>.toVersionsJson(): JsonObject =
    buildJsonObject {
        toSortedMap().forEach { (name, version) ->
            put(name, version)
        }
    }

/**
 * Converts ordered version sections to the JSON array used by
 * `content.versionSections`.
 */
internal fun List<RepositoryVersionSection>.toVersionSectionsJson(): JsonArray =
    map { section ->
        buildJsonObject {
            put("name", section.name)
            put("versions", section.versions.toVersionsJson())
        }
    }
        .let(::JsonArray)

/**
 * Converts a library catalog tree to the array consumed by Figma dependency
 * tree rendering.
 */
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
            put("providedByConventionPlugins", providedByConventionPlugins.toDesignJson())
            put("configuredByConventionPlugins", configuredByConventionPlugins.toConfigurationUsageDesignJson())
        }

        is LibraryCatalogEntry.ArtifactsBundle -> buildJsonObject {
            put("type", "bundle")
            put("alias", alias)
            put("artifacts", artifacts.sorted().toJsonArray())
            put("version", version.toDesignJson())
            put("requiredByModules", requiredByModules.toSortedJsonArray())
            put("providedByConventionPlugins", providedByConventionPlugins.toDesignJson())
        }
    }

private fun List<ConventionPluginUsage>.toDesignJson(): JsonArray =
    sortedWith(
        compareBy<ConventionPluginUsage>(
            { usage -> usage.pluginId },
            { usage -> usage.pluginModule }
        )
    )
        .map { usage ->
            buildJsonObject {
                put("pluginId", usage.pluginId)
                put("pluginModule", usage.pluginModule)
                put("requiredByModules", usage.requiredByModules.toSortedJsonArray())
            }
        }
        .let(::JsonArray)

private fun List<ConventionPluginConfigurationUsage>.toConfigurationUsageDesignJson(): JsonArray =
    sortedWith(
        compareBy<ConventionPluginConfigurationUsage>(
            { usage -> usage.pluginId },
            { usage -> usage.pluginModule },
            { usage -> usage.target }
        )
    )
        .map { usage ->
            buildJsonObject {
                put("pluginId", usage.pluginId)
                put("pluginModule", usage.pluginModule)
                put("target", usage.target)
            }
        }
        .let(::JsonArray)

/**
 * Converts a plugin catalog tree to the array consumed by Figma plugin tree
 * rendering.
 */
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
        put("providedByConventionPlugins", providedByConventionPlugins.toPluginConventionUsageDesignJson())
        put(
            "children",
            children
                .sortedWith(compareBy(PluginCatalogNode::id))
                .map(PluginCatalogNode::toDesignJson)
                .let(::JsonArray)
        )
    }

private fun List<PluginCatalogNode.ConventionPluginUsage>.toPluginConventionUsageDesignJson(): JsonArray =
    sortedWith(
        compareBy<PluginCatalogNode.ConventionPluginUsage>(
            { usage -> usage.pluginId },
            { usage -> usage.pluginModule }
        )
    )
        .map { usage ->
            buildJsonObject {
                put("pluginId", usage.pluginId)
                put("pluginModule", usage.pluginModule)
                put("requiredByModules", usage.requiredByModules.toSortedJsonArray())
            }
        }
        .let(::JsonArray)

private fun CatalogVersion.toDesignJson(): JsonObject =
    buildJsonObject {
        put("value", value?.let(::JsonPrimitive) ?: JsonNull)
        put("visible", visible)
    }

/**
 * Converts module names to a sorted JSON array for deterministic output.
 */
internal fun Collection<String>.toSortedJsonArray(): JsonArray =
    sorted().toJsonArray()

private fun Collection<String>.toJsonArray(): JsonArray =
    map(::JsonPrimitive).let(::JsonArray)

/**
 * Converts module dependency edges to stable JSON objects.
 */
internal fun Collection<ModuleDependency>.toModuleDependenciesJson(): JsonArray =
    sorted()
        .map { dependency ->
            buildJsonObject {
                put("dependentModule", dependency.dependentModule)
                put("dependencyModule", dependency.dependencyModule)
            }
        }
        .let(::JsonArray)

/**
 * Converts the versioned external CI topology to its stable model shape.
 */
internal fun CiExternalTopology.toDesignJson(): JsonObject =
    buildJsonObject {
        put("schemaVersion", schemaVersion)
        put(
            "validation",
            buildJsonObject {
                put("lastValidatedOn", validation.lastValidatedOn.toString())
                put("warnAfterDays", validation.warnAfterDays)
            }
        )
        put(
            "nodes",
            nodes
                .sortedBy(CiNode::id)
                .map { node ->
                    buildJsonObject {
                        put("id", node.id)
                        put("type", node.type.serializedName)
                        put("name", node.name)
                        put("description", node.description)
                    }
                }
                .let(::JsonArray)
        )
        put(
            "connections",
            connections
                .sortedBy { connection -> connection.id }
                .map { connection ->
                    buildJsonObject {
                        put("id", connection.id)
                        put("source", connection.sourceNodeId)
                        put("target", connection.targetNodeId)
                        put("label", connection.label)
                        put("description", connection.description)
                        put("protocol", connection.protocol.toJsonPrimitiveOrNull())
                        put("authentication", connection.authentication.toSortedJsonArray())
                        put("policy", connection.policy.toJsonPrimitiveOrNull())
                        put("path", connection.path.toJsonPrimitiveOrNull())
                        put("automation", connection.automation.serializedName)
                        put("annotation", connection.annotation.toJsonPrimitiveOrNull())
                    }
                }
                .let(::JsonArray)
        )
    }

/**
 * Converts the versioned Windows CI runtime to its stable model shape.
 */
internal fun CiWindowsRuntime.toDesignJson(): JsonObject =
    buildJsonObject {
        put("schemaVersion", schemaVersion)
        put(
            "validation",
            buildJsonObject {
                put("lastValidatedOn", validation.lastValidatedOn.toString())
                put("warnAfterDays", validation.warnAfterDays)
            }
        )
        put("platform", platform)
        put(
            "services",
            services
                .sortedBy(CiWindowsRuntime.Service::id)
                .map { service ->
                    buildJsonObject {
                        put("id", service.id)
                        put("name", service.name)
                        put("description", service.description)
                        put("service", service.service)
                        put("startup", service.startup)
                        put("identity", service.identity)
                    }
                }
                .let(::JsonArray)
        )
    }

/**
 * Converts effective CI pipelines and VCS roots to stable JSON.
 */
internal fun CiConfiguration.toDesignJson(): JsonObject =
    buildJsonObject {
        put(
            "pipelines",
            pipelines
                .sortedBy(CiPipeline::id)
                .map(CiPipeline::toDesignJson)
                .let(::JsonArray)
        )
        put(
            "vcsRoots",
            vcsRoots
                .sortedBy(CiVcsRoot::id)
                .map { vcsRoot ->
                    buildJsonObject {
                        put("id", vcsRoot.id)
                        put("name", vcsRoot.name)
                        put("url", vcsRoot.url)
                        put("defaultBranchRef", vcsRoot.defaultBranchRef)
                        put("branchSpec", vcsRoot.branchSpec.toJsonArray())
                    }
                }
                .let(::JsonArray)
        )
    }

private fun CiPipeline.toDesignJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put("name", name)
        put(
            "triggers",
            triggers
                .map { trigger ->
                    buildJsonObject {
                        put("type", trigger.type.serializedName)
                        put("branchFilter", trigger.branchFilter.toJsonPrimitiveOrNull())
                        put("dependencyPipelineId", trigger.dependencyPipelineId.toJsonPrimitiveOrNull())
                        put(
                            "afterSuccessfulBuildOnly",
                            trigger.afterSuccessfulBuildOnly?.let(::JsonPrimitive) ?: JsonNull
                        )
                    }
                }
                .let(::JsonArray)
        )
        put(
            "jobs",
            jobs
                .sortedBy(CiJob::id)
                .map(CiJob::toDesignJson)
                .let(::JsonArray)
        )
    }

private fun CiJob.toDesignJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put("name", name)
        put(
            "steps",
            steps.map { step ->
                buildJsonObject {
                    put("id", step.id)
                    put("name", step.name)
                    put("command", step.command)
                }
            }.let(::JsonArray)
        )
        put("repositoryIds", repositoryIds.toSortedJsonArray())
        put(
            "artifacts",
            artifacts
                .sortedBy(CiJob.Artifact::path)
                .map { artifact ->
                    buildJsonObject {
                        put("path", artifact.path)
                        put("publish", artifact.publish)
                        put("shareWithJobs", artifact.shareWithJobs)
                    }
                }
                .let(::JsonArray)
        )
        put(
            "dependencies",
            dependencies
                .sortedBy(CiJob.Dependency::jobId)
                .map { dependency ->
                    buildJsonObject {
                        put("jobId", dependency.jobId)
                        put("artifactPaths", dependency.artifactPaths.toSortedJsonArray())
                    }
                }
                .let(::JsonArray)
        )
        put(
            "publishedChecks",
            publishedChecks
                .sortedBy(CiJob.PublishedCheck::name)
                .map { check -> buildJsonObject { put("name", check.name) } }
                .let(::JsonArray)
        )
    }

private fun String?.toJsonPrimitiveOrNull() =
    this?.let(::JsonPrimitive) ?: JsonNull
