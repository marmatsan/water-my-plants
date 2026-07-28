package com.marmatsan.figmaDocumentationSync.plugin.errorhandling

import com.marmatsan.figmaDocumentationSync.domain.model.figma.FigmaNodeContentError

/** Renders a typed Figma node-content failure for Gradle operators. */
internal fun FigmaNodeContentError.operatorMessage(): String =
    when (this) {
        is FigmaNodeContentError.RequestRejected -> {
            "Figma node-content request failed with HTTP $statusCode: $responseBody"
        }

        is FigmaNodeContentError.TimedOut -> {
            "Figma node-content request timed out after $timeoutMillis ms."
        }

        is FigmaNodeContentError.NotFound -> {
            "Figma node '$nodeId' was not found."
        }

        FigmaNodeContentError.InvalidResponse -> {
            "Figma returned an invalid node-content response."
        }

        is FigmaNodeContentError.Unavailable -> {
            "Figma node-content transport is unavailable: $detail"
        }
    }
