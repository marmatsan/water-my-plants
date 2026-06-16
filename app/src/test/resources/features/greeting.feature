Feature: Greeting

  Scenario: Build a greeting message
    Given a person named "World"
    When the greeting is requested
    Then the greeting message should be "Hello World!"
