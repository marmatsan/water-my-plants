package com.marmatsan.figmaDocumentationSync.plugin.task.canonical

import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaVerificationScope
import com.marmatsan.figmaDocumentationSync.plugin.di.FigmaDocumentationSyncComponent
import com.marmatsan.figmaDocumentationSync.plugin.di.create
import java.io.File

/** Resolves whether a canonical phase must execute from its persisted scope artifact. */
internal object CanonicalFigmaFullVerificationResolver {
    /** Reads the classified change-impact artifact produced before model generation. */
    fun fromChangeImpact(
        changeImpactFile: File
    ): Boolean =
        FigmaDocumentationSyncComponent::class
            .create()
            .canonicalFigmaSyncScopeJson
            .readChangeImpact(changeImpactFile.absolutePath)
            .scope == FigmaVerificationScope.FULL_VERIFICATION

    /** Reads the validated wire value produced before the Figma trunk check. */
    fun fromVerifiedScope(
        verifiedScopeFile: File
    ): Boolean =
        verifiedScopeFile.readText().trim() == FigmaVerificationScope.FULL_VERIFICATION.wireValue
}
