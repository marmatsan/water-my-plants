# Module water-my-plants-project-config-plugin

Water My Plants composition plugin for reusable repository tooling.

This module is the product composition root. It selects concrete catalog,
Figma, TeamCity, and repository-policy adapters while reusable sibling builds
remain independent from Water My Plants identities and paths.

# Package com.marmatsan.waterMyPlants.projectConfig

Gradle entry points and focused configurators register the product-owned
composition without moving reusable behavior into the host adapter.

# Package com.marmatsan.waterMyPlants.projectConfig.figma.handoff

The Figma handoff capability coordinates canonical TeamCity artifact discovery,
validation, runner inspection, and summary generation through consumer-owned
ports and concrete adapters.
