package com.marmatsan.figmaDocumentationSync.data.writer.generation

import com.marmatsan.figmaDocumentationSync.data.writer.CanonicalMcpRunnerGenerator
import kotlinx.serialization.json.JsonObject
import java.nio.file.Path

/** Immutable validated inputs and fingerprints shared by runner-directory generation. */
internal data class CanonicalMcpRunnerGenerationContext(
    /** Original public generation request. */
    val request: CanonicalMcpRunnerGenerator.Request,
    /** Normalized output root containing visual and metadata runners. */
    val outputRoot: Path,
    /** Parsed canonical design-model object. */
    val designModel: JsonObject,
    /** Compact design-model JSON staged for execution. */
    val modelJson: String,
    /** Writer script staged for execution. */
    val script: String,
    /** Canonical design-model hash. */
    val modelHash: String,
    /** Canonical Git revision. */
    val gitSha: String,
    /** SHA-256 identity of the writer script. */
    val writerHash: String,
    /** SHA-256 identity of the selected transport contract. */
    val transportHash: String,
    /** Fingerprints for every configured Figma target. */
    val targetFingerprints: Map<String, String>,
    /** Fingerprints for writer source scopes. */
    val writerScopeFingerprints: Map<String, String>
)
