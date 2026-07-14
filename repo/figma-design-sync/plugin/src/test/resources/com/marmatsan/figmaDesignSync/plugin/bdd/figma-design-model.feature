@figma-design-model
Feature: Figma design model generation

  The generated design model is the reviewed repository snapshot used by the
  Figma sync runtime and by the trunk verification task.

  Background:
    Given repository versions are available
    And repository catalog trees are available
    And repository project modules are available
    And repository module dependencies are available
    And the external CI topology is available
    And the effective TeamCity configuration is available

  @domain
  Scenario: Generate the dependency design model
    When the design model is generated at 2026-06-19T10:15:30Z
    Then the generated model contains repository metadata
    And the generated model content contains:
      | versions           |
      | versionSections    |
      | catalogs           |
      | modules            |
      | moduleDependencies |
      | ci                 |
    And the version keys are sorted
    And the version sections keep repository order
    And the CI model contains external topology and effective TeamCity configuration
    And the model hash is stored in the generated model

  @domain
  Scenario: generatedAt does not affect the model hash
    When the design model is generated at 2026-06-19T10:15:30Z
    And the design model is generated again at 2026-06-19T10:16:30Z
    Then both generated model hashes are equal

  @domain
  Scenario: gitSha does not affect the model hash
    When the design model is generated for git sha abc123
    And the design model is generated again for git sha def456
    Then both generated model hashes are equal

  @gradle @integration
  Scenario: generateFigmaDesignModel writes the official TeamCity design model report
    Given a temporary Gradle project exists
    And the temporary Gradle project has repository model files
    And the temporary Gradle project applies the figmaDesignSync plugin
    And the temporary Gradle project is a git repository
    And official Figma Sync model generation is authorized
    When generateFigmaDesignModel runs in the temporary project
    Then the design model report is written in the temporary project
    And the written design model contains the current branch
    And the written design model contains the current git sha
    And the written design model contains content
    And the written design model contains repository infrastructure modules
    And the written design model contains a model hash

  @gradle @integration
  Scenario: generateFigmaDesignModel rejects unofficial model generation
    Given a temporary Gradle project exists
    And the temporary Gradle project has repository model files
    And the temporary Gradle project applies the figmaDesignSync plugin
    And the temporary Gradle project is a git repository
    When generateFigmaDesignModel runs without official Figma Sync authorization
    Then generateFigmaDesignModel fails because official Figma Sync generation is required
