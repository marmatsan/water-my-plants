package com.marmatsan.verificationPlatform.plugin

import com.marmatsan.verificationPlatform.data.json.CiExecutionTopologyJson
import com.marmatsan.verificationPlatform.data.json.CiPlanJson
import com.marmatsan.verificationPlatform.domain.service.CiTopologyPlanner
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Writes a non-authoritative agent-lane projection of an existing CI plan. */
@DisableCachingByDefault(
    because = "The preview is a diagnostic projection of a Git-derived CI plan",
)
abstract class GenerateCiTopologyPreviewTask : DefaultTask() {
    /** Provider-neutral plan used as the source of required verification units. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val planFile: RegularFileProperty

    /** Compatible build-agent count used to project execution lanes. */
    @get:Input
    abstract val availableAgents: Property<Int>

    /** JSON file receiving the preview-only execution topology. */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /** Reads [planFile], projects its topology, and writes [outputFile]. */
    @TaskAction
    fun generate() {
        val plan = CiPlanJson().read(planFile.get().asFile.readText())
        val topology =
            CiTopologyPlanner().create(
                plan,
                availableAgents.get(),
            )
        val output = outputFile.get().asFile
        CiExecutionTopologyJson().write(
            topology,
            output,
        )

        logger.lifecycle(
            "CI topology preview generated: agents={}, mode={}, lanes={}, output={}",
            topology.availableAgents,
            topology.mode,
            topology.lanes.joinToString { lane -> lane.id },
            output.absolutePath,
        )
    }
}
