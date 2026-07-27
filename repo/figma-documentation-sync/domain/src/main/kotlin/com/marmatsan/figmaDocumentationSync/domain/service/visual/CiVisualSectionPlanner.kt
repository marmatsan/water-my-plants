package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan

internal fun interface CiVisualSectionPlanner {
    fun create(
        context: CiVisualPlanningContext,
    ): CiVisualPlan.Section
}
