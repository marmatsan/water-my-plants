package com.marmatsan.verificationPlatform.data.json

import com.marmatsan.verificationPlatform.domain.model.CiExecutionTopology
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Serializes and deserializes the versioned CI execution-topology contract. */
class CiExecutionTopologyJson {
    /**
     * Writes [topology] as deterministic, human-readable JSON to [output].
     *
     * Missing parent directories are created and the document ends with the
     * platform line separator.
     */
    fun write(
        topology: CiExecutionTopology,
        output: File
    ) {
        output.parentFile.mkdirs()
        output.writeText(format.encodeToString(topology) + System.lineSeparator())
    }

    /**
     * Parses [source] into a provider-neutral execution topology.
     *
     * @throws kotlinx.serialization.SerializationException when the JSON does
     * not satisfy the serialized contract.
     */
    fun read(
        source: String
    ): CiExecutionTopology = format.decodeFromString(source)

    private companion object {
        val format = Json {
            prettyPrint = true
            encodeDefaults = true
        }
    }
}
