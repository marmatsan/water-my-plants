import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.pipelines.Pipeline
import jetbrains.buildServer.configs.kotlin.pipelines.PipelineCompatible

version = "2026.1"

project {
    vcsRoot(GitHub)

    params {
        param(
            "teamcity.activeBuildBranch.age.hours",
            "0"
        )
        password(
            "figma.file.content.access.token",
            "credentialsJSON:56b32d27-92ba-4f95-8a34-f4e24067105a"
        )
    }

    cleanup {
        baseRule {
            artifacts(
                days = 7
            )
            history(
                days = 14
            )
            all(
                days = 30
            )
            preventDependencyCleanup = false
        }
        keepRule {
            id = "KeepOfficialFigmaArtifacts"
            keepAtLeast = days(30) {
                since = today()
            }
            dataToKeep = artifacts(
                "+:build/reports/figma-sync/**",
                "+:.teamcity/target/generated-configs/**"
            )
            applyToBuilds {
                inPersonalBuilds = nonPersonal()
                inBranches {
                    branchFilter = patterns("+:<default>")
                }
                withStatus = successful()
            }
            preserveArtifactsDependencies = true
        }
    }

    pipeline(WaterMyPlantsCi)
    buildType(WaterMyPlantsCiGate)
    pipeline(WaterMyPlantsFigmaSync)
    pipeline(WaterMyPlantsInfrastructureHealth)
}

/**
 * Pull request and branch CI pipeline for the repository.
 *
 * The pipeline runs Gradle verification tasks for pull requests and branches.
 * It intentionally does not generate or publish the Figma design model because
 * the only authoritative `design-model.json` must come from the post-merge
 * `Figma Sync` pipeline on `main`.
 *
 * [WaterMyPlantsCiGate] owns automatic triggering and publishes the final
 * `TeamCity CI` GitHub status required by branch protection. Each job declares
 * the [GitHub] repository explicitly so TeamCity performs a native checkout
 * before executing Gradle.
 */
object WaterMyPlantsCi : Pipeline(
    {
    id("WaterMyPlantsCi")
    name = "CI"

    repositories {
        repository(
            GitHub,
            enabledByDefault = true
        )
    }

    job {
        id("verify")
        name = "Verify"
        allowReuse = false

        repositories {
            repository(GitHub)
        }

        params {
            param(
                "env.GIT_WORKFLOW_BRANCH",
                "%teamcity.build.branch%"
            )
            param(
                "ci.plan.comparisonBase",
                ""
            )
            param(
                "ci.plan.gradleTasks",
                "check"
            )
        }

        steps {
            step(
                PipelineScriptStep {
                name = "Validate agent capabilities"
                scriptContent = """powershell.exe -NoProfile -ExecutionPolicy Bypass -File .teamcity\scripts\test-agent-capabilities.ps1 -ExportTeamCityParameters"""
            }
            )
            step(
                PipelineScriptStep {
                name = "Generate verification plan"
                scriptContent = """.\gradlew.bat prepareTeamCityCiPlan --stacktrace"""
            }
            )
            step(
                PipelineScriptStep {
                name = "Run planned Gradle checks"
                scriptContent = """
                    @echo off
                    call .\gradlew.bat %ci.plan.gradleTasks% --stacktrace
                    if errorlevel 1 exit /b 1
                """.trimIndent()
            }
            )
        }

        requirements {
            contains(
                "teamcity.agent.jvm.os.name",
                "Windows"
            )
        }

        outputFiles {
            pipelineArtifacts("build/reports/ci")
        }
    }
}
)

/**
 * Versioned GitHub status gate for [WaterMyPlantsCi].
 *
 * TeamCity Pipelines publishes repository statuses through a server-side
 * repository toggle that is not exposed by the current Pipeline Kotlin DSL.
 * This step-less classic composite build keeps the complete contract in
 * versioned settings: its VCS trigger queues a fresh CI pipeline revision, its
 * snapshot dependency aggregates the result, and Commit Status Publisher emits
 * the required `TeamCity CI` status without adding an unsupported Pipeline YAML
 * job feature.
 */
