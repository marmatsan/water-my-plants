package com.marmatsan.verificationPlatform.domain.model

import kotlinx.serialization.Serializable

/**
 * Ordered verification work that one CI agent may execute in a single job.
 *
 * @property id stable lane identifier used by provider adapters and dependency
 * references.
 * @property verificationUnits required units executed by this lane in plan
 * order.
 * @property needs lane identifiers that must finish successfully before this
 * lane starts.
 * @property capabilities distinct agent capabilities required by the lane's
 * units.
 * @property parallelSafe whether every unit in the lane permits concurrent
 * execution.
 * @property publishesAuthoritativeStatus whether this lane alone publishes the
 * final CI result.
 */
@Serializable
data class CiExecutionLane(
    val id: String,
    val verificationUnits: List<VerificationUnitId>,
    val needs: List<String>,
    val capabilities: List<String>,
    val parallelSafe: Boolean,
    val publishesAuthoritativeStatus: Boolean,
)
