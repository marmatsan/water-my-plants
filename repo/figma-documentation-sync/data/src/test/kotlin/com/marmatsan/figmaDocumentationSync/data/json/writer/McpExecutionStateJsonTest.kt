package com.marmatsan.figmaDocumentationSync.data.json.writer

import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpExecutionIdentity
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpExecutionState
import com.marmatsan.unitTest.dsl.given
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldBeEmpty

class McpExecutionStateJsonTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "mcp-execution-state"
                )

            test("removes the temporary checkpoint when replacement fails") {
                given {
                    temporaryDirectory
                        .resolve("execution-state.json")
                        .apply {
                            mkdirs()
                            resolve("blocking-entry").writeText("keep destination non-empty")
                        }
                }.whenever { output ->
                    shouldThrowAny {
                        McpExecutionStateJson().writeAtomic(
                            state = executionState(),
                            path = output.absolutePath
                        )
                    }
                }.then {
                    temporaryDirectory
                        .listFiles { file -> file.extension == "tmp" }
                        .orEmpty()
                        .shouldBeEmpty()
                }
            }
        }
    )

private fun executionState(): McpExecutionState =
    McpExecutionState(
        schemaVersion = 1,
        identity =
            McpExecutionIdentity(
                modelHash = "model-hash",
                gitSha = "git-sha",
                writerHash = "writer-hash",
                transportHash = "transport-hash",
                manifestHash = "manifest-hash"
            ),
        startedAt = "2026-07-27T00:00:00Z",
        updatedAt = "2026-07-27T00:00:00Z",
        completedFiles = emptyList(),
        plannedFiles = emptyList(),
        failedFile = null
    )
