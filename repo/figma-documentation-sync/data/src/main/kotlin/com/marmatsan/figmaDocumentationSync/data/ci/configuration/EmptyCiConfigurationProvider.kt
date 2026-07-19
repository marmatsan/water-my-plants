package com.marmatsan.figmaDocumentationSync.data.ci.configuration

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConfiguration
import java.io.File

/** No-op adapter for portable plugin fixtures that exercise CI model wiring. */
class EmptyCiConfigurationProvider : CiConfigurationProvider {
    override fun read(directory: File): CiConfiguration =
        CiConfiguration(
            pipelines = emptyList(),
            vcsRoots = emptyList()
        )
}
