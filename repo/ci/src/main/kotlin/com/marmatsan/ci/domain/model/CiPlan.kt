package com.marmatsan.ci.domain.model

import kotlinx.serialization.Serializable

/** Provider-neutral verification plan generated for one repository revision. */
@Serializable
data class CiPlan(
    val schemaVersion: Int,
    val mode: CiPlanMode,
    val comparisonBase: String?,
    val head: String,
    val scope: CiScope,
    val changedFiles: List<String>,
    val changedModules: List<String>,
    val affectedModules: List<String>,
    val verificationUnits: List<VerificationUnit>,
    val fullVerification: Boolean,
    val fallbackReason: String?
)
