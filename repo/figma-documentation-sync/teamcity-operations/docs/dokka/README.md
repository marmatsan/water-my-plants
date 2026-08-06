# Module figmaDocumentationSync-teamcity-operations

Optional supervised TeamCity operations for Figma documentation sync.

This module owns canonical artifact handoff, verified PNG upload, idempotent
pipeline rerun, and the Gradle adapter that exposes those operations. Consumer
composition supplies repository identities through the extension.

# Package com.marmatsan.figmaDocumentationSync.teamcity.operations.auth

Provides TeamCity and Cloudflare credential contracts and environment adapters.

# Package com.marmatsan.figmaDocumentationSync.teamcity.operations.gradle

Exposes the optional Gradle plugin, consumer configuration, and focused task
registration.

# Package com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff

Coordinates canonical TeamCity artifact discovery, validation, runner
inspection, and summary generation.

# Package com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.adapter

Implements handoff boundaries with filesystem, process, and TeamCity tooling.

# Package com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.model

Defines immutable contracts exchanged during a supervised Figma handoff.

# Package com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.port

Defines consumer-owned boundaries required by handoff orchestration.

# Package com.marmatsan.figmaDocumentationSync.teamcity.operations.sync

Coordinates TeamCity reruns and canonical Figma payload uploads.

# Package com.marmatsan.figmaDocumentationSync.teamcity.operations.task

Exposes Gradle tasks for handoff, upload, and rerun workflows.
