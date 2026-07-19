package com.marmatsan.ci.domain.service

import com.marmatsan.ci.domain.model.CiExecutionLane
import com.marmatsan.ci.domain.model.CiPlan
import com.marmatsan.ci.domain.model.CiTopologyActivation
import com.marmatsan.ci.domain.model.CiTopologyMode
import com.marmatsan.ci.domain.model.RepositoryChangeSet
import com.marmatsan.ci.domain.model.VerificationUnitId
import com.marmatsan.ci.testModuleGraph
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CiTopologyPlannerTest : FunSpec({
    test("one agent keeps every required unit in one authoritative sequential lane") {
        val plan = plan(".teamcity/settings.kts")

        val topology = CiTopologyPlanner().create(plan, availableAgents = 1)

        topology.activation shouldBe CiTopologyActivation.PREVIEW_ONLY
        topology.mode shouldBe CiTopologyMode.SINGLE_AGENT_SEQUENTIAL
        topology.authoritativeStatusPublisherLaneId shouldBe "verify"
        topology.lanes.map(CiExecutionLane::id) shouldBe listOf("verify")
        topology.lanes.single().verificationUnits shouldBe plan.requiredUnitIds()
        topology.lanes.single().publishesAuthoritativeStatus shouldBe true
    }

    test("two agents preview supplemental and Gradle work in parallel after documentation") {
        val topology = CiTopologyPlanner().create(
            plan(".teamcity/settings.kts", "repo/figma-design-sync/tools/package.json"),
            availableAgents = 2
        )

        topology.mode shouldBe CiTopologyMode.MULTI_AGENT_PARALLEL
        topology.lanes.map(CiExecutionLane::id) shouldBe listOf(
            "documentation",
            "supplemental-verification",
            "gradle-verification",
            "ci-gate"
        )
        topology.lane("supplemental-verification").needs shouldBe listOf("documentation")
        topology.lane("gradle-verification").needs shouldBe listOf("documentation")
        topology.lane("ci-gate").needs shouldBe listOf(
            "documentation",
            "supplemental-verification",
            "gradle-verification"
        )
        topology.authoritativeStatusPublisherLaneId shouldBe "ci-gate"
    }

    test("three agents separate repository tooling and Gradle lanes without losing work") {
        val plan = plan(
            ".teamcity/settings.kts",
            "repo/figma-design-sync/tools/package.json",
            "repo/dependency-catalog/versions.properties"
        )

        val topology = CiTopologyPlanner().create(plan, availableAgents = 3)

        topology.lanes.map(CiExecutionLane::id) shouldBe listOf(
            "documentation",
            "repository-verification",
            "tooling-verification",
            "gradle-verification",
            "ci-gate"
        )
        topology.lane("repository-verification").verificationUnits shouldBe
            listOf(VerificationUnitId.TEAMCITY_DSL)
        topology.lane("tooling-verification").verificationUnits shouldBe listOf(
            VerificationUnitId.FIGMA_TOOLING,
            VerificationUnitId.DEPENDENCY_CATALOG
        )
        topology.lanes.flatMap(CiExecutionLane::verificationUnits) shouldBe plan.requiredUnitIds()
        topology.lanes.count(CiExecutionLane::publishesAuthoritativeStatus) shouldBe 1
    }

    test("empty future lanes are omitted for documentation-only work") {
        val topology = CiTopologyPlanner().create(plan("docs/README.md"), availableAgents = 3)

        topology.lanes.map(CiExecutionLane::id) shouldBe listOf(
            "documentation",
            "repository-verification",
            "ci-gate"
        )
        topology.lane("repository-verification").verificationUnits shouldBe
            listOf(VerificationUnitId.REPOSITORY_DIFF)
    }

    test("zero available agents is rejected") {
        shouldThrow<IllegalArgumentException> {
            CiTopologyPlanner().create(plan("docs/README.md"), availableAgents = 0)
        }.message shouldBe "CI topology requires at least one available agent."
    }
}) {
    companion object {
        private fun plan(vararg paths: String): CiPlan = CiPlanFactory().create(
            changeSet = RepositoryChangeSet(
                comparisonBase = "base-sha",
                head = "head-sha",
                changedFiles = paths.toList()
            ),
            moduleGraph = testModuleGraph()
        )

        private fun CiPlan.requiredUnitIds() =
            verificationUnits.filter { unit -> unit.required }.map { unit -> unit.id }

        private fun com.marmatsan.ci.domain.model.CiExecutionTopology.lane(id: String) =
            lanes.single { lane -> lane.id == id }
    }
}
