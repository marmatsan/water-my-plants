package com.marmatsan.verificationPlatform.data.json

import com.marmatsan.verificationPlatform.domain.model.CiPlan
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/** Serializes and deserializes the versioned provider-neutral CI plan. */
class CiPlanJson {
    /**
     * Writes [plan] as deterministic, human-readable JSON to [output].
     *
     * Default and explicit `null` values remain present so adapters observe the
     * complete schema. Missing parent directories are created.
     */
    fun write(
        plan: CiPlan,
        output: File,
    ) {
        output.parentFile.mkdirs()
        output.writeText(format.encodeToString(plan) + System.lineSeparator())
    }

    /**
     * Parses [source] into a provider-neutral CI plan.
     *
     * @throws kotlinx.serialization.SerializationException when the JSON does
     * not satisfy the serialized contract.
     */
    fun read(
        source: String,
    ): CiPlan = format.decodeFromString(source)

    private companion object {
        val format =
            Json {
                prettyPrint = true
                encodeDefaults = true
                explicitNulls = true
            }
    }
}
