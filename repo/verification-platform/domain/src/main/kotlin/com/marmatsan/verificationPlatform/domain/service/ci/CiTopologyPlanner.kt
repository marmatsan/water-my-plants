package com.marmatsan.verificationPlatform.domain.service.ci

import com.marmatsan.verificationPlatform.domain.model.ci.CiExecutionLane
import com.marmatsan.verificationPlatform.domain.model.ci.CiExecutionTopology
import com.marmatsan.verificationPlatform.domain.model.ci.CiPlan
import com.marmatsan.verificationPlatform.domain.model.ci.CiTopologyActivation
import com.marmatsan.verificationPlatform.domain.model.ci.CiTopologyMode
import com.marmatsan.verificationPlatform.domain.model.ci.VerificationUnit
import com.marmatsan.verificationPlatform.domain.model.ci.VerificationUnitId

/**
 * Projects a verification plan onto the currently available build agents.
 *
 * The projection is preview-only and preserves every required unit plus one
 * authoritative status publisher. The stable behavior is documented by
 * `ci-execution-topology.feature`.
 */
class CiTopologyPlanner {
    /**
     * Creates an execution topology without changing the authority of [plan].
     *
     * @param plan provider-neutral verification plan to schedule.
     * @param availableAgents number of compatible agents available to a future
     * provider adapter; must be at least one.
     * @return a sequential or parallel preview containing every required unit
     * exactly once.
     */
    fun create(
        plan: CiPlan,
        availableAgents: Int,
    ): CiExecutionTopology {
        require(availableAgents >= 1) { "CI topology requires at least one available agent." }

        val requiredUnits = plan.verificationUnits.filter(VerificationUnit::required)
        require(requiredUnits.any { unit -> unit.id == VerificationUnitId.PUBLISH_REPORTS }) {
            "CI topology requires publish-reports as the authoritative final unit."
        }

        val mode =
            if (availableAgents == 1) {
                CiTopologyMode.SINGLE_AGENT_SEQUENTIAL
            } else {
                CiTopologyMode.MULTI_AGENT_PARALLEL
            }
        val lanes =
            when (mode) {
                CiTopologyMode.SINGLE_AGENT_SEQUENTIAL -> {
                    singleAgentLanes(
                        requiredUnits = requiredUnits,
                    )
                }

                CiTopologyMode.MULTI_AGENT_PARALLEL -> {
                    multiAgentLanes(
                        requiredUnits = requiredUnits,
                        availableAgents = availableAgents,
                    )
                }
            }
        check(
            lanes.flatMap(CiExecutionLane::verificationUnits) ==
                requiredUnits.map(
                    transform = VerificationUnit::id,
                ),
        ) {
            "CI topology must schedule every required verification unit exactly once and in plan order."
        }
        val statusPublisher = lanes.single(CiExecutionLane::publishesAuthoritativeStatus)

        return CiExecutionTopology(
            schemaVersion = SCHEMA_VERSION,
            activation = CiTopologyActivation.PREVIEW_ONLY,
            sourcePlanSchemaVersion = plan.schemaVersion,
            sourceHead = plan.head,
            availableAgents = availableAgents,
            mode = mode,
            lanes = lanes,
            authoritativeStatusPublisherLaneId = statusPublisher.id,
        )
    }

    private fun singleAgentLanes(
        requiredUnits: List<VerificationUnit>,
    ): List<CiExecutionLane> =
        listOf(
            lane(
                id = VERIFY_LANE,
                units = requiredUnits,
                publishesAuthoritativeStatus = true,
            ),
        )

