package com.marmatsan.figmaDocumentationSync.data.fingerprint

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.CanonicalJson
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Computes model hashes for visual targets and atomic catalog execution scopes. */
class FigmaTargetFingerprintCalculator {
    fun create(
        designModel: JsonObject,
        visualTargets: List<String>,
        catalogTargets: List<String>,
    ): Map<String, String> =
        buildMap {
            visualTargets.forEach { target ->
                put(
                    target,
                    hash(
                        value =
                            modelSlice(
                                designModel = designModel,
                                target = target,
                                catalogTargets = catalogTargets,
                            ),
                    ),
                )
                if (target !in catalogTargets) return@forEach

                val nodes =
                    catalogNodes(
                        designModel = designModel,
                        target = target,
                    )
                val rootKey = if (target.substringAfter('.') == "libraries") "group" else "id"
                val roots =
                    nodes
                        .mapNotNull { node ->
                            node.jsonObject[rootKey]?.jsonPrimitive?.contentOrNull
                        }.distinct()
                roots.forEach { root ->
                    put(
                        "$target.$root",
                        hash(
                            value =
                                JsonArray(
                                    nodes.filter { node ->
                                        node.jsonObject[rootKey]?.jsonPrimitive?.content ==
                                            root
                                    },
                                ),
                        ),
                    )
                }
                put(
                    "$target.cleanup",
                    hash(
                        value =
                            buildJsonObject {
                                put(
                                    "roots",
                                    JsonArray(
                                        roots.map(
                                            transform = ::JsonPrimitive,
                                        ),
                                    ),
                                )
                            },
                    ),
                )
            }
        }

    private fun modelSlice(
        designModel: JsonObject,
        target: String,
        catalogTargets: List<String>,
    ): JsonElement {
        val content = designModel["content"]?.jsonObject
        return when {
            target == "preflight" -> {
                content ?: JsonNull
            }

            target == "headers" -> {
                buildJsonObject {
                    put(
                        "target",
                        target,
                    )
                }
            }

            target == "versions" -> {
                buildJsonObject {
                    content
                        ?.get(
                            key = "versions",
                        )?.let {
                            put(
                                "versions",
                                it,
                            )
                        }
                    content
                        ?.get(
                            key = "versionSections",
                        )?.let {
                            put(
                                "versionSections",
                                it,
                            )
                        }
                }
            }

            target.startsWith(
                prefix = "ci.",
            ) -> {
                content?.get(
                    key = "ci",
                ) ?: JsonNull
            }

            target in catalogTargets -> {
                catalogNodes(
                    designModel = designModel,
                    target = target,
                )
            }

            else -> {
                JsonNull
            }
        }
    }

    private fun catalogNodes(
        designModel: JsonObject,
        target: String,
    ): JsonArray {
        val catalogName = target.substringBefore('.')
        val treeName = target.substringAfter('.')
        return designModel["content"]
            ?.jsonObject
            ?.get(
                key = "catalogs",
            )?.jsonObject
            ?.get(
                key = catalogName,
            )?.jsonObject
            ?.get(
                key = treeName,
            )?.jsonArray
            ?: JsonArray(emptyList())
    }

    private fun hash(
        value: JsonElement,
    ): String =
        Sha256Hash.of(
            value =
                CanonicalJson.stringify(
                    value = value,
                ),
        )
}
