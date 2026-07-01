package com.marmatsan.figmaDesignSync.plugin.generator

import java.security.MessageDigest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Computes the stable SHA-256 hash used as the Figma sync contract.
 *
 * JSON object keys are sorted before hashing so equivalent model content
 * produces the same hash regardless of insertion order. The generator hashes
 * content plus Git identity, but intentionally excludes volatile metadata such
 * as `generatedAt`.
 */
internal object FigmaDesignModelHash {
    private val canonicalJson = Json {
        prettyPrint = false
        explicitNulls = true
    }

    /**
     * Returns a `sha256:<hex>` digest for the canonicalized [model].
     */
    fun compute(model: JsonElement): String {
        val bytes = canonicalJson
            .encodeToString(JsonElement.serializer(), model.canonicalized())
            .toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return "sha256:" + digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private fun JsonElement.canonicalized(): JsonElement =
        when (this) {
            is JsonArray -> JsonArray(map { element -> element.canonicalized() })
            is JsonObject -> JsonObject(
                entries
                    .sortedBy(Map.Entry<String, JsonElement>::key)
                    .associate { (key, value) -> key to value.canonicalized() }
            )
            else -> this
        }
}
