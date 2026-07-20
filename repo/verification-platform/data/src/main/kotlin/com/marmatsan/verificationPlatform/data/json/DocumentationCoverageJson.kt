package com.marmatsan.verificationPlatform.data.json

import com.marmatsan.verificationPlatform.domain.model.DocumentationCoverageRule
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Parses the versioned documentation coverage manifest. */
class DocumentationCoverageJson {
    /**
     * Parses [source] and returns provider-neutral coverage rules.
     *
     * @throws kotlinx.serialization.SerializationException when the manifest is
     * malformed or does not satisfy the versioned JSON shape.
     */
    fun read(source: String): List<DocumentationCoverageRule> =
        format.decodeFromString<Manifest>(source).rules.map { rule ->
            DocumentationCoverageRule(
                id = rule.id,
                sourcePaths = rule.sourcePaths,
                documentationPaths = rule.documentationPaths
            )
        }

    @Serializable
    private data class Manifest(
        val schemaVersion: Int,
        val rules: List<Rule>
    )

    @Serializable
    private data class Rule(
        val id: String,
        val sourcePaths: List<String>,
        val documentationPaths: List<String>
    )

    private companion object {
        val format = Json
    }
}
