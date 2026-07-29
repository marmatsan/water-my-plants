package com.marmatsan.figmaDocumentationSync.plugin.errorhandling

import com.marmatsan.figmaDocumentationSync.domain.model.figma.FigmaNodeContentError
import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class FigmaNodeContentErrorMessageTest :
    FunSpec(
        {
            listOf(
                FigmaNodeContentError.RequestRejected(
                    403,
                    "forbidden"
                ) to
                    "Figma node-content request failed with HTTP 403: forbidden",
                FigmaNodeContentError.TimedOut(60_000) to
                    "Figma node-content request timed out after 60000 ms.",
                FigmaNodeContentError.NotFound("node-id") to
                    "Figma node 'node-id' was not found.",
                FigmaNodeContentError.InvalidResponse to
                    "Figma returned an invalid node-content response.",
                FigmaNodeContentError.Unavailable("connection refused") to
                    "Figma node-content transport is unavailable: connection refused"
            ).forEach { (error, expectedMessage) ->
                test("renders ${error::class.simpleName} for a Gradle operator") {
                    given {
                        error
                    }.whenever { failure ->
                        failure.operatorMessage()
                    }.then { message ->
                        message shouldBe expectedMessage
                    }
                }
            }
        }
    )