    private fun multiAgentLanes(
        requiredUnits: List<VerificationUnit>,
        availableAgents: Int,
    ): List<CiExecutionLane> {
        val laneIdByUnit =
            requiredUnits.associate { unit ->
                unit.id to
                    multiAgentLaneId(
                        id = unit.id,
                        availableAgents = availableAgents,
                    )
            }
        val orderedLaneIds = if (availableAgents == 2) TWO_AGENT_LANE_ORDER else MANY_AGENT_LANE_ORDER
        val unitsByLane = requiredUnits.groupBy { unit -> laneIdByUnit.getValue(unit.id) }

        return orderedLaneIds.mapNotNull { laneId ->
            val units = unitsByLane[laneId].orEmpty()
            if (units.isEmpty()) return@mapNotNull null

            val dependencies =
                units
                    .flatMap(VerificationUnit::needs)
                    .mapNotNull(laneIdByUnit::get)
                    .filterNot { dependencyLaneId -> dependencyLaneId == laneId }
                    .distinct()

            lane(
                id = laneId,
                units = units,
                needs = dependencies,
                publishesAuthoritativeStatus = laneId == GATE_LANE,
            )
        }
    }

    private fun multiAgentLaneId(
        id: VerificationUnitId,
        availableAgents: Int,
    ): String =
        if (availableAgents == 2) {
            when (id) {
                VerificationUnitId.GIT_WORKFLOW,
                VerificationUnitId.DOCUMENTATION,
                -> DOCUMENTATION_LANE

                VerificationUnitId.GRADLE_VERIFICATION -> GRADLE_LANE

                VerificationUnitId.PUBLISH_REPORTS -> GATE_LANE

                VerificationUnitId.REPOSITORY_DIFF,
                VerificationUnitId.TEAMCITY_DSL,
                VerificationUnitId.TOOLING,
                VerificationUnitId.BUILD_INFRASTRUCTURE,
                VerificationUnitId.PORTABLE_DISTRIBUTION,
                -> SUPPLEMENTAL_LANE
            }
        } else {
            when (id) {
                VerificationUnitId.GIT_WORKFLOW,
                VerificationUnitId.DOCUMENTATION,
                -> DOCUMENTATION_LANE

                VerificationUnitId.REPOSITORY_DIFF,
                VerificationUnitId.TEAMCITY_DSL,
                -> REPOSITORY_LANE

                VerificationUnitId.TOOLING,
                VerificationUnitId.BUILD_INFRASTRUCTURE,
                VerificationUnitId.PORTABLE_DISTRIBUTION,
                -> TOOLING_LANE

                VerificationUnitId.GRADLE_VERIFICATION -> GRADLE_LANE

                VerificationUnitId.PUBLISH_REPORTS -> GATE_LANE
            }
        }

    private fun lane(
        id: String,
        units: List<VerificationUnit>,
        needs: List<String> = emptyList(),
        publishesAuthoritativeStatus: Boolean,
    ) = CiExecutionLane(
        id = id,
        verificationUnits =
            units.map(
                transform = VerificationUnit::id,
            ),
        needs = needs,
        capabilities = units.flatMap(VerificationUnit::capabilities).distinct(),
        parallelSafe = units.all(VerificationUnit::parallelSafe),
        publishesAuthoritativeStatus = publishesAuthoritativeStatus,
    )

    private companion object {
        const val SCHEMA_VERSION = 1
        const val VERIFY_LANE = "verify"
        const val DOCUMENTATION_LANE = "documentation"
        const val SUPPLEMENTAL_LANE = "supplemental-verification"
        const val REPOSITORY_LANE = "repository-verification"
        const val TOOLING_LANE = "tooling-verification"
        const val GRADLE_LANE = "gradle-verification"
        const val GATE_LANE = "ci-gate"
        val TWO_AGENT_LANE_ORDER =
            listOf(
                DOCUMENTATION_LANE,
                SUPPLEMENTAL_LANE,
                GRADLE_LANE,
                GATE_LANE,
            )
        val MANY_AGENT_LANE_ORDER =
            listOf(
                DOCUMENTATION_LANE,
                REPOSITORY_LANE,
                TOOLING_LANE,
                GRADLE_LANE,
                GATE_LANE,
            )
    }
}
