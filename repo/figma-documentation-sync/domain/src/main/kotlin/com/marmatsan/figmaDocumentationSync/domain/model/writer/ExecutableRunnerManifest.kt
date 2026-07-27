package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Complete executable contract for one generated MCP runner directory.
 *
 * @property path repository-relative runner directory.
 * @property schemaVersion runner manifest schema version.
 * @property mode runner execution mode.
 * @property entrypoint generated script entrypoint.
 * @property target primary visual target.
 * @property targets all visual targets included in the runner.
 * @property writeMetadata whether execution writes canonical metadata.
 * @property transport MCP transport selected by the runner.
 * @property namespace shared plugin-data namespace used for staging.
 * @property sectionNodeId optional section node constrained by the runner.
 * @property roots writer roots included in the execution.
 * @property allowCanonicalSections whether canonical Figma sections may be changed.
 * @property fullVisualSync whether the runner performs the complete visual write.
 * @property allowPartial whether a subset of planned files may be executed.
 * @property metadataPageId Figma page that owns canonical metadata.
 * @property modelPath path of the design model consumed by the runner.
 * @property scriptPath path of the generated runner script.
 * @property modelHash canonical design-model hash.
 * @property gitSha canonical Git revision.
 * @property designModelLength design-model byte length used for identity checks.
 * @property scriptLength runner script byte length used for identity checks.
 * @property writerHash hash of writer behavior affecting visual output.
 * @property transportHash hash of MCP transport behavior.
 * @property targetFingerprints visual target fingerprints keyed by target.
 * @property writerScopeFingerprints writer-scope fingerprints keyed by scope.
 * @property writerScopeFingerprintSchemaVersion fingerprint calculation schema version.
 * @property executionScopes file-to-execution-scope mapping.
 * @property payloadImage optional pre-rendered image payload.
 * @property files ordered runner files available for execution.
 * @property fileHashes content hashes keyed by runner file.
 * @property manifestHash hash covering the complete runner manifest identity.
 */
data class ExecutableRunnerManifest(
    val path: String,
    val schemaVersion: Int,
    val mode: String,
    val entrypoint: String,
    val target: String,
    val targets: List<String>,
    val writeMetadata: Boolean,
    val transport: String,
    val namespace: String,
    val sectionNodeId: String?,
    val roots: List<String>,
    val allowCanonicalSections: Boolean,
    val fullVisualSync: Boolean,
    val allowPartial: Boolean,
    val metadataPageId: String,
    val modelPath: String,
    val scriptPath: String,
    val modelHash: String,
    val gitSha: String,
    val designModelLength: Int,
    val scriptLength: Int,
    val writerHash: String,
    val transportHash: String,
    val targetFingerprints: Map<String, String>,
    val writerScopeFingerprints: Map<String, String>,
    val writerScopeFingerprintSchemaVersion: Int,
    val executionScopes: Map<String, String>,
    val payloadImage: RunnerPayloadImage?,
    val files: List<String>,
    val fileHashes: Map<String, String>,
    val manifestHash: String,
)