object WaterMyPlantsCiGate : BuildType(
    {
    id("WaterMyPlantsCiGate")
    name = "CI Gate"
    type = BuildTypeSettings.Type.COMPOSITE

    vcs {
        root(GitHub)
    }

    triggers {
        trigger(
            PipelineVcsTrigger {
            branchFilter = "+:*"
        }
        )
    }

    dependencies {
        snapshot(WaterMyPlantsCi) {
            reuseBuilds = ReuseBuilds.NO
        }
    }

    features {
        feature(
            GitHubCommitStatusPublisher(
                statusCheckName = "TeamCity CI"
            )
        )
    }
}
)

/**
 * Post-merge Figma synchronization verification pipeline.
 *
 * This pipeline is scoped to `main` because Figma is derived documentation for
 * the trunk state, not for every short-lived branch. This is the only pipeline
 * allowed to generate and publish the authoritative `design-model.json`.
 * The MCP-operated visual sync still runs outside TeamCity; this pipeline
 * either verifies the metadata after Figma has been updated or fails visibly
 * until the MCP sync is run with the `Generate main design model` artifact and
 * the pipeline is rerun. It remains visible in TeamCity but does not publish a
 * GitHub status. Status publication is deliberately limited to
 * [WaterMyPlantsCiGate] until this pipeline also has a versioned classic gate.
 */
object WaterMyPlantsFigmaSync : Pipeline(
    {
    id("WaterMyPlantsFigmaSync")
    name = "Figma Sync"

    repositories {
        repository(
            GitHub,
            enabledByDefault = true
        )
    }

    triggers {
        trigger(
            PipelineFinishBuildTrigger {
            buildType = "${WaterMyPlantsCiGate.id}"
            successfulOnly = true
            branchFilter = "+:<default>"
        }
        )
    }

    params {
        param(
            "env.FIGMA_FILE_CONTENT_ACCESS_TOKEN",
            "%figma.file.content.access.token%"
        )
        param(
            "env.FIGMA_DOCUMENTATION_SYNC_OFFICIAL",
            "true"
        )
        param(
            "env.FIGMA_DOCUMENTATION_SYNC_BRANCH",
            "%teamcity.build.branch%"
        )
    }

    job {
        id("figma_sync_generate_design_model")
        name = "Generate main design model"
        allowReuse = false

        repositories {
            repository(GitHub)
        }

        steps {
            step(
                PipelineScriptStep {
                name = "Validate agent capabilities"
                scriptContent = """powershell.exe -NoProfile -ExecutionPolicy Bypass -File .teamcity\scripts\test-agent-capabilities.ps1 -RequireNode -ExportTeamCityParameters"""
            }
            )
            step(
                PipelineScriptStep {
                name = "Classify Figma change impact"
                scriptContent = """.\gradlew.bat classifyOfficialFigmaSyncChangeImpact -PfigmaOfficialTeamCityPhasedExecution=true --stacktrace"""
            }
            )
            step(
                PipelineScriptStep {
                name = "Materialize official design model"
                scriptContent = """.\gradlew.bat materializeFigmaSyncCiConfiguration generateOfficialFigmaSyncModel -PfigmaOfficialTeamCityPhasedExecution=true --stacktrace"""
            }
            )
            step(
                PipelineScriptStep {
                name = "Build MCP runners and visual plan"
                scriptContent = """.\gradlew.bat prepareOfficialFigmaSync -PfigmaOfficialTeamCityPhasedExecution=true --stacktrace"""
            }
            )
        }

        requirements {
            contains(
                "teamcity.agent.jvm.os.name",
                "Windows"
            )
        }

        outputFiles {
            pipelineArtifacts("build/reports/figma-sync")
            sharedWithJobs("build/reports/figma-sync")
            pipelineArtifacts(".teamcity/target/generated-configs")
            sharedWithJobs(".teamcity/target/generated-configs")
        }

    }

    job {
        id("figma_sync_check_trunk_sync")
        name = "Check Figma trunk sync"
        allowReuse = false

        repositories {
            repository(GitHub)
        }

        steps {
            step(
                PipelineScriptStep {
                name = "Validate agent capabilities"
                scriptContent = """powershell.exe -NoProfile -ExecutionPolicy Bypass -File .teamcity\scripts\test-agent-capabilities.ps1 -ExportTeamCityParameters"""
            }
            )
            step(
                PipelineScriptStep {
                name = "Validate official sync scope"
                scriptContent = """.\gradlew.bat validateOfficialFigmaSyncScope -PfigmaOfficialTeamCityPhasedExecution=true --stacktrace"""
            }
            )
            step(
                PipelineScriptStep {
                name = "Verify Figma sync metadata"
                scriptContent = """.\gradlew.bat checkOfficialFigmaTrunkSync -PfigmaOfficialTeamCityPhasedExecution=true --stacktrace"""
            }
            )
        }

        requirements {
            contains(
                "teamcity.agent.jvm.os.name",
                "Windows"
            )
        }

        dependency(
            "figma_sync_generate_design_model",
            listOf(
                "build/reports/figma-sync",
                ".teamcity/target/generated-configs"
            )
        )
    }
}
)

