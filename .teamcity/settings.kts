import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.pipelines.Pipeline
import jetbrains.buildServer.configs.kotlin.pipelines.PipelineCompatible

version = "2026.1"

project {
    vcsRoot(GitHub)

    params {
        param("android.sdk.path", "C:\\Users\\mmate\\AppData\\Local\\Android\\Sdk")
        param("teamcity.activeBuildBranch.age.hours", "0")
        password("figma.file.content.access.token", "credentialsJSON:56b32d27-92ba-4f95-8a34-f4e24067105a")
    }

    cleanup {
        baseRule {
            artifacts(days = 7)
            history(days = 14)
            all(days = 30)
            preventDependencyCleanup = false
        }
    }

    pipeline(WaterMyPlantsCi)
    pipeline(WaterMyPlantsFigmaSync)
}

/**
 * Pull request and branch CI pipeline for the repository.
 *
 * The pipeline runs Gradle verification tasks for pull requests and branches.
 * It intentionally does not generate or publish the Figma design model because
 * the only authoritative `design-model.json` must come from the post-merge
 * `Figma Sync` pipeline on `main`.
 *
 * The final job publishes the `TeamCity CI` GitHub status required by branch
 * protection. Each job declares the [GitHub] repository explicitly so TeamCity
 * performs a native checkout before executing Gradle.
 */
object WaterMyPlantsCi : Pipeline({
    id("WaterMyPlantsCi")
    name = "CI"

    repositories {
        repository(GitHub, enabledByDefault = true)
    }

    triggers {
        trigger(PipelineVcsTrigger {
            branchFilter = "+:*"
        })
    }

    params {
        param("env.ANDROID_HOME", "%android.sdk.path%")
        param("env.ANDROID_SDK_ROOT", "%android.sdk.path%")
    }

    job {
        id("verify")
        name = "Verify"
        allowReuse = false

        repositories {
            repository(GitHub)
        }

        steps {
            step(PipelineScriptStep {
                name = "Verify change scope"
                scriptContent = """powershell.exe -NoProfile -ExecutionPolicy Bypass -File .teamcity\scripts\invoke-ci-verification.ps1"""
            })
        }

        features {
            feature(GitHubStatusPublisher("TeamCity CI"))
        }
    }
})

/**
 * Post-merge Figma synchronization verification pipeline.
 *
 * This pipeline is scoped to `main` because Figma is derived documentation for
 * the trunk state, not for every short-lived branch. This is the only pipeline
 * allowed to generate and publish the authoritative `design-model.json`.
 * The MCP-operated visual sync still runs outside TeamCity; this pipeline
 * either verifies the metadata after Figma has been updated or fails visibly
 * until the MCP sync is run with the `Generate main design model` artifact and
 * the pipeline is rerun. The final job publishes an optional GitHub status so
 * the post-merge documentation state is visible on `main` commits without
 * becoming a pull request merge gate.
 */
object WaterMyPlantsFigmaSync : Pipeline({
    id("WaterMyPlantsFigmaSync")
    name = "Figma Sync"

    repositories {
        repository(GitHub, enabledByDefault = true)
    }

    triggers {
        trigger(PipelineFinishBuildTrigger {
            buildType = "${WaterMyPlantsCi.id}"
            successfulOnly = true
            branchFilter = "+:<default>"
        })
    }

    params {
        param("env.ANDROID_HOME", "%android.sdk.path%")
        param("env.ANDROID_SDK_ROOT", "%android.sdk.path%")
        param("env.FIGMA_FILE_CONTENT_ACCESS_TOKEN", "%figma.file.content.access.token%")
        param("env.FIGMA_DESIGN_SYNC_OFFICIAL", "true")
        param("env.FIGMA_DESIGN_SYNC_BRANCH", "%teamcity.build.branch%")
    }

    job {
        id("figma_sync_generate_design_model")
        name = "Generate main design model"
        allowReuse = false

        repositories {
            repository(GitHub)
        }

        steps {
            step(PipelineScriptStep {
                name = "Prepare Figma Sync"
                scriptContent = """powershell.exe -NoProfile -ExecutionPolicy Bypass -File .teamcity\scripts\prepare-figma-sync.ps1"""
            })
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
            step(PipelineScriptStep {
                name = "Verify Figma sync metadata"
                scriptContent = """powershell.exe -NoProfile -ExecutionPolicy Bypass -File .teamcity\scripts\verify-figma-trunk-sync.ps1"""
            })
        }

        features {
            feature(GitHubStatusPublisher("TeamCity Figma Sync"))
        }

        dependency(
            "figma_sync_generate_design_model",
            listOf("build/reports/figma-sync", ".teamcity/target/generated-configs")
        )
    }
})

