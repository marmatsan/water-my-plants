package com.marmatsan.figmaDocumentationSync.plugin.task.canonical

import com.marmatsan.figmaDocumentationSync.plugin.task.sync.CheckFigmaTrunkSyncTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.work.DisableCachingByDefault

/** Applies validated canonical-scope gating to the portable Figma trunk check. */
@DisableCachingByDefault(
    because = "The check reads Figma, Git, a secret token, and current-time runtime state"
)
abstract class CheckCanonicalFigmaTrunkSyncTask : CheckFigmaTrunkSyncTask() {
    /** Validated scope wire value that decides whether Figma metadata must be checked. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val verifiedScopeFile: RegularFileProperty

    init {
        onlyIf("Validated Figma scope requires full verification") {
            CanonicalFigmaFullVerificationResolver.fromVerifiedScope(verifiedScopeFile.get().asFile)
        }
    }
}
