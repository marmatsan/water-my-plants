package com.marmatsan.ci.data.json

import com.marmatsan.ci.domain.model.CiPlan
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CiPlanJson {
    fun write(plan: CiPlan, output: File) {
        output.parentFile.mkdirs()
        output.writeText(format.encodeToString(plan) + System.lineSeparator())
    }

    fun read(source: String): CiPlan = format.decodeFromString(source)

    private companion object {
        val format = Json {
            prettyPrint = true
            encodeDefaults = true
            explicitNulls = true
        }
    }
}
