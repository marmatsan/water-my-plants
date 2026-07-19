package com.marmatsan.ci.domain.model

import kotlinx.serialization.Serializable

/** Ordered verification work that one CI agent may execute in a single job. */
@Serializable
data class CiExecutionLane(
    val id: String,
    val verificationUnits: List<VerificationUnitId>,
    val needs: List<String>,
    val capabilities: List<String>,
    val parallelSafe: Boolean,
    val publishesAuthoritativeStatus: Boolean
)
