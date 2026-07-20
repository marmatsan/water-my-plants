package com.marmatsan.verificationPlatform.domain.model

import kotlinx.serialization.Serializable

/**
 * Preview of how a provider may assign one verification plan to available
 * agents.
 *
 * [lanes] preserves all required plan units, while
 * [authoritativeStatusPublisherLaneId] identifies the only lane allowed to
 * publish the final CI status.
 *
 * @property schemaVersion version of this serialized topology contract.
 * @property activation whether a provider may execute this topology or only
 * inspect it.
 * @property sourcePlanSchemaVersion schema version of the plan being projected.
 * @property sourceHead Git revision represented by the source plan.
 * @property availableAgents compatible agent count used for the projection.
 * @property mode sequential or parallel scheduling strategy.
 * @property lanes ordered execution lanes and their dependencies.
 * @property authoritativeStatusPublisherLaneId identifier of the only lane
 * allowed to publish the final CI status.
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
    val authoritativeStatusPublisherLaneId: String,
)
