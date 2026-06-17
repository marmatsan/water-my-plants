package com.marmatsan.figmaVersions.plugin

import com.marmatsan.figmaVersions.FigmaFileContentClient
import com.marmatsan.figmaVersions.FigmaVersionsChecker
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
internal abstract class FigmaVersionsComponent {
    abstract val checker: FigmaVersionsChecker

    @Provides
    protected fun figmaFileContentClient(): FigmaFileContentClient {
        return FigmaFileContentClient()
    }
}
