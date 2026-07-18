package com.marmatsan.figmaDesignSync.data.datasource.ci

import com.marmatsan.figmaDesignSync.data.ci.configuration.CiConfigurationProviderFactory
import com.marmatsan.figmaDesignSync.domain.model.ci.CiConfiguration
import com.marmatsan.figmaDesignSync.domain.port.ci.CiConfigurationPort
import com.marmatsan.figmaDesignSync.domain.port.ci.CiGeneratedConfigurationSource
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
