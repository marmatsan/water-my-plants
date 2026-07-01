package com.marmatsan.figmaDesignSync.domain.model.catalog

/**
 * Domain representation of version metadata that can be rendered in Figma
 * catalog documentation.
 *
 * This is not a Figma component. It is the source value that later becomes part
 * of `design-model.json` and drives whether version text is shown in Figma.
 *
 * A null [value] represents a versionless dependency. Versionless entries must
 * stay hidden because there is no concrete version text to render.
 *
 * @sample com.marmatsan.figmaDesignSync.domain.samples.DomainKDocSamples.catalogVersionSample
 *
 * @property value Concrete version text, or null when the catalog entry is
 * versionless.
 * @property visible Whether the generated Figma documentation should display
 * the version text.
 */
data class CatalogVersion(
    val value: String?,
    val visible: Boolean = value != null
) {
    init {
        require(value != null || !visible) {
            "A catalog version cannot be visible when its value is null"
        }
    }
}
