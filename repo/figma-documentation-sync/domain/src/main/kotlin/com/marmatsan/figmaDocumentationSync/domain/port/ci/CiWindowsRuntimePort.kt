package com.marmatsan.figmaDocumentationSync.domain.port.ci

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiWindowsRuntime

/**
 * Reads the versioned Windows service runtime used by CI documentation.
 */
interface CiWindowsRuntimePort {
    fun readRuntime(
        source: CiWindowsRuntimeSource
    ): CiWindowsRuntime
}
