package com.marmatsan.figmaDocumentationSync.data.json.writer.config

import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaCatalogTreeTargetConfig
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaHeaderSectionTarget
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaVersionSectionTarget
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Projects header section targets to the portable writer schema. */
internal fun List<FigmaHeaderSectionTarget>.toHeaderTargetsJson(): JsonArray =
    map { target ->
        buildJsonObject {
            put(
                "sectionNodeId",
                target.sectionNodeId
            )
            put(
                "links",
                JsonArray(
                    target.links.map { link ->
                        buildJsonObject {
                            put(
                                "label",
                                link.label
                            )
                            put(
                                "url",
                                link.url
                            )
                        }
                    }
                )
            )
            target.definition?.let { definition ->
                put(
                    "definition",
                    definition
                )
            }
        }
    }.let(::JsonArray)

/** Projects version section targets to the portable writer schema. */
internal fun Map<String, FigmaVersionSectionTarget>.toVersionTargetsJson(): JsonObject =
    mapValues { (_, target) ->
        buildJsonObject {
            put(
                "parentNodeId",
                target.parentNodeId
            )
            put(
                "variableFolder",
                target.variableFolder
            )
        }
    }.let(::JsonObject)

/** Projects catalog tree targets to the portable writer schema. */
internal fun List<FigmaCatalogTreeTargetConfig>.toCatalogTargetsJson(): JsonArray =
    map { target ->
        buildJsonObject {
            put(
                "name",
                target.name
            )
            put(
                "sectionNodeId",
                target.sectionNodeId
            )
            put(
                "type",
                target.type.wireValue
            )
            put(
                "lifecycle",
                target.lifecycle.wireValue
            )
            put(
                "nodesPath",
                target.nodesPath.toJsonArray()
            )
            if (target.gradlePluginNodes) {
                put(
                    "gradlePluginNodes",
                    true
                )
            }
            if (target.warnWhenUnused) {
                put(
                    "warnWhenUnused",
                    true
                )
            }
            if (target.versionValuesPath.isNotEmpty()) {
                put(
                    "versionValuesPath",
                    target.versionValuesPath.toJsonArray()
                )
            }
            if (target.sharedVersionKeys.isNotEmpty()) {
                put(
                    "sharedVersionKeys",
                    target.sharedVersionKeys.toJsonArray()
                )
            }
        }
    }.let(::JsonArray)

/** Projects string map values to JSON primitives while preserving key order. */
internal fun Map<String, String>.toJsonObject(): JsonObject =
    mapValues { (_, value) -> JsonPrimitive(value) }.let(::JsonObject)

/** Projects ordered string values to JSON primitives. */
internal fun List<String>.toJsonArray(): JsonArray =
    map(
        transform = ::JsonPrimitive
    ).let(::JsonArray)
