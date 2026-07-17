package com.marmatsan.figmaDesignSync.data.datasource.ci

import com.marmatsan.figmaDesignSync.data.yaml.ci.CiWindowsRuntimeYamlReader
import com.marmatsan.figmaDesignSync.domain.model.ci.CiWindowsRuntime
import com.marmatsan.figmaDesignSync.domain.port.ci.CiWindowsRuntimePort
import com.marmatsan.figmaDesignSync.domain.port.ci.CiWindowsRuntimeSource
import me.tatarka.inject.annotations.Inject
import java.io.File

/**
 * YAML-backed adapter for the Windows CI runtime port.
 */
@Inject
class CiWindowsRuntimeDataSource(
    private val reader: CiWindowsRuntimeYamlReader
) : CiWindowsRuntimePort {
    override fun readRuntime(source: CiWindowsRuntimeSource): CiWindowsRuntime =
        reader.read(File(source.filePath))
}
