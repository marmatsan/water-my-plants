package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model

/**
 * Read-only execution projection used in the operator handoff.
 *
 * @property manifestHash integrity hash of the inspected runner manifest.
 * @property statePath resolved execution checkpoint path.
 * @property reuseStaging whether compatible canonical staging may be reused.
 * @property decision optional visual synchronization decision.
 * @property executionFiles ordered runner files still selected for execution.
 */
internal data class CanonicalFigmaRunnerInspection(
    val manifestHash: String,
    val statePath: String,
    val reuseStaging: Boolean,
    val decision: String?,
    val executionFiles: List<String>,
)
