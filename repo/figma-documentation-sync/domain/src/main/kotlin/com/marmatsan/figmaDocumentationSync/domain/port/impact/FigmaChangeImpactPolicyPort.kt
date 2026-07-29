package com.marmatsan.figmaDocumentationSync.domain.port.impact

import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaChangeImpactPolicy

/** Loads the versioned Figma change-impact policy from an external source. */
fun interface FigmaChangeImpactPolicyPort {
    /** Reads the versioned impact policy from repository-relative [sourcePath]. */
    fun read(
        sourcePath: String
    ): FigmaChangeImpactPolicy
}
