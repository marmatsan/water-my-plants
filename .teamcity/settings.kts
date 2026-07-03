import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.pipelines.Pipeline
import jetbrains.buildServer.configs.kotlin.pipelines.PipelineCompatible

version = "2026.1"

project {
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
        param("env.FIGMA_FILE_CONTENT_ACCESS_TOKEN", "%figma.file.content.access.token%")
    }

    job {
        id("verify")
        name = "Verify"

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
        const val SCRIPT_CONTENT_PARAM = "script.content"
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
