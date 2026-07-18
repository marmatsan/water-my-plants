package com.marmatsan.figmaDesignSync.domain.port.impact

import com.marmatsan.figmaDesignSync.domain.model.impact.FigmaChangeImpactPolicy

/** Loads the versioned Figma change-impact policy from an external source. */
fun interface FigmaChangeImpactPolicyPort {
    fun read(sourcePath: String): FigmaChangeImpactPolicy
}
