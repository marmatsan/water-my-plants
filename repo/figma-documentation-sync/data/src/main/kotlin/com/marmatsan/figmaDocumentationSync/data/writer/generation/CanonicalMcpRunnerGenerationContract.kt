package com.marmatsan.figmaDocumentationSync.data.writer.generation

import com.marmatsan.figmaDocumentationSync.data.writer.CanonicalMcpRunnerGenerator

/** Internal wire and filesystem constants shared by canonical runner collaborators. */
internal object CanonicalMcpRunnerGenerationContract {
    /** Version of the payload-transport identity contract. */
    const val TRANSPORT_CONTRACT_VERSION = 2

    /** Default shared-plugin-data chunk length. */
    const val DEFAULT_CHUNK_SIZE = 30_000

    /** Smallest supported shared-plugin-data chunk length. */
    const val MINIMUM_CHUNK_SIZE = 1_000

    /** Maximum supported shared-plugin-data value length. */
    const val MAX_SHARED_PLUGIN_DATA_ENTRY_LENGTH = 100_000

    /** UTF-8 byte-order mark removed from model input when present. */
    const val UTF8_BOM = "\uFEFF"

    /** Visual runner directory name. */
    const val VISUAL_DIRECTORY = "visual"

    /** Metadata runner directory name. */
    const val METADATA_DIRECTORY = "metadata"

    /** Executable runner manifest file name. */
    const val MANIFEST_FILE = "manifest.json"

    /** Canonical PNG payload file name. */
    const val PAYLOAD_PNG_FILE = "10-canonical-sync-payload.png"

    /** Staging reset source file name. */
    const val CLEAR_STAGING_FILE = "00-clear-staging.mcp.js"

    /** PNG staging source file name. */
    const val STAGE_PAYLOAD_FILE = "10-stage-payload-from-png.mcp.js"

    /** Staging finalization source file name. */
    const val FINALIZE_STAGING_FILE = "90-finalize-staging.mcp.js"

    /** Payload transports accepted by canonical runner generation. */
    val supportedTransports =
        setOf(
            CanonicalMcpRunnerGenerator.TRANSPORT_PNG,
            CanonicalMcpRunnerGenerator.TRANSPORT_CHUNKS
        )
}
