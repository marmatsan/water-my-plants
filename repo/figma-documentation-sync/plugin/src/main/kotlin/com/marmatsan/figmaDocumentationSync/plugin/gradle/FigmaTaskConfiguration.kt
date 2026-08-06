package com.marmatsan.figmaDocumentationSync.plugin.gradle

import com.marmatsan.figmaDocumentationSync.plugin.task.catalog.CheckFigmaCatalogUsageTask
import com.marmatsan.figmaDocumentationSync.plugin.task.generate.GenerateFigmaDesignModelTask
import com.marmatsan.figmaDocumentationSync.plugin.task.impact.ClassifyFigmaChangeImpactTask
import com.marmatsan.figmaDocumentationSync.plugin.task.input.IncludedBuildTaskInputs
import com.marmatsan.figmaDocumentationSync.plugin.task.sync.CheckFigmaTrunkSyncTask

/** Maps the shared plugin extension onto one design-model generation task. */
internal fun GenerateFigmaDesignModelTask.configureDesignModelInputs(
    context: FigmaPluginContext
) {
    val extension = context.extension
    primaryCatalogModelName.set(extension.primaryCatalogModelName)
    dependencyCatalogProviderClassName.set(extension.dependencyCatalogProviderClassName)
    dependencyCatalogTreesJson.set(extension.dependencyCatalogTreesJson)
    ciDocumentationEnabled.set(extension.ciDocumentationEnabled)
    ciConfigurationModelName.set(extension.ciConfigurationModelName)
    ciConfigurationProviderClassName.set(extension.ciConfigurationProviderClassName)
    ciDefaultBranchAlias.set(extension.ciDefaultBranchAlias)
    versionsFile.set(extension.versionsFile)
    rootSettingsFile.set(extension.rootSettingsFile)
    ciExternalTopologyFile.set(extension.ciExternalTopologyFile)
    ciWindowsRuntimeFile.set(extension.ciWindowsRuntimeFile)
    ciGeneratedConfigurationDirectory.set(extension.ciGeneratedConfigurationDirectory)
    configureIncludedBuildInputs(
        context = context
    )
    projectRootDirectory.set(context.project.layout.projectDirectory)
}

/** Maps the shared plugin extension onto one Figma trunk verification task. */
internal fun CheckFigmaTrunkSyncTask.configureTrunkSyncInputs(
    context: FigmaPluginContext
) {
    val extension = context.extension
    metadataNodeUrl.set(extension.designModelMetadataNodeUrl)
    metadataNamespace.set(extension.metadataNamespace)
    primaryCatalogModelName.set(extension.primaryCatalogModelName)
    dependencyCatalogProviderClassName.set(extension.dependencyCatalogProviderClassName)
    dependencyCatalogTreesJson.set(extension.dependencyCatalogTreesJson)
    ciDocumentationEnabled.set(extension.ciDocumentationEnabled)
    ciConfigurationModelName.set(extension.ciConfigurationModelName)
    ciConfigurationProviderClassName.set(extension.ciConfigurationProviderClassName)
    versionsFile.set(extension.versionsFile)
    rootSettingsFile.set(extension.rootSettingsFile)
    ciExternalTopologyFile.set(extension.ciExternalTopologyFile)
    ciWindowsRuntimeFile.set(extension.ciWindowsRuntimeFile)
    ciGeneratedConfigurationDirectory.set(extension.ciGeneratedConfigurationDirectory)
    configureIncludedBuildInputs(
        context = context
    )
    projectRootDirectory.set(context.project.layout.projectDirectory)
    figmaToken.set(context.project.providers.environmentVariable("FIGMA_FILE_CONTENT_ACCESS_TOKEN"))
}

/** Maps product catalog inputs onto the unused-catalog verification task. */
internal fun CheckFigmaCatalogUsageTask.configureCatalogInputs(
    context: FigmaPluginContext
) {
    val extension = context.extension
    primaryCatalogModelName.set(extension.primaryCatalogModelName)
    dependencyCatalogProviderClassName.set(extension.dependencyCatalogProviderClassName)
    dependencyCatalogTreesJson.set(extension.dependencyCatalogTreesJson)
    rootSettingsFile.set(extension.rootSettingsFile)
    configureIncludedBuildInputs(
        context = context
    )
    projectRootDirectory.set(context.project.layout.projectDirectory)
}

/** Maps the lazily normalized included-build model onto one task input contract. */
private fun IncludedBuildTaskInputs.configureIncludedBuildInputs(
    context: FigmaPluginContext
) {
    includedBuildSettingsFiles.from(
        context.includedBuildSources.map { sources -> sources.map { source -> source.settingsFile } }
    )
    includedBuildSettingsFilePaths.set(
        context.includedBuildSources.map { sources -> sources.map { source -> source.settingsFile.absolutePath } }
    )
    includedBuildRootDirectoryPaths.set(
        context.includedBuildSources.map { sources -> sources.map { source -> source.rootDirectory.absolutePath } }
    )
    includedBuildModelNames.set(
        context.includedBuildSources.map { sources -> sources.map { source -> source.modelName } }
    )
    includedBuildModulePathPrefixes.set(
        context.includedBuildSources.map { sources -> sources.map { source -> source.modulePathPrefix } }
    )
    includedBuildPublishesCatalogs.set(
        context.includedBuildSources.map { sources -> sources.map { source -> source.publishesCatalogs } }
    )
    includedBuildPublishesConventionPlugins.set(
        context.includedBuildSources.map { sources -> sources.map { source -> source.publishesConventionPlugins } }
    )
}

/** Maps repository change-impact inputs onto one classifier task. */
internal fun ClassifyFigmaChangeImpactTask.configureChangeImpactInputs(
    context: FigmaPluginContext
) {
    val project = context.project
    policyFile.set(context.extension.changeImpactPolicyFile)
    projectRootDirectory.set(project.layout.projectDirectory)
    outputFile.set(context.extension.changeImpactFile)
    changedPathsOverride.convention(
        project.providers
            .gradleProperty("figmaChangedPaths")
            .map { value ->
                value
                    .split(',')
                    .map(String::trim)
                    .filter(String::isNotEmpty)
            }.orElse(emptyList())
    )
    project.providers
        .gradleProperty("figmaComparisonBase")
        .orNull
        ?.let(comparisonBaseOverride::set)
    outputs.upToDateWhen { false }
}