/**
 * Scheduled infrastructure health pipeline.
 *
 * This pipeline checks the build-agent toolchain and the observable TeamCity
 * HTTPS boundaries. It is intentionally independent from pull request status
 * publishing so an infrastructure incident is visible without blocking an
 * unrelated source change.
 */
object WaterMyPlantsInfrastructureHealth : Pipeline(
    {
    id("WaterMyPlantsInfrastructureHealth")
    name = "Infrastructure Health"

    repositories {
        repository(
            GitHub,
            enabledByDefault = true
        )
    }

    triggers {
        trigger(
            PipelineDailyScheduleTrigger {
            hour = 6
            minute = 0
            branchFilter = "+:<default>"
        }
        )
    }

    params {
        param(
            "env.TEAMCITY_SERVER_URL",
            "%teamcity.serverUrl%"
        )
    }

    job {
        id("infrastructure_health")
        name = "Check infrastructure health"
        allowReuse = false

        repositories {
            repository(GitHub)
        }

        steps {
            step(
                PipelineScriptStep {
                name = "Validate agent capabilities"
                scriptContent = """powershell.exe -NoProfile -ExecutionPolicy Bypass -File .teamcity\scripts\test-agent-capabilities.ps1 -RequireNode -ExportTeamCityParameters"""
            }
            )
            step(
                PipelineScriptStep {
                name = "Probe TeamCity boundaries"
                scriptContent = """powershell.exe -NoProfile -ExecutionPolicy Bypass -File .teamcity\scripts\test-ci-infrastructure-health.ps1"""
            }
            )
        }

        requirements {
            contains(
                "teamcity.agent.jvm.os.name",
                "Windows"
            )
        }

        outputFiles {
            pipelineArtifacts("build/reports/ci-health")
        }
    }
}
)

/**
 * Classic Commit Status Publisher feature for the repository GitHub root.
 *
 * This feature belongs only to classic [BuildType] instances. Pipeline jobs
 * must not use it because `commit-status-publisher` is not part of the Pipeline
 * YAML job-feature enum.
 */
class GitHubCommitStatusPublisher(
    statusCheckName: String
) : BuildFeature() {
    init {
        type = "commit-status-publisher"
        param(
            "publisherId",
            "githubStatusPublisher"
        )
        param(
            "github_host",
            "https://api.github.com"
        )
        param(
            "github_authentication_type",
            "vcsRoot"
        )
        param(
            "build_custom_name",
            statusCheckName
        )
    }
}

/**
 * GitHub repository VCS root used by both versioned settings and pipeline jobs.
 *
 * The branch specification includes regular branches and pull request heads, and
 * disables fallback to the default branch so branch resolution mistakes fail
 * visibly instead of running jobs against `main`.
 */
object GitHub : VcsRoot(
    {
    id("GitHub")
    name = "water-my-plants"
    type = "jetbrains.git"

    param(
        "url",
        "https://github.com/marmatsan/water-my-plants.git"
    )
    param(
        "branch",
        "refs/heads/main"
    )
    param(
        "branchSpec",
        """
        #! fallbackToDefault: false
        +:refs/heads/(*)
        +:refs/pull/(*/head)
        """.trimIndent()
    )
}
)

/**
 * Pipeline-compatible command line script step.
 *
 * The TeamCity Pipelines DSL serializes script steps with the `script-content`
 * YAML property. This wrapper keeps the Kotlin DSL explicit while avoiding raw
 * untyped build step declarations at call sites.
 */
