package com.marmatsan.figmaDesignSync.plugin.bdd.steps

import io.cucumber.java.PendingException
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

class FigmaDesignSyncDocumentationSteps {

    @Given("the repository uses build-logic/versions.properties as the project version source")
    fun theRepositoryUsesVersionsPropertiesAsTheProjectVersionSource() = pendingDocumentationStep()

    @Given("the repository uses Gradle settings and build-logic files as catalog sources")
    fun theRepositoryUsesGradleSettingsAndBuildLogicFilesAsCatalogSources() = pendingDocumentationStep()

    @Given("Figma stores sync metadata in the water_my_plants_sync namespace")
    fun figmaStoresSyncMetadataInTheWaterMyPlantsSyncNamespace() = pendingDocumentationStep()

    @Given("repository versions, catalog trees, project modules, and module dependencies are available")
    fun repositoryVersionsCatalogTreesProjectModulesAndModuleDependenciesAreAvailable() =
        pendingDocumentationStep()

    @Given("build/reports/figma-sync/design-model.json was generated from the current branch")
    fun designModelJsonWasGeneratedFromTheCurrentBranch() = pendingDocumentationStep()

    @Given("the Figma Gradle dependencies page contains the expected visual sections")
    fun theFigmaGradleDependenciesPageContainsTheExpectedVisualSections() = pendingDocumentationStep()

    @Given("build/reports/figma-sync/design-model.json contains content that must be rendered")
    fun designModelJsonContainsContentThatMustBeRendered() = pendingDocumentationStep()

    @Given("Figma is missing a required variable, section, instance, or connector template")
    fun figmaIsMissingARequiredVariableSectionInstanceOrConnectorTemplate() = pendingDocumentationStep()

    @Given("Figma exposes shared plugin data for water_my_plants_sync")
    fun figmaExposesSharedPluginDataForWaterMyPlantsSync() = pendingDocumentationStep()

    @Given("a reviewed PlantUML diagram exists under the repository UML convention")
    fun aReviewedPlantUmlDiagramExistsUnderTheRepositoryUmlConvention() = pendingDocumentationStep()

    @When("generateFigmaDesignModel runs")
    fun generateFigmaDesignModelRuns() = pendingDocumentationStep()

    @When("the MCP sync runtime applies the generated model to Figma")
    fun theMcpSyncRuntimeAppliesTheGeneratedModelToFigma() = pendingDocumentationStep()

    @When("checkFigmaTrunkSync compares the generated model with Figma metadata")
    fun checkFigmaTrunkSyncComparesTheGeneratedModelWithFigmaMetadata() = pendingDocumentationStep()

    @When("the diagram is rendered to SVG and published to the UML Figma page")
    fun theDiagramIsRenderedToSvgAndPublishedToTheUmlFigmaPage() = pendingDocumentationStep()

    @Then("it writes build/reports/figma-sync/design-model.json")
    fun itWritesDesignModelJson() = pendingDocumentationStep()

    @Then("the model contains versions, version sections, catalogs, and module dependencies")
    fun theModelContainsVersionsVersionSectionsCatalogsAndModuleDependencies() = pendingDocumentationStep()

    @Then("the model hash ignores generatedAt")
    fun theModelHashIgnoresGeneratedAt() = pendingDocumentationStep()

    @Then("the model hash changes when reviewed model content changes")
    fun theModelHashChangesWhenReviewedModelContentChanges() = pendingDocumentationStep()

    @Then("repository versions are reflected through Figma variables and project version instances")
    fun repositoryVersionsAreReflectedThroughFigmaVariablesAndProjectVersionInstances() =
        pendingDocumentationStep()

    @Then("catalog trees are reflected through tree node instances and connectors")
    fun catalogTreesAreReflectedThroughTreeNodeInstancesAndConnectors() = pendingDocumentationStep()

    @Then("stale synced catalog nodes are removed when they are no longer present in the model")
    fun staleSyncedCatalogNodesAreRemovedWhenTheyAreNoLongerPresentInTheModel() = pendingDocumentationStep()

    @Then("Figma sync metadata is written only after the visual update succeeds")
    fun figmaSyncMetadataIsWrittenOnlyAfterTheVisualUpdateSucceeds() = pendingDocumentationStep()

    @Then("the sync fails without writing new sync metadata")
    fun theSyncFailsWithoutWritingNewSyncMetadata() = pendingDocumentationStep()

    @Then("the failing Figma contract is reported clearly")
    fun theFailingFigmaContractIsReportedClearly() = pendingDocumentationStep()

    @Then("the check passes only when schemaVersion, gitSha, and modelHash match")
    fun theCheckPassesOnlyWhenSchemaVersionGitShaAndModelHashMatch() = pendingDocumentationStep()

    @Then("the check fails when Figma is stale, incomplete, or unreadable")
    fun theCheckFailsWhenFigmaIsStaleIncompleteOrUnreadable() = pendingDocumentationStep()

    @Then("the Figma section is named after the PlantUML file stem")
    fun theFigmaSectionIsNamedAfterThePlantUmlFileStem() = pendingDocumentationStep()

    @Then("the generated SVG is placed in that section")
    fun theGeneratedSvgIsPlacedInThatSection() = pendingDocumentationStep()

    @Then("the section is locked to prevent accidental manual edits")
    fun theSectionIsLockedToPreventAccidentalManualEdits() = pendingDocumentationStep()

    private fun pendingDocumentationStep() {
        throw PendingException(
            "This documentation scenario is tagged @manual. Convert it to an automated step before executing it."
        )
    }
}
