Feature: Figma design model generation

  The generated design model is the reviewed repository snapshot used by the
  Figma sync runtime and by the trunk verification task.

  Background:
    Given repository model sources contain versions, catalogs, modules, and module dependencies

  Scenario: Generate the dependency design model
    When the design model is generated at "2026-06-19T10:15:30Z"
    Then the generated model contains versions, version sections, catalogs, modules, and module dependencies
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