/**
 * Pipeline-compatible Commit Status Publisher feature for GitHub.
 *
 * TeamCity Pipelines Kotlin DSL accepts build features that implement
 * [PipelineCompatible]. This wrapper emits the `commit-status-publisher`
 * feature into the generated Pipeline YAML and publishes the final pipeline
 * status with [statusCheckName].
 *
 * @param statusCheckName GitHub status check name required by branch protection.
 */
class GitHubStatusPublisher(statusCheckName: String) : BuildFeature(), PipelineCompatible {
    init {
        type = "commit-status-publisher"
        yamlType = "commit-status-publisher"

        param("publisherId", "githubStatusPublisher")
        param("github_host", "https://api.github.com")
        param("github_authentication_type", "vcsRoot")
        param("build_custom_name", statusCheckName)
    }
}

/**
 * GitHub repository VCS root used by both versioned settings and pipeline jobs.
 *
 * The branch specification includes regular branches and pull request heads, and
 * disables fallback to the default branch so branch resolution mistakes fail
 * visibly instead of running jobs against `main`.
 */
object GitHub : VcsRoot({
    id("GitHub")
    name = "water-my-plants"
    type = "jetbrains.git"

    param("url", "https://github.com/marmatsan/water-my-plants.git")
    param("branch", "refs/heads/main")
    param(
        "branchSpec",
        """
        #! fallbackToDefault: false
        +:refs/heads/(*)
        +:refs/pull/(*/head)
        """.trimIndent()
    )
})

/**
 * Pipeline-compatible command line script step.
 *
 * The TeamCity Pipelines DSL serializes script steps with the `script-content`
 * YAML property. This wrapper keeps the Kotlin DSL explicit while avoiding raw
 * untyped build step declarations at call sites.
 */
open class PipelineScriptStep(init: PipelineScriptStep.() -> Unit = {}) : BuildStep(), PipelineCompatible {
    /**
     * Command content emitted as `script-content` in generated Pipeline YAML.
     */
    var scriptContent: String
        get() = params.find { it.name == SCRIPT_CONTENT_PARAM }?.value.orEmpty()
        set(value) {
            param(SCRIPT_CONTENT_PARAM, value)
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
open class PipelineVcsTrigger(init: PipelineVcsTrigger.() -> Unit = {}) : Trigger(), PipelineCompatible {
    /**
     * TeamCity branch filter used by the generated VCS trigger.
     */
    var branchFilter: String
        get() = params.find { it.name == BRANCH_FILTER_PARAM }?.value.orEmpty()
        set(value) {
            param(BRANCH_FILTER_PARAM, value)
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
        set(value) {
            param(BUILD_TYPE_PARAM, value)
        }

    /**
     * Whether this trigger should react only to successful watched builds.
     */
    var successfulOnly: Boolean
        get() = params.find { it.name == SUCCESSFUL_ONLY_PARAM }?.value == "true"
        set(value) {
            param(SUCCESSFUL_ONLY_PARAM, if (value) "true" else "")
        }

    /**
     * TeamCity branch filter used to limit watched CI builds.
     */
    var branchFilter: String
        get() = params.find { it.name == BRANCH_FILTER_PARAM }?.value.orEmpty()
        set(value) {
            param(BRANCH_FILTER_PARAM, value)
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
