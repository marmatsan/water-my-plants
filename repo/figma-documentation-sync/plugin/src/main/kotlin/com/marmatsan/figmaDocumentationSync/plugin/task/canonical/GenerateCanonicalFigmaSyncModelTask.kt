package com.marmatsan.figmaDocumentationSync.plugin.task.canonical

import com.marmatsan.figmaDocumentationSync.plugin.task.generate.GenerateFigmaDesignModelTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.work.DisableCachingByDefault

/** Applies canonical change-impact gating to portable design-model generation. */
@DisableCachingByDefault(
    because = "Generation records Git, environment, and current-time runtime state"
)
abstract class GenerateCanonicalFigmaSyncModelTask : GenerateFigmaDesignModelTask() {
    /** Classified change-impact artifact that decides whether generation is required. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val changeImpactFile: RegularFileProperty

    init {
        onlyIf("Figma change impact requires full verification") {
            CanonicalFigmaFullVerificationResolver.fromChangeImpact(changeImpactFile.get().asFile)
        }
    }
}
