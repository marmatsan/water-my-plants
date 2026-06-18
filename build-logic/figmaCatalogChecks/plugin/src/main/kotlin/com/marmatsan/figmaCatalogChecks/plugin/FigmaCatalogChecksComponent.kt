package com.marmatsan.figmaCatalogChecks.plugin

import com.marmatsan.figmaCatalogChecks.data.FigmaFileContentClient
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
internal abstract class FigmaCatalogChecksComponent {
    abstract val checker: FigmaVersionsChecker
    abstract val catalogTreeChecker: FigmaCatalogTreeChecker

    @Provides
    protected fun figmaFileContentClient(): FigmaFileContentClient {
        return FigmaFileContentClient()
    }
}
