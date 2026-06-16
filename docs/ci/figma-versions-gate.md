# Figma Versions Gate

## Purpose

Before merging into `main`, CI must verify that the stable Figma file content matches the dependency versions declared in the repository.

## Sources

- Figma file: `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants`
- Figma file key: `YBZXsd8oyGLbcI2KWxJvRK`
- Figma page: `🐘 Gradle dependencies`
- Figma section: `build-logic\versions.properties`
- Figma component instances: `.project version`
- Repository file: `build-logic/versions.properties`

## Comparison Rules

- `repositoryVersions` are the versions read from `build-logic/versions.properties`.
- Compare properties by name and value.
- Read each `.project version` instance using `Version alias` as the property name.
- Read the rendered internal text for the version value, because `Version number` is resolved through the `versions.properties` Figma variable collection mode named `Version number`.
- Ignore comments and blank lines in `build-logic/versions.properties`.
- Ignore declaration order.
- Treat values as exact strings after trimming surrounding whitespace.
- Fail the gate when Figma contains an extra key.
- Fail the gate when the repository contains a key missing from Figma.
- Fail the gate when matching keys have different values.
- Fail the gate when the `🐘 Gradle dependencies` page cannot be found in Figma.
- Fail the gate when the `build-logic\versions.properties` section cannot be found in Figma.
- Fail the gate when more than one `build-logic\versions.properties` section exists in the `Gradle dependencies` page.

## Secrets

The check must read the Figma token from `FIGMA_FILE_CONTENT_ACCESS_TOKEN`.

The token must have read access to the Figma file and the `file_content:read` scope.

## TeamCity Integration

Use a dedicated TeamCity build configuration, or add a dedicated build step to the existing merge-validation configuration, for the `develop` to `main` flow.

The recommended flow is:

- Open a pull request from `develop` into `main`.
- Configure TeamCity's Pull Requests build feature to run for pull requests whose target branch is `main`.
- Add a branch filter so the gate runs only for `main` merge validation, not for every feature branch build.
- Store the Figma token as a TeamCity secure parameter.
- Expose that secure parameter to the Gradle process as `env.FIGMA_FILE_CONTENT_ACCESS_TOKEN`.
- Add a Gradle runner step that executes `checkFigmaVersions`.
- Mark the TeamCity build as a required successful check before allowing the `develop` to `main` merge.

The TeamCity parameter setup should be:

- Secure parameter: `figma.file.content.access.token`
- Environment parameter: `env.FIGMA_FILE_CONTENT_ACCESS_TOKEN=%figma.file.content.access.token%`

The Gradle runner setup should be:

- Gradle wrapper: enabled
- Tasks: `checkFigmaVersions`
- Working directory: repository root

## CI Behavior

TeamCity must run this check for `develop` to `main` merge validation.

The merge into `main` is allowed only when:

- The Figma versions gate passes.
- The normal project build passes.

The check output must include a readable diff of missing keys, extra keys, and changed values.
