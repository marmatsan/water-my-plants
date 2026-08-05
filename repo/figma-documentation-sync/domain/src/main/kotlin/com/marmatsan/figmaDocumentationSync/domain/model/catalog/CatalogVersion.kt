package com.marmatsan.figmaDocumentationSync.domain.model.catalog

/**
 * Domain representation of version metadata that can be rendered in Figma
 * catalog documentation.
 *
 * This is not a Figma component. It is the source value that later becomes part
 * of `design-model.json` and drives whether version text is shown in Figma.
 *
 * [value] contains either a concrete resolved version or the symbolic version
 * property selected by the catalog source. A null value represents a
 * versionless dependency. Versionless entries must stay hidden because there
 * is no version text to render.
 *
 * @sample com.marmatsan.figmaDocumentationSync.domain.samples.DomainKDocSamples.catalogVersionSample
 *
 * @property value Concrete version text, symbolic version property, or null
 * when the catalog entry is versionless.
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