open class PipelineScriptStep(
    init: PipelineScriptStep.() -> Unit = {}
) : BuildStep(), PipelineCompatible {
    /**
     * Command content emitted as `script-content` in generated Pipeline YAML.
     */
    var scriptContent: String
        get() = params.find { it.name == SCRIPT_CONTENT_PARAM }?.value.orEmpty()
        set(
            value
        ) {
            param(
                SCRIPT_CONTENT_PARAM,
                value
            )
        }

    init {
        type = "simpleRunner"
        yamlType = "script"
        init()
    }

    private companion object {
        const val SCRIPT_CONTENT_PARAM = "script-content"
    }
}

/**
 * Pipeline-compatible VCS trigger.
 *
 * The typed trigger helper used by classic build configurations is not accepted
 * directly by Pipeline DSL blocks, so this wrapper emits the VCS trigger with
 * the branch filter parameter expected by TeamCity Pipelines.
 */
open class PipelineVcsTrigger(
    init: PipelineVcsTrigger.() -> Unit = {}
) : Trigger(), PipelineCompatible {
    /**
     * TeamCity branch filter used by the generated VCS trigger.
     */
    var branchFilter: String
        get() = params.find { it.name == BRANCH_FILTER_PARAM }?.value.orEmpty()
        set(
            value
        ) {
            param(
                BRANCH_FILTER_PARAM,
                value
            )
        }

    init {
        type = "vcsTrigger"
        init()
    }

    private companion object {
        const val BRANCH_FILTER_PARAM = "branchFilter"
    }
}

/**
 * Pipeline-compatible finish build trigger.
 *
 * TeamCity's Finish Build Trigger uses the `buildDependencyTrigger` type. The
 * Figma Sync pipeline uses it to run only after the CI pipeline has completed
 * successfully on the default branch.
 */
open class PipelineFinishBuildTrigger(
    init: PipelineFinishBuildTrigger.() -> Unit = {}
) : Trigger(), PipelineCompatible {
    /**
     * External id of the build configuration or pipeline head to watch.
     */
    var buildType: String
        get() = params.find { it.name == BUILD_TYPE_PARAM }?.value.orEmpty()
        set(
            value
        ) {
            param(
                BUILD_TYPE_PARAM,
                value
            )
        }

    /**
     * Whether this trigger should react only to successful watched builds.
     */
    var successfulOnly: Boolean
        get() = params.find { it.name == SUCCESSFUL_ONLY_PARAM }?.value == "true"
        set(
            value
        ) {
            param(
                SUCCESSFUL_ONLY_PARAM,
                if (value) "true" else ""
            )
        }

    /**
     * TeamCity branch filter used to limit watched CI builds.
     */
    var branchFilter: String
        get() = params.find { it.name == BRANCH_FILTER_PARAM }?.value.orEmpty()
        set(
            value
        ) {
            param(
                BRANCH_FILTER_PARAM,
                value
            )
        }

    init {
        type = "buildDependencyTrigger"
        init()
    }

    private companion object {
        const val BUILD_TYPE_PARAM = "dependsOn"
        const val SUCCESSFUL_ONLY_PARAM = "afterSuccessfulBuildOnly"
        const val BRANCH_FILTER_PARAM = "branchFilter"
    }
}

/**
 * Pipeline-compatible daily schedule trigger.
 *
 * The current Pipeline DSL exposes only the generic trigger boundary. These
 * parameters are the versioned TeamCity scheduling trigger contract for a
 * daily run in the server time zone.
 */
open class PipelineDailyScheduleTrigger(
    init: PipelineDailyScheduleTrigger.() -> Unit = {}
) : Trigger(), PipelineCompatible {
    var hour: Int = 0
        set(
            value
        ) {
            require(value in 0..23) { "Schedule hour must be between 0 and 23." }
            field = value
            param(
                "hour",
                value.toString()
            )
        }

    var minute: Int = 0
        set(
            value
        ) {
            require(value in 0..59) { "Schedule minute must be between 0 and 59." }
            field = value
            param(
                "minute",
                value.toString()
            )
        }

    var branchFilter: String
        get() = params.find { it.name == BRANCH_FILTER_PARAM }?.value.orEmpty()
        set(
            value
        ) {
            param(
                BRANCH_FILTER_PARAM,
                value
            )
        }

    init {
        type = "schedulingTrigger"
        param(
            "schedulingPolicy",
            "daily"
        )
        param(
            "timezone",
            "SERVER"
        )
        param(
            "triggerBuildWithPendingChangesOnly",
            "false"
        )
        init()
    }

    private companion object {
        const val BRANCH_FILTER_PARAM = "branchFilter"
    }
}
