package com.marmatsan.ci.plugin

import com.marmatsan.ci.data.json.CiPlanJson
import com.marmatsan.ci.data.teamcity.TeamCityCiPlanParameters
import com.marmatsan.ci.data.teamcity.TeamCityServiceMessageFormatter
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "TeamCity service messages must be emitted on every execution")
abstract class PrepareTeamCityCiPlanTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val planFile: RegularFileProperty

    @TaskAction
    fun prepare() {
        val plan = CiPlanJson().read(planFile.get().asFile.readText())
        val parameters = TeamCityCiPlanParameters().create(plan)

        TeamCityServiceMessageFormatter().setParameters(parameters).forEach(logger::lifecycle)

        val requiredUnits = plan.verificationUnits.filter { it.required }.joinToString { it.id.name }
        logger.lifecycle(
            "TeamCity CI plan prepared: scope={}, mode={}, requiredUnits={}",
            plan.scope,
            plan.mode,
            requiredUnits
        )
    }
}
