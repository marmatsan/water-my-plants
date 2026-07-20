@ci-plan
Feature: Select repository verification

  The CI planner selects the smallest safe verification for a committed change
  and falls back to full verification when the change cannot be classified.

  @domain
  Scenario: Documentation-only changes avoid full Gradle verification
    Given repository changes include:
      | docs/standards/testing.md |
      | repo/verification-platform/README.md         |
    When the verification plan is created
    Then the plan scope is DOCUMENTATION_ONLY
    And full Gradle verification is not required
    And the selected Gradle tasks are:
      | checkDocumentation  |
      | checkRepositoryDiff |

  @domain
  Scenario: TeamCity changes use Gradle-owned verification
    Given repository changes include:
      | .teamcity/settings.kts |
    When the verification plan is created
    Then the plan scope is TEAMCITY
    And full Gradle verification is required
    And the selected Gradle tasks are:
      | checkDocumentation |
      | checkTeamCityDsl   |
      | check              |

  @domain
  Scenario: Changes to a shared module verify all reverse dependents
    Given repository changes include:
      | core/ui/src/main/kotlin/com/marmatsan/ui/Theme.kt |
    When the verification plan is created
    Then the affected modules are:
      | :app           |
      | :core:ui       |
      | :onboarding:ui |
    And the selected Gradle tasks are:
      | checkDocumentation     |
      | :app:check             |
      | :core:ui:check         |
      | :onboarding:ui:check   |
      | checkFigmaCatalogUsage |

  @domain
  Scenario: Unknown changes fail closed
    Given repository changes include:
      | automation/unclassified.txt |
    When the verification plan is created
    Then the plan scope is UNKNOWN
    And full Gradle verification is required
    And the selected Gradle tasks are:
      | checkDocumentation |
      | check |
    And the plan explains that the change has no targeted verification policy
