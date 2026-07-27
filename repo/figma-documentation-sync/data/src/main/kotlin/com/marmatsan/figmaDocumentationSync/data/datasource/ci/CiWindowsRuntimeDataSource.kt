package com.marmatsan.figmaDocumentationSync.data.datasource.ci

import com.marmatsan.figmaDocumentationSync.data.yaml.ci.CiWindowsRuntimeYamlReader
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiWindowsRuntime
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiWindowsRuntimePort
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiWindowsRuntimeSource
import me.tatarka.inject.annotations.Inject
import java.io.File

/**
 * YAML-backed adapter for the Windows CI runtime port.
 */
@Inject
class CiWindowsRuntimeDataSource(
    private val reader: CiWindowsRuntimeYamlReader,
) : CiWindowsRuntimePort {
    /** Reads the repository-owned Windows runtime YAML selected by [source]. */
    override fun readRuntime(
        source: CiWindowsRuntimeSource,
    ): CiWindowsRuntime =
        reader.read(File(source.filePath))
}
