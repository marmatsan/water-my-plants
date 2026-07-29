package com.marmatsan.figmaDocumentationSync.domain.model.versions

/**
 * Named section from the configured repository versions file.
 *
 * Sections preserve the repository ordering used by the generated Figma
 * documentation while [versions] contains the key/value pairs inside that
 * section.
 *
 * @sample com.marmatsan.figmaDocumentationSync.domain.samples.DomainKDocSamples.repositoryVersionSectionSample
 *
 * @property name section heading from the configured repository versions file.
 * @property versions Version keys and values declared inside the section.
 */
data class RepositoryVersionSection(
    val name: String,
    val versions: Map<String, String>
)
