package com.marmatsan.figmaDocumentationSync.data.ci.configuration

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConfiguration
import java.io.File

/** No-op adapter for portable plugin fixtures that exercise CI model wiring. */
class EmptyCiConfigurationProvider : CiConfigurationProvider {
    /** Returns an empty configuration without reading [directory]. */
    override fun read(
        directory: File,
    ): CiConfiguration =
        CiConfiguration(
            pipelines = emptyList(),
            vcsRoots = emptyList(),
        )
}
