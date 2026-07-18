package com.marmatsan.figmaDesignSync.domain.port.writer

import com.marmatsan.figmaDesignSync.domain.model.writer.VisualSyncPlanBody

/** Canonical hashing boundary for a language-neutral visual sync plan. */
fun interface VisualSyncPlanHasher {
    fun hash(body: VisualSyncPlanBody): String
}
