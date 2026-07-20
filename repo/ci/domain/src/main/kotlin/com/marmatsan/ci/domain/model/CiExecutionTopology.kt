package com.marmatsan.ci.domain.model

import kotlinx.serialization.Serializable

/**
 * Preview of how a provider may assign one verification plan to available
 * agents.
 *
 * [lanes] preserves all required plan units, while
 * [authoritativeStatusPublisherLaneId] identifies the only lane allowed to
 * publish the final CI status.
 */
@Serializable
data class CiExecutionTopology(
    val schemaVersion: Int,
    val activation: CiTopologyActivation,
    val sourcePlanSchemaVersion: Int,
    val sourceHead: String,
    val availableAgents: Int,
    val mode: CiTopologyMode,
    val lanes: List<CiExecutionLane>,
    val authoritativeStatusPublisherLaneId: String
)
