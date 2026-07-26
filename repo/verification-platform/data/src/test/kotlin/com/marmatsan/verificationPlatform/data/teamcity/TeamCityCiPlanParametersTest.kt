package com.marmatsan.verificationPlatform.data.teamcity

import com.marmatsan.verificationPlatform.domain.model.RepositoryChangeSet
import com.marmatsan.verificationPlatform.domain.model.VerificationUnitId
import com.marmatsan.verificationPlatform.domain.service.CiPlanFactory
import com.marmatsan.verificationPlatform.testCiPlanPolicy
import com.marmatsan.verificationPlatform.testModuleGraph
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TeamCityCiPlanParametersTest :
    FunSpec(
        {
            test("exports allow-listed parameters for a TeamCity change") {
                val plan =
                    CiPlanFactory(testCiPlanPolicy()).create(
                        changeSet =
                            RepositoryChangeSet(
                                comparisonBase = "base-sha",
                                head = "head-sha",
                                changedFiles = listOf(".teamcity/settings.kts"),
                            ),
                        moduleGraph = testModuleGraph(),
                    )

                TeamCityCiPlanParameters().create(plan) shouldBe
                    linkedMapOf(
                        "ci.plan.schemaVersion" to "5",
                        "ci.plan.mode" to "enforced",
                        "ci.plan.scope" to "teamcity",
                        "ci.plan.comparisonBase" to "base-sha",
                        "ci.plan.head" to "head-sha",
                        "ci.plan.fullVerification" to "true",
                        "ci.plan.fallbackReason" to "",
                        "ci.plan.changedModules" to "",
                        "ci.plan.affectedModules" to "",
                        "ci.plan.gradleTasks" to "checkGitWorkflow checkDocumentation checkTeamCityDsl check",
                        "ci.unit.git-workflow.required" to "true",
                        "ci.unit.documentation.required" to "true",
                        "ci.unit.repository-diff.required" to "false",
                        "ci.unit.teamcity-dsl.required" to "true",
                        "ci.unit.tooling.required" to "false",
                        "ci.unit.build-infrastructure.required" to "false",
                        "ci.unit.portable-distribution.required" to "false",
                        "ci.unit.gradle-verification.required" to "true",
                        "ci.unit.publish-reports.required" to "true",
                    )
            }

            test("exports affected module tasks as a validated TeamCity value") {
                val plan =
                    CiPlanFactory(testCiPlanPolicy()).create(
                        changeSet =
                            RepositoryChangeSet(
                                comparisonBase = "base-sha",
                                head = "head-sha",
                                changedFiles = listOf("core/ui/src/main/kotlin/Theme.kt"),
                            ),
                        moduleGraph = testModuleGraph(),
                    )

                val parameters = TeamCityCiPlanParameters().create(plan)

                parameters["ci.plan.changedModules"] shouldBe ":core:ui"
                parameters["ci.plan.affectedModules"] shouldBe ":app,:core:ui,:onboarding:ui"
                parameters["ci.plan.gradleTasks"] shouldBe
                    "checkGitWorkflow checkDocumentation :app:check :core:ui:check " +
                    ":onboarding:ui:check checkSharedUsage"
            }

            test("rejects Gradle task values that could inject shell content") {
                val plan =
                    CiPlanFactory(testCiPlanPolicy()).create(
                        changeSet =
                            RepositoryChangeSet(
                                comparisonBase = "base-sha",
                                head = "head-sha",
                                changedFiles = listOf("app/src/main/kotlin/MainActivity.kt"),
                            ),
                        moduleGraph = testModuleGraph(),
                    )
                val unsafePlan =
                    plan.copy(
                        verificationUnits =
                            plan.verificationUnits.map { unit ->
                                if (unit.id == VerificationUnitId.GRADLE_VERIFICATION) {
                                    unit.copy(
                                        gradleTasks = listOf("check && publish"),
                                    )
                                } else {
                                    unit
                                }
                            },
                    )

                shouldThrow<IllegalArgumentException> {
                    TeamCityCiPlanParameters().create(unsafePlan)
                }
            }
        },
    )
