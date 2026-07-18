package com.marmatsan.figmaDesignSync.domain.model.writer

/** Stability contract applied when a catalog target is absent from a generated model. */
enum class FigmaCatalogTreeTargetLifecycle(
    val wireValue: String
) {
    STABLE_DOCUMENTATION_TARGET("stableDocumentationTarget"),
    DECLARED_CATALOG_TARGET("declaredCatalogTarget")
}
