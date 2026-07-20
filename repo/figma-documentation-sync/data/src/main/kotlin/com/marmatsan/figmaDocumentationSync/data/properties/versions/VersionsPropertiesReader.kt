package com.marmatsan.figmaDocumentationSync.data.properties.versions


import com.marmatsan.figmaDocumentationSync.domain.model.versions.RepositoryVersionSection
import me.tatarka.inject.annotations.Inject
import java.io.File
import java.util.Properties

/**
 * Reads the repository version properties file used by gradle plugins and Figma
 * documentation.
 *
 * Java [Properties] is used for the flat view, while [readSections] parses the
 * source file line by line to preserve headings declared with `##`.
 */
@Inject
class VersionsPropertiesReader {
    /**
     * Reads a sorted flat key/value map.
     */
    fun read(
        file: File
    ): Map<String, String> {
        val properties = Properties().apply {
            file.inputStream().use(::load)
        }

        return properties
            .stringPropertyNames()
            .associateWith { key -> properties.getProperty(key).trim() }
            .toSortedMap()
    }

    /**
     * Reads the version file as ordered documentation sections.
     *
     * Lines starting with `## ` open a new section. Regular comments and blank
     * lines are ignored.
     */
    fun readSections(
        file: File
    ): List<RepositoryVersionSection> {
        val sections = linkedMapOf<String, MutableMap<String, String>>()
        var currentSection = DEFAULT_SECTION

        file.forEachLine { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("## ") -> {
                    currentSection = trimmed.removePrefix("##").trim()
                    sections.getOrPut(
                        currentSection,
                        ::linkedMapOf
                    )
                }

                trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!") -> Unit

                "=" in trimmed -> {
                    val separatorIndex = trimmed.indexOf("=")
                    val key = trimmed.substring(
                        0,
                        separatorIndex
                    ).trim()
                    val value = trimmed.substring(separatorIndex + 1).trim()
                    sections.getOrPut(
                        currentSection,
                        ::linkedMapOf
                    )[key] = value
                }
            }
        }

        return sections
            .filterValues(Map<String, String>::isNotEmpty)
            .map { (name, versions) ->
                RepositoryVersionSection(
                    name = name,
                    versions = versions.toSortedMap()
                )
            }
    }

    private companion object {
        const val DEFAULT_SECTION = "Uncategorized"
    }
}
