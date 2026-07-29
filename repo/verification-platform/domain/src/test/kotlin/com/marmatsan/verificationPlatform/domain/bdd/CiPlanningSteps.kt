package com.marmatsan.verificationPlatform.domain.bdd

import com.marmatsan.verificationPlatform.domain.model.ci.CiExecutionTopology
import com.marmatsan.verificationPlatform.domain.model.ci.CiPlan
import com.marmatsan.verificationPlatform.domain.model.ci.CiScope
import com.marmatsan.verificationPlatform.domain.model.ci.CiTopologyMode
import com.marmatsan.verificationPlatform.domain.model.ci.VerificationUnit
import com.marmatsan.verificationPlatform.domain.model.git.RepositoryChangeSet
import com.marmatsan.verificationPlatform.domain.service.ci.CiPlanFactory
import com.marmatsan.verificationPlatform.domain.service.ci.CiTopologyPlanner
import com.marmatsan.verificationPlatform.testCiPlanPolicy
import com.marmatsan.verificationPlatform.testModuleGraph
import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class CiPlanningSteps : En {
    private var changedFiles = emptyList<String>()
    private lateinit var plan: CiPlan
    private lateinit var topology: CiExecutionTopology

    init {
        Given("repository changes include:") { table: DataTable ->
            changedFiles = table.asList()
        }
        Given("a verification plan for these changes:") { table: DataTable ->
            changedFiles = table.asList()
            plan = createPlan()
        }
        When("the verification plan is created") {
            plan = createPlan()
        }
        When("execution is planned for {int} available agent") { availableAgents: Int ->
            topology =
                CiTopologyPlanner().create(
                    plan,
                    availableAgents
                )
        }
        When("execution is planned for {int} available agents") { availableAgents: Int ->
            topology =
                CiTopologyPlanner().create(
                    plan,
                    availableAgents
                )
        }
        Then("the plan scope is {word}") { expectedScope: String ->
            plan.scope shouldBe CiScope.valueOf(expectedScope)
        }
        Then("full Gradle verification is not required") {
            plan.fullVerification shouldBe false
        }
        Then("full Gradle verification is required") {
            plan.fullVerification shouldBe true
        }
        Then("no Gradle tasks are selected") {
            plan.requiredGradleTasks() shouldBe emptyList()
        }
        Then("the affected modules are:") { table: DataTable ->
            plan.affectedModules shouldContainExactly table.asList()
        }
        Then("the selected Gradle tasks are:") { table: DataTable ->
            plan.requiredGradleTasks() shouldContainExactly table.asList()
        }
        Then("the plan explains that the change has no targeted verification policy") {
            plan.fallbackReason shouldBe "At least one changed path has no targeted verification policy."
        }
        Then("the topology mode is {word}") { expectedMode: String ->
            topology.mode shouldBe CiTopologyMode.valueOf(expectedMode)
        }
        Then("the execution lanes are:") { table: DataTable ->
            topology.lanes.map { lane -> lane.id } shouldContainExactly table.asList()
        }
        Then("every required verification unit is scheduled exactly once") {
            topology.lanes.flatMap { lane -> lane.verificationUnits } shouldContainExactly
                plan.verificationUnits.filter(VerificationUnit::required).map { unit -> unit.id }
        }
        Then("the {string} lane is the only authoritative status publisher") { expectedLaneId: String ->
            topology.authoritativeStatusPublisherLaneId shouldBe expectedLaneId
            topology.lanes.filter { lane -> lane.publishesAuthoritativeStatus }.map { lane -> lane.id } shouldBe
                listOf(expectedLaneId)
        }
    }

    private fun createPlan(): CiPlan =
        CiPlanFactory(testCiPlanPolicy()).create(
            changeSet =
                RepositoryChangeSet(
                    comparisonBase = "base-sha",
                    head = "head-sha",
                    changedFiles = changedFiles
                ),
            moduleGraph = testModuleGraph()
        )
}
