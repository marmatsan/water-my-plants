package com.marmatsan.ci.domain.model

/** Changed modules, their reverse dependents, and any graph validation failure. */
data class ModuleImpact(
    val changedModules: List<String>,
    val affectedModules: List<String>,
    val fallbackReason: String?
) {
    val isValid: Boolean
        get() = fallbackReason == null
}
