package com.marmatsan.figmaDocumentationSync.domain.model.impact

/** Kind of effect that a repository change can have on Figma documentation. */
enum class FigmaImpact(val wireValue: String) {
    DOCUMENTATION_ONLY("documentation-only"),
    TRANSPORT_ONLY("transport-only"),
    MODEL_NEUTRAL("model-neutral"),
    MODEL_CONTENT("model-content"),
    VISUAL_TARGETS("visual-targets"),
    UNKNOWN("unknown")
}
