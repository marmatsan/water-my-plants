@git-workflow
Feature: Enforce the trunk-based Git branch contract

  Repository branches communicate intent and remain compatible with the
  protected trunk, release, and hotfix workflows.

  @domain
  Scenario Outline: Supported repository branches are accepted
    Given the Git branch is "<branch>"
    When the Git branch name is validated
    Then the Git branch is accepted

    Examples:
      | branch                          |
      | main                            |
      | feature/plant-reminders         |
      | fix/watering-date-calculation   |
      | chore/git-branching-strategy    |
      | release/1.4.0                   |
      | hotfix/reminder-crash           |

  @domain
  Scenario Outline: Unsupported repository branches are rejected
    Given the Git branch is "<branch>"
    When the Git branch name is validated
    Then the Git branch is rejected

    Examples:
      | branch                       |
      | develop                      |
      | feature/Plant_Reminders      |
      | feature/plant--reminders     |
      | release/v1.4.0               |
      | personal/marmatsan           |

  @domain
  Scenario: Provider-managed pull request refs do not block required CI
    Given the Git branch is "refs/pull/96/head"
    When the Git branch name is validated
    Then the Git branch is accepted
    And the Git branch is provider-managed
