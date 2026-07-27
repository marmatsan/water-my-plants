package com.marmatsan.figmaDocumentationSync.domain.port.ci

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiWindowsRuntime

/**
 * Reads the versioned Windows service runtime used by CI documentation.
 */
interface CiWindowsRuntimePort {
    /** Reads and validates the versioned Windows runtime at [source]. */
    fun readRuntime(
        source: CiWindowsRuntimeSource,
    ): CiWindowsRuntime
}
