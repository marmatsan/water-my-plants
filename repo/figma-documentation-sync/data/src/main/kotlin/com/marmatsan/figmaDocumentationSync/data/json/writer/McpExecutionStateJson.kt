package com.marmatsan.figmaDocumentationSync.data.json.writer

import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpCompletedFile
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpExecutionFailure
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpExecutionIdentity
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpExecutionState
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

/** Atomic JSON checkpoint adapter for MCP execution state. */
class McpExecutionStateJson {
    fun readOptional(path: String): McpExecutionState? {
        val source = Path.of(path)
        if (!Files.isRegularFile(source)) return null
        val state = Json.parseToJsonElement(Files.readString(source).removePrefix(UTF8_BOM)).jsonObject.toState()
        require(state.schemaVersion == SUPPORTED_SCHEMA_VERSION) {
            "Unsupported MCP execution state schema ${state.schemaVersion}; expected $SUPPORTED_SCHEMA_VERSION."
        }
        return state
    }

    fun writeAtomic(state: McpExecutionState, path: String) {
        val output = Path.of(path)
        output.parent?.let(Files::createDirectories)
        val temporary = output.resolveSibling("${output.fileName}.${ProcessHandle.current().pid()}.tmp")
        Files.writeString(
            temporary,
            prettyJson.encodeToString(JsonObject.serializer(), state.toJson()) + System.lineSeparator()
        )
        try {
            Files.move(
                temporary,
                output,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun McpExecutionState.toJson(): JsonObject = buildJsonObject {
        put("schemaVersion", schemaVersion)
        put("identity", buildJsonObject {
            put("modelHash", identity.modelHash)
            put("gitSha", identity.gitSha)
            put("writerHash", identity.writerHash)
            put("transportHash", identity.transportHash)
            put("manifestHash", identity.manifestHash)
        })
        put("startedAt", startedAt)
        put("updatedAt", updatedAt)
        put("completedFiles", JsonArray(completedFiles.map { entry -> entry.toJson() }))
        put("plannedFiles", JsonArray(plannedFiles.map(::JsonPrimitive)))
        put("failedFile", failedFile?.let(::JsonPrimitive) ?: JsonNull)
        put("failure", failure?.toJson() ?: JsonNull)
    }

    private fun McpCompletedFile.toJson(): JsonObject = buildJsonObject {
        put("file", file)
        put("fileHash", fileHash)
        put("durationMs", durationMs)
        put("completedAt", completedAt)
        put("summary", summary?.let(::JsonPrimitive) ?: JsonNull)
    }

    private fun McpExecutionFailure.toJson(): JsonObject = buildJsonObject {
        put("message", message)
        put("durationMs", durationMs)
        put("failedAt", failedAt)
    }

    private fun JsonObject.toState(): McpExecutionState {
        val identity = getValue("identity").jsonObject
        return McpExecutionState(
            schemaVersion = getValue("schemaVersion").jsonPrimitive.content.toInt(),
            identity = McpExecutionIdentity(
                modelHash = identity.getValue("modelHash").jsonPrimitive.content,
                gitSha = identity.getValue("gitSha").jsonPrimitive.content,
                writerHash = identity.getValue("writerHash").jsonPrimitive.content,
                transportHash = identity.getValue("transportHash").jsonPrimitive.content,
                manifestHash = identity.getValue("manifestHash").jsonPrimitive.content
            ),
            startedAt = getValue("startedAt").jsonPrimitive.content,
            updatedAt = getValue("updatedAt").jsonPrimitive.content,
            completedFiles = getValue("completedFiles").jsonArray.map { value -> value.jsonObject.toCompleted() },
            plannedFiles = getValue("plannedFiles").jsonArray.map { value -> value.jsonPrimitive.content },
            failedFile = getValue("failedFile").jsonPrimitive.contentOrNull,
            failure = getValue("failure").takeUnless { value -> value === JsonNull }?.jsonObject?.toFailure()
        )
    }

    private fun JsonObject.toCompleted(): McpCompletedFile = McpCompletedFile(
        file = getValue("file").jsonPrimitive.content,
        fileHash = getValue("fileHash").jsonPrimitive.content,
        durationMs = getValue("durationMs").jsonPrimitive.long,
        completedAt = getValue("completedAt").jsonPrimitive.content,
        summary = getValue("summary").jsonPrimitive.contentOrNull
    )

    private fun JsonObject.toFailure(): McpExecutionFailure = McpExecutionFailure(
        message = getValue("message").jsonPrimitive.content,
        durationMs = getValue("durationMs").jsonPrimitive.long,
        failedAt = getValue("failedAt").jsonPrimitive.content
    )

    private companion object {
        const val UTF8_BOM = "\uFEFF"
        const val SUPPORTED_SCHEMA_VERSION = 1
        val prettyJson = Json { prettyPrint = true }
    }
}
