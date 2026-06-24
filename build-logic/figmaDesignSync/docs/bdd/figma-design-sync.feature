Feature: Figma design sync

  figmaDesignSync keeps the generated dependency design model and the Figma
  documentation in sync with the repository state that is reviewed in main.

  Background:
    Given the repository uses build-logic/versions.properties as the project version source
    And the repository uses Gradle settings and build-logic files as catalog sources
    And Figma stores sync metadata in the water_my_plants_sync namespace

  Scenario: Generate the dependency design model
    Given repository versions, catalog trees, project modules, and module dependencies are available
    When generateFigmaDesignModel runs
    Then it writes build/reports/figma-sync/design-model.json
    And the model contains versions, version sections, catalogs, and module dependencies
    And the model hash ignores generatedAt
    And the model hash changes when reviewed model content changes

  Scenario: Synchronize the visual Figma model from the generated model
    Given build/reports/figma-sync/design-model.json was generated from the current branch
    And the Figma Gradle dependencies page contains the expected visual sections
    When the MCP sync runtime applies the generated model to Figma
    Then repository versions are reflected through Figma variables and project version instances
    And catalog trees are reflected through tree node instances and connectors
    And stale synced catalog nodes are removed when they are no longer present in the model
    And Figma sync metadata is written only after the visual update succeeds

  Scenario: Reject an incomplete Figma visual model
    Given build/reports/figma-sync/design-model.json contains content that must be rendered
    And Figma is missing a required variable, section, instance, or connector template
    When the MCP sync runtime applies the generated model to Figma
    Then the sync fails without writing new sync metadata
    And the failing Figma contract is reported clearly

  Scenario: Verify that Figma reflects trunk
    Given build/reports/figma-sync/design-model.json was generated from the current branch
    And Figma exposes shared plugin data for water_my_plants_sync
    When checkFigmaTrunkSync compares the generated model with Figma metadata
    Then the check passes only when schemaVersion, gitSha, and modelHash match
    And the check fails when Figma is stale, incomplete, or unreadable

  Scenario: Publish a PlantUML diagram to the UML Figma page
    Given a reviewed PlantUML diagram exists under the repository UML convention
    When the diagram is rendered to SVG and published to the UML Figma page
    Then the Figma section is named after the PlantUML file stem
    And the generated SVG is placed in that section
    And the section is locked to prevent accidental manual edits
