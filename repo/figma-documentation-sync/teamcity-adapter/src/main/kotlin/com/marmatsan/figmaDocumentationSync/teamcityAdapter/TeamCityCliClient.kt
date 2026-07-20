package com.marmatsan.figmaDocumentationSync.teamcityAdapter

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayOutputStream
import java.io.File

/** TeamCity CLI adapter used by local Kotlin operational tasks. */
class TeamCityCliClient(
    private val environment: Map<String, String?> = emptyMap(),
    private val workingDirectory: File? = null,
    private val execute: (List<String>, Map<String, String?>) -> CommandResult =
        { arguments, commandEnvironment ->
            executeProcess(
                arguments = arguments,
                environment = commandEnvironment,
                workingDirectory = workingDirectory,
            )
        },
) : TeamCityBuildArtifactClient,
    TeamCityRunClient {
    override fun readBuild(
        buildId: Long,
    ): TeamCityBuild {
        val root =
            executeJson(
                "run",
                "view",
                buildId.toString(),
                "--json",
            )
        val buildType =
            root["buildType"]?.jsonObject
                ?: throw IllegalArgumentException("TeamCity build JSON is missing 'buildType'.")

        return TeamCityBuild(
            id = root.requiredString("id").toLong(),
            state = root.requiredString("state"),
            status = root.requiredString("status"),
            branchName = root.requiredString("branchName"),
            buildTypeName = buildType.requiredString("name"),
            webUrl = root["webUrl"]?.jsonPrimitive?.content,
        )
    }

    override fun listRuns(
        buildTypeId: String,
        branch: String,
        status: String,
        limit: Int,
    ): List<TeamCityRun> {
        require(limit > 0) { "TeamCity run list limit must be positive." }
        val root =
            executeJson(
                "run",
                "list",
                "--job",
                buildTypeId,
                "--branch",
                branch,
                "--status",
                status,
                "--limit",
                limit.toString(),
                "--json",
            )
        return root["build"]?.jsonArray.orEmpty().map { it.jsonObject.toTeamCityRun() }
    }

    override fun startRun(
        buildTypeId: String,
        branch: String,
    ): TeamCityRun =
        executeJson(
            "run",
            "start",
            buildTypeId,
            "--branch",
            branch,
            "--json",
        ).toTeamCityRun()

    override fun watchRun(
        buildId: Long,
        pollIntervalSeconds: Int,
        timeoutMinutes: Int,
    ): TeamCityRun {
        require(pollIntervalSeconds in 1..300) {
            "TeamCity polling interval must be between 1 and 300 seconds."
        }
        require(timeoutMinutes in 1..1440) {
            "TeamCity timeout must be between 1 and 1440 minutes."
        }
        return executeJson(
            "run",
            "watch",
            buildId.toString(),
            "--interval",
            pollIntervalSeconds.toString(),
            "--timeout",
            "${timeoutMinutes}m",
            "--json",
        ).toTeamCityRun()
    }

    override fun readRun(
        buildId: Long,
    ): TeamCityRun =
        executeJson(
            "run",
            "view",
            buildId.toString(),
            "--json",
        ).toTeamCityRun()

    override fun downloadArtifacts(
        buildId: Long,
        outputDirectory: File,
    ) {
        outputDirectory.mkdirs()
        executeTeamCity(
            "run",
            "download",
            buildId.toString(),
            "--output",
            outputDirectory.absolutePath,
        )
    }

    private fun executeTeamCity(
        vararg arguments: String,
    ): CommandResult {
        val command =
            listOf(
                "teamcity",
                "--no-color",
                "--no-input",
            ) + arguments
        val result =
            execute(
                command,
                environment,
            )
        require(result.exitCode == 0) {
            "TeamCity CLI failed with exit code ${result.exitCode}: ${result.error.trim()}"
        }
        return result
    }

    private fun executeJson(
        vararg arguments: String,
    ): JsonObject {
        val result =
            executeTeamCity(
                arguments = *arguments,
            )
        val root =
            runCatching { Json.parseToJsonElement(result.output).jsonObject }
                .getOrElse { error ->
                    throw IllegalArgumentException(
                        "TeamCity CLI returned invalid JSON.",
                        error,
                    )
                }
        root["error"]?.jsonObject?.let { error ->
            val message = error["message"]?.jsonPrimitive?.content ?: "unknown TeamCity CLI error"
            throw IllegalArgumentException("TeamCity CLI failed: $message")
        }
        return root
    }

    data class CommandResult(
        val exitCode: Int,
        val output: String,
        val error: String,
    )

    private companion object {
        fun executeProcess(
            arguments: List<String>,
            environment: Map<String, String?>,
            workingDirectory: File?,
        ): CommandResult {
            val processBuilder = ProcessBuilder(arguments)
            workingDirectory?.let(processBuilder::directory)
            environment.forEach { (name, value) ->
                if (value == null) {
                    processBuilder.environment().remove(name)
                } else {
                    processBuilder.environment()[name] = value
                }
            }
            val process = processBuilder.start()
            val output = ByteArrayOutputStream()
            val error = ByteArrayOutputStream()
            process.inputStream.use { input -> input.copyTo(output) }
            process.errorStream.use { input -> input.copyTo(error) }
            return CommandResult(
                exitCode = process.waitFor(),
                output = output.toString().trim(),
                error = error.toString().trim(),
            )
        }
    }
}

private fun JsonObject.requiredString(
    name: String,
): String =
    this[name]?.jsonPrimitive?.content
        ?: throw IllegalArgumentException("TeamCity build JSON is missing '$name'.")

private fun JsonObject.toTeamCityRun(): TeamCityRun =
    TeamCityRun(
        id =
            requiredString(
                name = "id",
            ).toLong(),
        state =
            requiredString(
                name = "state",
            ),
        status = this["status"]?.jsonPrimitive?.content,
        statusText = this["statusText"]?.jsonPrimitive?.content,
        branchName = this["branchName"]?.jsonPrimitive?.content,
        webUrl = this["webUrl"]?.jsonPrimitive?.content,
    )
