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
}

/**
 * CI pipeline for the repository.
 *
 * The pipeline runs the Gradle verification tasks, generates the Figma design
 * model, and finishes with the Figma trunk sync gate. Each job declares the
 * [GitHub] repository explicitly so TeamCity performs a native checkout before
 * executing Gradle.
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
        param("env.FIGMA_FILE_CONTENT_ACCESS_TOKEN", "%figma.file.content.access.token%")
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
                name = "Run Gradle check"
                scriptContent = """.\gradlew.bat check"""
            })
        }
    }

    job {
        id("generate_design_model")
        name = "Generate design model"
        allowReuse = false

        repositories {
            repository(GitHub)
        }

        steps {
            step(PipelineScriptStep {
                name = "Generate Figma design model"
                scriptContent = """.\gradlew.bat generateFigmaDesignModel"""
            })
        }

        outputFiles {
            pipelineArtifacts("build/reports/figma-sync/design-model.json")
            sharedWithJobs("build/reports/figma-sync/design-model.json")
        }

        dependency("verify")
    }

    job {
        id("check_figma_trunk_sync")
        name = "Check Figma trunk sync"
        allowReuse = false

        repositories {
            repository(GitHub)
        }

        features {
            feature(GitHubStatusPublisher("TeamCity CI"))
        }

        steps {
            step(PipelineScriptStep {
                name = "Verify Figma sync metadata"
                scriptContent = """.\gradlew.bat checkFigmaTrunkSync"""
            })
        }

        dependency("generate_design_model", listOf("build/reports/figma-sync/design-model.json"))
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
