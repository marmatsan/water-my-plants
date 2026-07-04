import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.pipelines.Pipeline
import jetbrains.buildServer.configs.kotlin.pipelines.PipelineCompatible

version = "2026.1"

project {
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
        repository(DslContext.settingsRoot)
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
                scriptContent = ".\\gradlew.bat check"
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
                scriptContent = ".\\gradlew.bat generateFigmaDesignModel"
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

        steps {
            step(PipelineScriptStep {
                name = "Verify Figma sync metadata"
                scriptContent = ".\\gradlew.bat checkFigmaTrunkSync"
            })
        }

        dependency("generate_design_model", listOf("build/reports/figma-sync/design-model.json"))
    }
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
