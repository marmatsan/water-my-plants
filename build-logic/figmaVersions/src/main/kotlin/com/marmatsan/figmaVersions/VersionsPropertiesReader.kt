package com.marmatsan.figmaVersions

import java.io.File
import java.util.Properties

object VersionsPropertiesReader {
    fun read(file: File): Map<String, String> {
        val properties = Properties().apply {
            file.inputStream().use(::load)
        }

        return properties
            .stringPropertyNames()
            .associateWith { key -> properties.getProperty(key).trim() }
            .toSortedMap()
    }
}
