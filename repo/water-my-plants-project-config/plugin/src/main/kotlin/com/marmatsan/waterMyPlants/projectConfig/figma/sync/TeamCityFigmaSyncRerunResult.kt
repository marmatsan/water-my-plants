package com.marmatsan.waterMyPlants.projectConfig.figma.sync

/**
 * Observable outcome of validating or rerunning the canonical Figma Sync pipeline.
 *
 * @property runId TeamCity run id, absent for validation-only execution.
 * @property webUrl optional operator link to the TeamCity run.
 * @property branch branch constrained by the repository adapter.
 * @property state resulting TeamCity or validation state.
 * @property reused whether an already active run satisfied the request.
 */
data class TeamCityFigmaSyncRerunResult(
    val runId: Long?,
    val webUrl: String?,
    val branch: String,
    val state: String,
    val reused: Boolean,
)
