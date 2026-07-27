# Module water-my-plants-project-config-plugin

Water My Plants composition plugin for reusable repository tooling.

This module is the product composition root. It selects concrete catalog,
Figma, TeamCity, and repository-policy adapters while reusable sibling builds
remain independent from Water My Plants identities and paths.

# Package com.marmatsan.waterMyPlants.projectConfig.catalog

Selects the Water My Plants dependency catalog for reusable consumers.

# Package com.marmatsan.waterMyPlants.projectConfig.gradle

Gradle entry points and focused registrars compose product-owned adapters.

# Package com.marmatsan.waterMyPlants.projectConfig.figma.configuration

Maps Water My Plants Figma identities and targets onto the reusable extension.

# Package com.marmatsan.waterMyPlants.projectConfig.figma.handoff

The Figma handoff capability coordinates canonical TeamCity artifact discovery,
validation, runner inspection, and summary generation through consumer-owned
ports and concrete adapters.

# Package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.adapter

Implements handoff boundaries with filesystem, process, and TeamCity tooling.

# Package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model

Defines the immutable contracts exchanged during a supervised Figma handoff.

# Package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.port

Defines consumer-owned boundaries required by handoff orchestration.

# Package com.marmatsan.waterMyPlants.projectConfig.figma.sync

Coordinates product-specific TeamCity reruns and canonical Figma uploads.

# Package com.marmatsan.waterMyPlants.projectConfig.figma.task

Exposes operational Gradle tasks for handoff, upload, and rerun workflows.

# Package com.marmatsan.waterMyPlants.projectConfig.platform

Detects the host platform selected by product composition.

# Package com.marmatsan.waterMyPlants.projectConfig.teamcity.auth

Provides TeamCity and Cloudflare credential contracts and environment adapters.
