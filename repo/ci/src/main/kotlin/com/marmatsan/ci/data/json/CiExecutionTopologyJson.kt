package com.marmatsan.ci.data.json

import com.marmatsan.ci.domain.model.CiExecutionTopology
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CiExecutionTopologyJson {
    fun write(topology: CiExecutionTopology, output: File) {
        output.parentFile.mkdirs()
        output.writeText(format.encodeToString(topology) + System.lineSeparator())
    }

    fun read(source: String): CiExecutionTopology = format.decodeFromString(source)

    private companion object {
        val format = Json {
            prettyPrint = true
            encodeDefaults = true
        }
    }
}
