package com.marmatsan.figmaDocumentationSync.data.writer.generation

import com.marmatsan.figmaDocumentationSync.data.fingerprint.WriterScopeFingerprintCalculator
import com.marmatsan.figmaDocumentationSync.data.json.visual.CiVisualPlanJson
import com.marmatsan.figmaDocumentationSync.data.writer.McpRunnerSourceRenderer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Plans deterministic target runner sources and their execution-scope identities. */
internal class CanonicalMcpRunnerTargetPlanner(
    private val renderer: McpRunnerSourceRenderer,
) {
    /** Adds one source per required target scope and returns file-to-scope mappings. */
    fun addTargetSources(
        sources: MutableMap<String, String>,
        context: CanonicalMcpRunnerGenerationContext,
        targets: List<String>,
        writeMetadata: Boolean,
        fullVisualSync: Boolean,
    ): Map<String, String> {
        val executionScopes = linkedMapOf<String, String>()
        val namespace = context.request.config.canonicalStagingNamespace
        if (!fullVisualSync) {
            val target = targets.single()
            val fileName = "99-run-target.mcp.js"
            sources[fileName] =
                targetSource(
                    context = context,
                    namespace = namespace,
                    target = target,
                    writeMetadata = writeMetadata,
                    executionScope = target,
                )
            executionScopes[fileName] = target
            return executionScopes
        }

        targets.forEachIndexed { index, target ->
            val roots =
                catalogRoots(
                    designModel = context.designModel,
                    target = target,
                    catalogTargets = context.request.config.catalogTargetNames,
                )
            if (roots.isEmpty()) {
                addUnscopedTarget(
                    sources = sources,
                    executionScopes = executionScopes,
                    context = context,
                    namespace = namespace,
                    target = target,
                    targetIndex = index,
                )
            } else {
                addCatalogTargets(
                    sources = sources,
                    executionScopes = executionScopes,
                    context = context,
                    namespace = namespace,
                    target = target,
                    targetIndex = index,
                    roots = roots,
                )
            }
        }
        return executionScopes
    }

    private fun addUnscopedTarget(
        sources: MutableMap<String, String>,
        executionScopes: MutableMap<String, String>,
        context: CanonicalMcpRunnerGenerationContext,
        namespace: String,
        target: String,
        targetIndex: Int,
    ) {
        val fileName = "99-${targetIndex.twoDigits()}-${target.safeName()}.mcp.js"
        sources[fileName] =
            targetSource(
                context = context,
                namespace = namespace,
                target = target,
                writeMetadata = false,
                executionScope = target,
            )
        executionScopes[fileName] = target
    }

    private fun addCatalogTargets(
        sources: MutableMap<String, String>,
        executionScopes: MutableMap<String, String>,
        context: CanonicalMcpRunnerGenerationContext,
        namespace: String,
        target: String,
        targetIndex: Int,
        roots: List<String>,
    ) {
        roots.forEachIndexed { rootIndex, root ->
            val scope = "$target.$root"
            val fileName =
                "99-${targetIndex.twoDigits()}-${rootIndex.twoDigits()}-" +
                    "${target.safeName()}-${root.safeName()}.mcp.js"
            sources[fileName] =
                targetSource(
                    context = context,
                    namespace = namespace,
                    target = target,
                    writeMetadata = false,
                    executionScope = scope,
                    roots = listOf(root),
                )
            executionScopes[fileName] = scope
        }
        val cleanupScope = "$target.cleanup"
        val cleanupFile = "99-${targetIndex.twoDigits()}-99-${target.safeName()}-cleanup.mcp.js"
        sources[cleanupFile] =
            targetSource(
                context = context,
                namespace = namespace,
                target = target,
                writeMetadata = false,
                executionScope = cleanupScope,
                cleanupOnly = true,
            )
        executionScopes[cleanupFile] = cleanupScope
    }

    private fun targetSource(
        context: CanonicalMcpRunnerGenerationContext,
        namespace: String,
        target: String,
        writeMetadata: Boolean,
        executionScope: String,
        roots: List<String> = emptyList(),
        cleanupOnly: Boolean = false,
    ): String {
        val executionMetadata =
            buildJsonObject {
                put(
                    "writerHash",
                    context.writerHash,
                )
                put(
                    "transportHash",
                    context.transportHash,
                )
                put(
                    "targetFingerprints",
                    context.targetFingerprints.toJsonObject(),
                )
                put(
                    "writerScopeFingerprints",
                    context.writerScopeFingerprints.toJsonObject(),
                )
                put(
                    "writerScopeFingerprintSchemaVersion",
                    WriterScopeFingerprintCalculator.SCHEMA_VERSION,
                )
            }
        val syncOptions =
            buildJsonObject {
                put(
                    "targets",
                    JsonArray(listOf(JsonPrimitive(target))),
                )
                put(
                    "writeMetadata",
                    writeMetadata,
                )
                if (roots.isNotEmpty()) {
                    put(
                        "catalogRootFilters",
                        buildJsonObject {
                            put(
                                target,
                                JsonArray(
                                    roots.map(
                                        transform = ::JsonPrimitive,
                                    ),
                                ),
                            )
                        },
                    )
                }
                if (cleanupOnly) {
                    put(
                        "catalogCleanupOnlyTargets",
                        JsonArray(listOf(JsonPrimitive(target))),
                    )
                }
                if (target.startsWith(
                        prefix = "ci.",
                    )
                ) {
                    val ciConfig =
                        context.request.config.ciVisualPlanConfig
                            ?: throw IllegalArgumentException(
                                "CI visual target '$target' requires CI visual plan project configuration.",
                            )
                    put(
                        "ciVisualPlan",
                        CiVisualPlanJson.create(
                            context.designModel,
                            ciConfig,
                            target,
                        ),
                    )
                }
                put(
                    "executionMetadata",
                    executionMetadata,
                )
            }
        return renderer.runTarget(
            metadataPageId = context.request.config.metadataPageId,
            namespace = namespace,
            syncOptions = syncOptions,
            executionScope = executionScope,
            modelTarget = target,
        )
    }

    private fun catalogRoots(
        designModel: JsonObject,
        target: String,
        catalogTargets: List<String>,
    ): List<String> {
        if (target !in catalogTargets) return emptyList()
        val catalogName = target.substringBefore('.')
        val treeName = target.substringAfter('.')
        val nodes =
            designModel["content"]
                ?.jsonObject
                ?.get("catalogs")
                ?.jsonObject
                ?.get(catalogName)
                ?.jsonObject
                ?.get(treeName)
                ?.jsonArray
                ?: JsonArray(emptyList())
        val rootKey = if (treeName == "libraries") "group" else "id"
        return nodes.mapNotNull { node -> node.jsonObject[rootKey]?.jsonPrimitive?.contentOrNull }.distinct()
    }

    private fun Int.twoDigits(): String =
        toString().padStart(
            2,
            '0',
        )

    private fun String.safeName(): String =
        replace(
            Regex("[^A-Za-z0-9_-]+"),
            "-",
        )

    private fun Map<String, String>.toJsonObject(): JsonObject =
        JsonObject(mapValues { (_, value) -> JsonPrimitive(value) })
}
