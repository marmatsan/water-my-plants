package com.marmatsan.figmaDocumentationSync.data.ci.configuration

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConfiguration
import java.io.File

/**
 * Project-selected adapter for a generated CI configuration format.
 *
 * Concrete providers live outside the portable data module and translate
 * their CI system into the shared [CiConfiguration] model.
 */
interface CiConfigurationProvider {
    fun read(directory: File): CiConfiguration
}
