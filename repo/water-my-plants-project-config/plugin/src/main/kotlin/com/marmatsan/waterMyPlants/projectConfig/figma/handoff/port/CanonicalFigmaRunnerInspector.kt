package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.port

import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model.CanonicalFigmaRunnerInspection
import java.nio.file.Path

/** Supplies the deterministic runner inspection required to create the handoff. */
internal fun interface CanonicalFigmaRunnerInspector {
    /** Inspects [manifestPath] under the execution selection recorded by [planPath]. */
    fun inspect(
        manifestPath: Path,
        planPath: Path,
    ): CanonicalFigmaRunnerInspection
}
