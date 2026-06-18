package com.marmatsan.figmaCatalogChecks.domain.model.catalog

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
