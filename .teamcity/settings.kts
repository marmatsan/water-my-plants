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

        steps {
            step(PipelineScriptStep {
                name = "Run Gradle check"
                scriptContent = GradleScripts.gradle("check")
            })
        }
    }

    job {
        id("generate_design_model")
        name = "Generate design model"
        allowReuse = false

        steps {
            step(PipelineScriptStep {
                name = "Generate Figma design model"
                scriptContent = GradleScripts.gradle("generateFigmaDesignModel")
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

        features {
            feature(GitHubStatusPublisher("TeamCity CI"))
        }

        steps {
            step(PipelineScriptStep {
                name = "Verify Figma sync metadata"
                scriptContent = GradleScripts.gradle("checkFigmaTrunkSync")
            })
        }

        dependency("generate_design_model", listOf("build/reports/figma-sync/design-model.json"))
    }
})

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

object GradleScripts {
    fun gradle(tasks: String): String =
        """
        setlocal EnableExtensions EnableDelayedExpansion
        set "WMP_BRANCH=%teamcity.build.branch%"
        if "!WMP_BRANCH!"=="<default>" set "WMP_BRANCH=main"
        if "!WMP_BRANCH!"=="" set "WMP_BRANCH=main"

        if not exist ".git" git init || exit /b 1
        git remote remove origin 2>NUL
        git remote add origin https://github.com/marmatsan/water-my-plants.git || exit /b 1
        git fetch --depth=1 origin "+refs/heads/*:refs/remotes/origin/*" "+refs/pull/*/head:refs/remotes/origin/pull/*" || exit /b 1
        git checkout --force -B "!WMP_BRANCH!" "origin/!WMP_BRANCH!" || git checkout --force "origin/pull/!WMP_BRANCH!" || exit /b 1

        .\gradlew.bat $tasks
        """.trimIndent()
}

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

open class PipelineScriptStep(init: PipelineScriptStep.() -> Unit = {}) : BuildStep(), PipelineCompatible {
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

open class PipelineVcsTrigger(init: PipelineVcsTrigger.() -> Unit = {}) : Trigger(), PipelineCompatible {
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
