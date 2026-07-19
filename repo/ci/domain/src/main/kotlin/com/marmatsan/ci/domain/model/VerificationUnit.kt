package com.marmatsan.ci.domain.model

import kotlinx.serialization.Serializable

/** One allow-listed unit that a CI provider may execute sequentially or in parallel. */
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
