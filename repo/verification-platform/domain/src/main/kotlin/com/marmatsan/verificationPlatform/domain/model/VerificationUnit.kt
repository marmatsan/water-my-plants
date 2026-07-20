package com.marmatsan.verificationPlatform.domain.model

import kotlinx.serialization.Serializable

/**
 * One allow-listed unit that a CI provider may execute sequentially or in
 * parallel.
 *
 * @property id stable identifier understood by reviewed provider adapters.
 * @property required whether this unit applies to the current change.
 * @property needs units that must complete before this unit can start.
 * @property capabilities build-agent capabilities required for execution.
 * @property parallelSafe whether the unit may overlap independent work.
 * @property gradleTasks allow-listed Gradle task paths selected for this unit.
 * @property reasons human-readable explanations for requiring the unit.
 */
@Serializable
data class VerificationUnit(
    val id: VerificationUnitId,
    val required: Boolean,
    val needs: List<VerificationUnitId>,
    val capabilities: List<String>,
    val parallelSafe: Boolean,
    val gradleTasks: List<String>,
    val reasons: List<String>
)
