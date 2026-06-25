Feature: Figma design model generation

  The generated design model is the reviewed repository snapshot used by the
  Figma sync runtime and by the trunk verification task.

  Background:
    Given repository versions are available
    And repository catalog trees are available
    And repository project modules are available
    And repository module dependencies are available

  Scenario: Generate the dependency design model
    When the design model is generated at "2026-06-19T10:15:30Z"
    Then the generated model contains repository metadata
    And the generated model contains repository versions
    And the generated model contains version sections
    And the generated model contains catalog trees
    And the generated model contains project modules
    And the generated model contains module dependencies
    And the version keys are sorted
    And the version sections keep repository order
    And the model hash is stored in the generated model

  Scenario: generatedAt does not affect the model hash
    When the design model is generated at "2026-06-19T10:15:30Z"
    And the design model is generated again at "2026-06-19T10:16:30Z"
    Then both generated model hashes are equal

  Scenario: gitSha affects the model hash
    When the design model is generated for git sha "abc123"
    And the design model is generated again for git sha "def456"
    Then both generated model hashes are different

  Scenario: generateFigmaDesignModel writes the design model report
    Given a temporary Gradle project exists
    And the temporary Gradle project has repository model files
    And the temporary Gradle project applies the figmaDesignSync plugin
    And the temporary Gradle project is a git repository
    When generateFigmaDesignModel runs in the temporary project
    Then the design model report is written in the temporary project
    And the written design model contains the current branch
    And the written design model contains the current git sha
    And the written design model contains content
    And the written design model contains a model hash
