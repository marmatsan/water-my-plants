package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan

/** Consumer-owned extension point for one independent CI documentation section. */
internal fun interface CiVisualSectionPlanner {
    /** Creates this planner's deterministic section from validated [context]. */
    fun create(
        context: CiVisualPlanningContext,
    ): CiVisualPlan.Section
}
