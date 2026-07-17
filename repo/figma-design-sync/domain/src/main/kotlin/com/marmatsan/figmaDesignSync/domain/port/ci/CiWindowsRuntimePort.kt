package com.marmatsan.figmaDesignSync.domain.port.ci

import com.marmatsan.figmaDesignSync.domain.model.ci.CiWindowsRuntime

/**
 * Reads the versioned Windows service runtime used by CI documentation.
 */
interface CiWindowsRuntimePort {
    fun readRuntime(source: CiWindowsRuntimeSource): CiWindowsRuntime
}
