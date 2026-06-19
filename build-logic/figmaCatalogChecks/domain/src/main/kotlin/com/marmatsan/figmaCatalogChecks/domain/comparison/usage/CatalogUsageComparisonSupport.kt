package com.marmatsan.figmaCatalogChecks.domain.comparison.usage

internal fun compareUsages(
    subject: String,
    expectedModulesByKey: Map<String, List<String>>,
    actualModulesByKey: Map<String, List<String>>
): CatalogUsageComparisonResult {
    val changedValues = expectedModulesByKey
        .mapValues { (_, modules) -> modules.sorted() }
        .mapNotNull { (key, figmaModules) ->
            val repositoryModules = actualModulesByKey[key].orEmpty().sorted()

            if (repositoryModules == figmaModules) {
                null
            } else {
                key to CatalogUsageDifference(
                    repositoryModules = repositoryModules,
                    figmaModules = figmaModules
                )
            }
        }
        .toMap()
        .toSortedMap()

    return CatalogUsageComparisonResult(
        subject = subject,
        changedValues = changedValues
    )
}
