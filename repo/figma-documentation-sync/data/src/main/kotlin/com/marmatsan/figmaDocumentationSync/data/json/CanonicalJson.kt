package com.marmatsan.figmaDocumentationSync.data.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Canonical JSON representation with recursively sorted object keys. */
object CanonicalJson {
    /** Serializes [value] with deterministic object-key ordering. */
    fun stringify(
        value: JsonElement,
    ): String =
        when (value) {
            is JsonArray -> {
                value.joinToString(
                    prefix = "[",
                    postfix = "]",
                    separator = ",",
                    transform = ::stringify,
                )
            }

            is JsonObject -> {
                value.keys.sorted().joinToString(
                    prefix = "{",
                    postfix = "}",
                    separator = ",",
                ) { key ->
                    "${kotlinx.serialization.json.Json.encodeToString(key)}:${stringify(
                        value = value.getValue(key),
                    )}"
                }
            }

            else -> {
                value.toString()
            }
        }
}
