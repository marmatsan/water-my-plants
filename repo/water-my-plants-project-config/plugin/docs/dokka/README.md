# Module water-my-plants-project-config-plugin

Water My Plants composition plugin for reusable repository tooling.

This module is the product composition root. It selects concrete catalog,
Figma, TeamCity, and repository-policy adapters while reusable sibling builds
remain independent from Water My Plants identities and paths.

# Package com.marmatsan.waterMyPlants.projectConfig.catalog

Selects the Water My Plants dependency catalog for reusable consumers.

# Package com.marmatsan.waterMyPlants.projectConfig.gradle

Gradle entry points compose product-owned adapters and delegate reusable task
registration to their owning capabilities.

# Package com.marmatsan.waterMyPlants.projectConfig.figma.configuration

Maps Water My Plants Figma identities and targets onto the reusable extension.

# Package com.marmatsan.waterMyPlants.projectConfig.teamcity.configuration

Maps Water My Plants TeamCity identities onto the optional reusable operations
extension.
