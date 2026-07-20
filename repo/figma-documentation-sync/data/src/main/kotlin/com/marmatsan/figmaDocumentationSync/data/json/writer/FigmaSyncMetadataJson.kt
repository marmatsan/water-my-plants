package com.marmatsan.figmaDocumentationSync.data.json.writer

import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaSyncMetadata
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Decodes writer identity values stored in Figma shared plugin data. */
object FigmaSyncMetadataJson {
    fun read(
        sharedPluginData: Map<String, Map<String, String>>,
        namespace: String
    ): FigmaSyncMetadata? {
        val metadata = sharedPluginData[namespace] ?: return null
        return FigmaSyncMetadata(
            modelHash = metadata["modelHash"],
            writerHash = metadata["writerHash"],
            targetFingerprints = metadata["targetFingerprints"].toStringMapOrNull(),
            writerScopeFingerprints = metadata["writerScopeFingerprints"].toStringMapOrNull(),
            writerScopeFingerprintSchemaVersion = metadata["writerScopeFingerprintSchemaVersion"]?.toIntOrNull()
        )
    }

    private fun String?.toStringMapOrNull(): Map<String, String>? =
        this?.let { value ->
            runCatching {
                Json.parseToJsonElement(value).jsonObject.mapValues { (_, element) -> element.jsonPrimitive.content }
            }.getOrNull()
        }
}
