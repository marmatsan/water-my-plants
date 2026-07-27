@ci-topology
Feature: Plan CI execution for available agents

  The same provider-neutral verification plan remains authoritative whether it
  is executed sequentially today or distributed across future build agents.

  @domain
  Scenario: One agent keeps verification in one sequential lane
    Given a verification plan for these changes:
      | .teamcity/settings.kts |
    When execution is planned for 1 available agent
    Then the topology mode is SINGLE_AGENT_SEQUENTIAL
    And the execution lanes are:
      | verify |
    And every required verification unit is scheduled exactly once
    And the "verify" lane is the only authoritative status publisher

  @domain
  Scenario: Multiple agents separate independent work behind one gate
    Given a verification plan for these changes:
      | .teamcity/settings.kts                                  |
      | tooling/public-api/package.json                         |
      | build-infrastructure/settings.gradle.kts                |
    When execution is planned for 3 available agents
    Then the topology mode is MULTI_AGENT_PARALLEL
    And the execution lanes are:
      | documentation           |
      | repository-verification |
      | tooling-verification    |
      | gradle-verification     |
      | ci-gate                 |
    And every required verification unit is scheduled exactly once
    And the "ci-gate" lane is the only authoritative status publisher
