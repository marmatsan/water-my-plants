package com.marmatsan.figmaCatalogChecks.data.properties.versions


import me.tatarka.inject.annotations.Inject
import java.io.File
import java.util.Properties

@Inject
class VersionsPropertiesReader {
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
