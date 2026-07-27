package com.marmatsan.verificationPlatform.domain.service.ci

import com.marmatsan.verificationPlatform.domain.model.ci.CiScope
import com.marmatsan.verificationPlatform.domain.model.ci.VerificationUnitId
import com.marmatsan.verificationPlatform.domain.model.git.RepositoryChangeSet
import com.marmatsan.verificationPlatform.domain.model.modules.ModuleDependency
import com.marmatsan.verificationPlatform.domain.model.modules.RepositoryModuleGraph
import com.marmatsan.verificationPlatform.testCiPlanPolicy
import com.marmatsan.verificationPlatform.testModuleGraph
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class CiPlanFactoryTest :
    FunSpec(
        {
            test("TeamCity changes retain full verification in enforced mode") {
                val plan = plan(".teamcity/settings.kts")

                plan.scope shouldBe CiScope.TEAMCITY
                plan.fullVerification shouldBe true
                plan.requiredUnitIds() shouldContain VerificationUnitId.TEAMCITY_DSL
                plan.gradleTasks() shouldBe
                    listOf(
                        "checkGitWorkflow",
                        "checkDocumentation",
                        "checkTeamCityDsl",
                        "check",
                    )
            }

            test("mixed changes keep all matching units") {
                val plan =
                    plan(
                        "tooling/sync/package.json",
                        "app/build.gradle.kts",
                    )

                plan.scope shouldBe CiScope.MIXED
                plan.requiredUnitIds() shouldContain VerificationUnitId.TOOLING
                plan.requiredUnitIds() shouldContain VerificationUnitId.GRADLE_VERIFICATION
            }

            test("public build-infrastructure changes verify architecture and the staged consumer") {
                val plan =
                    plan(
                        "build-infrastructure/public-api/src/main/kotlin/example/SettingsPlugin.kt",
                    )

                plan.schemaVersion shouldBe 5
                plan.requiredUnitIds() shouldContain VerificationUnitId.BUILD_INFRASTRUCTURE
                plan.requiredUnitIds() shouldContain VerificationUnitId.PORTABLE_DISTRIBUTION
                plan.gradleTasks() shouldContain "checkBuildInfrastructure"
                plan.gradleTasks() shouldContain "checkVersionOwnership"
                plan.gradleTasks() shouldContain "verifyPortableDistribution"
            }

            test("empty change sets fail closed") {
                val plan = plan()

                plan.scope shouldBe CiScope.UNKNOWN
                plan.fullVerification shouldBe true
                plan.fallbackReason shouldBe "No changed files were resolved; verification fails closed."
            }

            test("markdown outside an approved documentation surface is verified as code") {
                val plan = plan("tooling/sync/implementation-notes.md")

                plan.scope shouldBe CiScope.TOOLING
                plan.fullVerification shouldBe true
                plan.requiredUnitIds() shouldContain VerificationUnitId.TOOLING
                plan.requiredUnitIds() shouldContain VerificationUnitId.GRADLE_VERIFICATION
            }

            test("application module changes select only the module and repository-wide catalog usage") {
                val plan = plan("app/src/main/kotlin/com/marmatsan/MainActivity.kt")

                plan.scope shouldBe CiScope.APPLICATION
                plan.changedModules shouldBe listOf(":app")
                plan.affectedModules shouldBe listOf(":app")
                plan.fullVerification shouldBe false
                plan.gradleTasks() shouldBe
                    listOf(
                        "checkGitWorkflow",
                        "checkDocumentation",
                        ":app:check",
                        "checkSharedUsage",
                    )
            }

            test("unresolved module graph dependencies fail closed to root check") {
                val invalidGraph =
                    testModuleGraph().copy(
                        dependencies =
                            testModuleGraph().dependencies +
                                ModuleDependency(
                                    dependentModule = ":app",
                                    dependencyModule = ":missing",
                                ),
                    )
                val plan =
                    plan(
                        "app/src/main/kotlin/com/marmatsan/MainActivity.kt",
                        moduleGraph = invalidGraph,
                    )

                plan.scope shouldBe CiScope.UNKNOWN
                plan.changedModules shouldBe emptyList()
                plan.affectedModules shouldBe emptyList()
                plan.fullVerification shouldBe true
                plan.gradleTasks() shouldBe
                    listOf(
                        "checkGitWorkflow",
                        "checkDocumentation",
                        "check",
                    )
                plan.fallbackReason shouldBe
                    "The Gradle module graph contains an unresolved dependency: :app -> :missing."
            }
        },
    ) {
    companion object {
        private fun plan(
            vararg paths: String,
            moduleGraph: RepositoryModuleGraph = testModuleGraph(),
        ) = CiPlanFactory(testCiPlanPolicy()).create(
            changeSet =
                RepositoryChangeSet(
                    comparisonBase = "base-sha",
                    head = "head-sha",
                    changedFiles = paths.toList(),
                ),
            moduleGraph = moduleGraph,
        )

        private fun com.marmatsan.verificationPlatform.domain.model.ci.CiPlan.requiredUnitIds() =
            verificationUnits.filter { it.required }.map { it.id }

        private fun com.marmatsan.verificationPlatform.domain.model.ci.CiPlan.gradleTasks() =
            requiredGradleTasks()
    }
}
