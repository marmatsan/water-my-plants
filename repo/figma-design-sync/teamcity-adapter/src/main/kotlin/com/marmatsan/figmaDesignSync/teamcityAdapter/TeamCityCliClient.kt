package com.marmatsan.figmaDesignSync.teamcityAdapter

import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** TeamCity CLI adapter used by local Kotlin operational tasks. */
class TeamCityCliClient(
    private val execute: (List<String>) -> CommandResult = { arguments -> executeProcess(arguments) }
) : TeamCityBuildArtifactClient {
    override fun readBuild(buildId: Long): TeamCityBuild {
        val result = executeTeamCity("run", "view", buildId.toString(), "--json")
        val root = runCatching { Json.parseToJsonElement(result.output).jsonObject }
            .getOrElse { error ->
                throw IllegalArgumentException("TeamCity CLI returned invalid build JSON.", error)
            }
        root["error"]?.jsonObject?.let { error ->
            val message = error["message"]?.jsonPrimitive?.content ?: "unknown TeamCity CLI error"
            throw IllegalArgumentException("TeamCity CLI failed: $message")
        }
        val buildType = root["buildType"]?.jsonObject
            ?: throw IllegalArgumentException("TeamCity build JSON is missing 'buildType'.")

        return TeamCityBuild(
            id = root.requiredString("id").toLong(),
            state = root.requiredString("state"),
            status = root.requiredString("status"),
            branchName = root.requiredString("branchName"),
            buildTypeName = buildType.requiredString("name"),
            webUrl = root["webUrl"]?.jsonPrimitive?.content
        )
    }

    override fun downloadArtifacts(buildId: Long, outputDirectory: File) {
        outputDirectory.mkdirs()
        executeTeamCity(
            "run",
            "download",
            buildId.toString(),
            "--output",
            outputDirectory.absolutePath
        )
    }

    private fun executeTeamCity(vararg arguments: String): CommandResult {
        val command = listOf("teamcity", "--no-color", "--no-input") + arguments
        val result = execute(command)
        require(result.exitCode == 0) {
            "TeamCity CLI failed with exit code ${result.exitCode}: ${result.error.trim()}"
        }
        return result
    }

    data class CommandResult(
        val exitCode: Int,
        val output: String,
        val error: String
    )

    private companion object {
        fun executeProcess(arguments: List<String>): CommandResult {
            val process = ProcessBuilder(arguments).start()
            val output = ByteArrayOutputStream()
            val error = ByteArrayOutputStream()
            process.inputStream.use { input -> input.copyTo(output) }
            process.errorStream.use { input -> input.copyTo(error) }
            return CommandResult(
                exitCode = process.waitFor(),
                output = output.toString().trim(),
                error = error.toString().trim()
            )
        }
    }
}

private fun kotlinx.serialization.json.JsonObject.requiredString(name: String): String =
    this[name]?.jsonPrimitive?.content
        ?: throw IllegalArgumentException("TeamCity build JSON is missing '$name'.")
