package com.marmatsan.figmaDocumentationSync.data.writer

import com.marmatsan.figmaDocumentationSync.data.datasource.impact.FigmaChangeImpactPolicyDataSource
import com.marmatsan.figmaDocumentationSync.data.fingerprint.FigmaTargetFingerprintCalculator
import com.marmatsan.figmaDocumentationSync.data.fingerprint.WriterScopeFingerprintCalculator
import com.marmatsan.figmaDocumentationSync.data.json.writer.ExecutableRunnerManifestJson
import com.marmatsan.figmaDocumentationSync.data.png.PayloadPngEncoder
import com.marmatsan.figmaDocumentationSync.data.writer.generation.CanonicalMcpRunnerContextFactory
import com.marmatsan.figmaDocumentationSync.data.writer.generation.CanonicalMcpRunnerDirectoryGenerator
import com.marmatsan.figmaDocumentationSync.data.writer.generation.CanonicalMcpRunnerGenerationContract
import com.marmatsan.figmaDocumentationSync.data.writer.generation.CanonicalMcpRunnerStagingPlanner
import com.marmatsan.figmaDocumentationSync.data.writer.generation.CanonicalMcpRunnerTargetPlanner
import com.marmatsan.figmaDocumentationSync.domain.model.writer.ExecutableRunnerManifest
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaWriterRuntimeConfig

/** Generates canonical visual and metadata MCP runner directories without Node orchestration. */
class CanonicalMcpRunnerGenerator(
    renderer: McpRunnerSourceRenderer = McpRunnerSourceRenderer(),
    targetFingerprints: FigmaTargetFingerprintCalculator = FigmaTargetFingerprintCalculator(),
    writerFingerprints: WriterScopeFingerprintCalculator = WriterScopeFingerprintCalculator(),
    manifestJson: ExecutableRunnerManifestJson = ExecutableRunnerManifestJson(),
    payloadEncoder: PayloadPngEncoder = PayloadPngEncoder(),
    policySource: FigmaChangeImpactPolicyDataSource = FigmaChangeImpactPolicyDataSource()
) {
    private val contextFactory =
        CanonicalMcpRunnerContextFactory(
            renderer = renderer,
            targetFingerprints = targetFingerprints,
            writerFingerprints = writerFingerprints,
            policySource = policySource
        )
    private val directoryGenerator =
        CanonicalMcpRunnerDirectoryGenerator(
            stagingPlanner =
                CanonicalMcpRunnerStagingPlanner(
                    renderer = renderer,
                    payloadEncoder = payloadEncoder
                ),
            targetPlanner = CanonicalMcpRunnerTargetPlanner(renderer),
            manifestJson = manifestJson
        )

    /** Generates deterministic visual and metadata runner artifacts for [request]. */
    fun generate(
        request: Request
    ): Manifests {
        val context = contextFactory.create(request)
        val visual =
            directoryGenerator.generate(
                context = context,
                outputDirectory =
                    context.outputRoot.resolve(
                        CanonicalMcpRunnerGenerationContract.VISUAL_DIRECTORY
                    ),
                targets = request.config.visualTargetNames,
                writeMetadata = false,
                fullVisualSync = true
            )
        val metadata =
            directoryGenerator.generate(
                context = context,
                outputDirectory =
                    context.outputRoot.resolve(
                        CanonicalMcpRunnerGenerationContract.METADATA_DIRECTORY
                    ),
                targets = listOf("metadata"),
                writeMetadata = true,
                fullVisualSync = false
            )
        return Manifests(
            visualManifest = visual,
            metadataManifest = metadata
        )
    }

    /**
     * Inputs required to generate portable MCP runner directories.
     *
     * @property modelPath Canonical design-model JSON used as the runner input.
     * @property scriptPath Figma writer script bundled into canonical staging.
     * @property outputDirectory Root under which visual and metadata runners are recreated.
     * @property toolsDirectory Base directory used to make manifest paths portable.
     * @property writerSourceDirectory Root whose writer sources contribute to scope fingerprints.
     * @property repositoryRootDirectory Repository root used to normalize fingerprint paths.
     * @property changeImpactPolicyPath Policy file that maps writer changes to affected scopes.
     * @property config Runtime project contract that selects targets and Figma destinations.
     * @property transport Payload transport, either [TRANSPORT_PNG] or [TRANSPORT_CHUNKS].
     * @property chunkSize Maximum staging chunk length when chunk transport is selected.
     */
    data class Request(
        val modelPath: String,
        val scriptPath: String,
        val outputDirectory: String,
        val toolsDirectory: String,
        val writerSourceDirectory: String,
        val repositoryRootDirectory: String,
        val changeImpactPolicyPath: String,
        val config: FigmaWriterRuntimeConfig,
        val transport: String = TRANSPORT_PNG,
        val chunkSize: Int = CanonicalMcpRunnerGenerationContract.DEFAULT_CHUNK_SIZE
    )

    /**
     * Manifests produced for the two canonical runner directories.
     *
     * @property visualManifest Manifest for visual documentation targets.
     * @property metadataManifest Manifest for the metadata target.
     */
    data class Manifests(
        val visualManifest: ExecutableRunnerManifest,
        val metadataManifest: ExecutableRunnerManifest
    )

    /** Stable wire values shared by Gradle tasks and generated runner manifests. */
    companion object {
        /** Payload transport that stages canonical input through a PNG asset. */
        const val TRANSPORT_PNG = "png"

        /** Payload transport that stages canonical input through JavaScript chunks. */
        const val TRANSPORT_CHUNKS = "chunks"

        /** Current executable runner manifest schema version. */
        const val MANIFEST_SCHEMA_VERSION = 4
    }
}
