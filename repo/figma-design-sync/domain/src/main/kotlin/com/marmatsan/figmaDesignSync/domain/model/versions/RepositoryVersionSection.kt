package com.marmatsan.figmaDesignSync.domain.model.versions

/**
 * Named section from `repo/dependency-catalog/versions.properties`.
 *
 * Sections preserve the repository ordering used by the generated Figma
 * documentation while [versions] contains the key/value pairs inside that
 * section.
 *
 * @sample com.marmatsan.figmaDesignSync.domain.samples.DomainKDocSamples.repositoryVersionSectionSample
 *
 * @property name Section heading from `repo/dependency-catalog/versions.properties`.
 * @property versions Version keys and values declared inside the section.
 */
data class RepositoryVersionSection(
    val name: String,
    val versions: Map<String, String>
)
