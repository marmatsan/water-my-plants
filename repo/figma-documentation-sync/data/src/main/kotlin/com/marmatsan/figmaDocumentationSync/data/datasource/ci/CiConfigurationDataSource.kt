package com.marmatsan.figmaDocumentationSync.data.datasource.ci

import com.marmatsan.figmaDocumentationSync.data.ci.configuration.CiConfigurationProviderFactory
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConfiguration
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiConfigurationPort
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiGeneratedConfigurationSource
import java.io.File
import me.tatarka.inject.annotations.Inject

/** Filesystem boundary for the CI provider selected by project-config. */
@Inject
class CiConfigurationDataSource : CiConfigurationPort {
    override fun readConfiguration(source: CiGeneratedConfigurationSource): CiConfiguration =
        CiConfigurationProviderFactory
            .create(source.providerClassName)
            .read(File(source.directoryPath))
}
