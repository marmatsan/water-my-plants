package com.marmatsan.figmaDesignSync.domain.model.impact

/** Amount of repository verification required before evaluating Figma sync. */
enum class FigmaVerificationScope(val wireValue: String) {
    DOCUMENTATION_ONLY("documentation-only"),
    TRANSPORT_ONLY("transport-only"),
    MODEL_NEUTRAL("model-neutral"),
    FULL_VERIFICATION("full-verification")
}
