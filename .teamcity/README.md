# TeamCity Versioned Settings

This directory is the source of truth for TeamCity project settings.

## Branching Workflow

Change `.teamcity/settings.kts` on the short-lived branch:

```text
chore/teamcity-settings
```

Create or recreate the branch from the latest `main`, keep the change focused on
TeamCity settings, open a pull request back to `main`, and delete the branch
after the merge.

Do not push TeamCity settings changes directly to `main`.

## Versioned Settings

TeamCity loads the stable project configuration from the default branch of the
versioned settings VCS root. For this repository, that branch is `main`.

Branch builds can use branch-local settings when the project is configured with:

```text
When build starts: use settings from VCS
```

That option allows a build in a feature or chore branch to run with the
`.teamcity/settings.kts` from that branch. It does not mean the TeamCity project
UI immediately switches to that branch configuration. The stable project
configuration shown in the UI is updated after the settings are merged to
`main` and TeamCity reloads versioned settings.

`Load project settings from VCS` is a manual resync tool. It should not be
required for every change once polling or webhooks detect new commits, but it is
useful while bootstrapping the setup or recovering from stopped synchronization.

## UI Editing

Project editing through the TeamCity UI is disabled because settings are stored
in VCS.

TeamCity Pipelines defined in Kotlin DSL are also read-only in the UI. If
TeamCity reports:

```text
pipelines cannot be edited in UI, edit Kotlin DSL files instead
```

the fix belongs in `.teamcity/settings.kts`, not in the pipeline editor.

## Pipeline Definitions

The TeamCity pipelines are defined with TeamCity Pipelines Kotlin DSL.

### CI

`CI` is the pull request and branch validation pipeline. It is the only TeamCity
status required by GitHub branch protection.

The pipeline:

- monitors all branches;
- runs `Verify`;
- runs `Generate design model` after `Verify`;
- publishes `build/reports/figma-sync/design-model.json`;
- publishes the final GitHub status check from the final job.

`CI` does not run `checkFigmaTrunkSync`. Figma represents the stable `main`
state, so short-lived branch builds should prove that the model can be generated
without requiring Figma to already match that temporary branch.

### Figma Sync

`Figma Sync` is the post-merge Figma verification pipeline. It is scoped to the
default branch and should not be required by GitHub before merging pull
requests.

The pipeline:

- triggers only for `<default>`;
- generates `build/reports/figma-sync/design-model.json` from `main`;
- publishes the generated model as an artifact;
- runs `Check Figma trunk sync` against the metadata currently stored in Figma.

The visual write step is still MCP-operated outside TeamCity. Until that write
step is automated, `Figma Sync` is expected to fail after a model-affecting
merge if Figma has not been synchronized yet. That failure is a post-merge
documentation signal, not a pull request merge gate. After the MCP sync writes
the latest metadata, rerun `Figma Sync` on `main` to verify the result.

Keep job reuse disabled:

```kotlin
allowReuse = false
```

This prevents TeamCity from satisfying a branch or pull request pipeline with a
previously successful job from another branch. The generated design model must
belong to the same branch revision as the pipeline chain being validated.

## Repository Checkout

Declare the project VCS root in `.teamcity/settings.kts` with a local DSL object
and use that object everywhere inside the DSL:

```kotlin
object GitHub : VcsRoot({
    id("GitHub")
    name = "water-my-plants"
    type = "jetbrains.git"

    param("url", "https://github.com/marmatsan/water-my-plants.git")
    param("branch", "refs/heads/main")
    param(
        "branchSpec",
        """
        #! fallbackToDefault: false
        +:refs/heads/(*)
        +:refs/pull/(*/head)
        """.trimIndent()
    )
})
```

Reference the same object from the pipeline:

```kotlin
repositories {
    repository(GitHub, enabledByDefault = true)
}
```

Also declare the repository explicitly in each job:

```kotlin
repositories {
    repository(GitHub)
}
```

The job-level repository block is intentional. It makes TeamCity perform a
native checkout for every virtual pipeline job before the Gradle command runs.

The generated Pipeline YAML should contain a native job repository entry:

```yaml
repositories:
- <generated-github-vcs-root-id>:
    enabled: true
    path: ""
```

The generated script content should remain a direct Gradle call:

```yaml
script-content: .\gradlew.bat check
```

Do not perform `git init`, `git fetch`, or `git checkout` from build script
content. Repository checkout belongs in the Pipeline DSL through job
repositories.

Do not reference TeamCity-generated project-prefixed VCS root ids directly from
job repository blocks. Use the local DSL object, such as `GitHub`. Generated ids
are implementation details and can make pipeline generation fail if they are not
available in the current generation context.

Do not create a second Git VCS root with the same URL. Duplicate roots can make
the pipeline UI show one branch while individual jobs checkout another.

Do not use `DslContext.settingsRoot` as the job checkout repository. It is the
settings root, and TeamCity can apply settings-path checkout rules such as
`.teamcity`. Jobs need the full repository to run `gradlew.bat`.

## GitHub Status Publishing

GitHub status publishing is configured in DSL because Pipeline editing is
read-only when versioned settings are enabled.

The Pipeline DSL exposes `Job.features` and accepts build features that
implement `PipelineCompatible`. This project declares a small generic feature
wrapper and attaches it to the final pipeline job:

```kotlin
features {
    feature(GitHubStatusPublisher("TeamCity CI"))
}
```

That emits `commit-status-publisher` into the generated Pipeline YAML:

```yaml
features:
- type: commit-status-publisher
  build_custom_name: TeamCity CI
```

The feature uses GitHub with VCS root credentials:

```kotlin
param("publisherId", "githubStatusPublisher")
param("github_host", "https://api.github.com")
param("github_authentication_type", "vcsRoot")
param("build_custom_name", statusCheckName)
```

GitHub branch protection should require only the `TeamCity CI` status check.
The status is published by the final `CI` pipeline job, which depends on the
earlier job, so it represents the pull request validation chain.

Do not require the `Figma Sync` pipeline in GitHub branch protection. That
pipeline runs after changes reach `main`.

Do not add a raw GitHub token to the repository. The VCS root credentials or a
TeamCity-managed GitHub App token must provide permission to write commit
statuses.

The feature intentionally omits `vcsRootId`. TeamCity's Commit Status Publisher
then publishes for the Git VCS roots attached to the virtual job. If GitHub
receives no statuses, inspect `teamcity-commit-status.log`.

## Secure Parameters

Secrets are declared in DSL only by TeamCity credential references, never by raw
secret values.

Use a TeamCity-generated secure value:

```kotlin
password("figma.file.content.access.token", "<teamcity-secure-token-reference>")
```

Map the secure project parameter to the build environment:

```kotlin
param("env.FIGMA_FILE_CONTENT_ACCESS_TOKEN", "%figma.file.content.access.token%")
```

## Local Validation

Validate TeamCity settings before pushing:

```powershell
.\mvnw.cmd -f .teamcity\pom.xml teamcity-configs:generate
```

Generated files are written to:

```text
.teamcity/target/generated-configs
```

The generated directory is ignored by Git, but it is useful for checking what
XML and YAML TeamCity will receive.

For this project, the generated pipeline should:

- reference only one Git VCS root for the GitHub repository;
- emit job-level `repositories` entries;
- emit direct Gradle script content;
- emit `commit-status-publisher` only on the final `CI` job;
- keep `Figma Sync` as a separate default-branch pipeline.

## TeamCity CLI

Use the TeamCity CLI for local diagnostics and repeatable pipeline runs. It does
not replace `.teamcity/settings.kts`; it is a client for validating settings,
querying TeamCity, and starting builds.

Keep `teamcity.toml` out of Git. It points to a developer-local TeamCity server
and is ignored by `.gitignore`.

Authenticate the CLI with a TeamCity access token:

```powershell
teamcity auth login --server <teamcity-url> --token <token>
teamcity auth status
```

Bind the current checkout to the TeamCity project and default pipeline if the
local `teamcity.toml` is missing:

```powershell
teamcity link --server <teamcity-url> --project <project-id> --job <pipeline-id> --scope=
```

Validate versioned settings from the CLI when `mvn` is available on `PATH`:

```powershell
teamcity project settings validate .teamcity --verbose
```

On Windows with TeamCity CLI, the CLI may not use the Maven Wrapper from the
repository root for a `.teamcity` DSL directory. Keep a single Maven Wrapper at
the repository root and expose the downloaded Maven distribution on `PATH` if
CLI validation requires `mvn`:

```powershell
$mavenBin = Get-ChildItem "$env:USERPROFILE\.m2\wrapper\dists" -Recurse -Filter mvn.cmd | Select-Object -First 1 -ExpandProperty DirectoryName
$env:PATH = "$mavenBin;$env:PATH"
teamcity project settings validate .teamcity --verbose
```

Check versioned settings synchronization:

```powershell
teamcity project settings status <project-id>
```

Run the linked CI pipeline for the current Git branch:

```powershell
teamcity run start --branch '@this' --watch
```

Quote `@this` in PowerShell. Without quotes, PowerShell can treat `@...` as
syntax instead of passing the literal value to the CLI.

Useful diagnostics after a run:

```powershell
teamcity run tree <run-id>
teamcity run view <run-id>
teamcity run changes <run-id>
teamcity run log <job-run-id> --tail 120 --raw
teamcity run log <job-run-id> --failed --raw
teamcity run artifacts <job-run-id>
```

The top-level pipeline run is composite. Use `teamcity run tree <run-id>` to get
the child job run ids, then inspect the failing child job log.

Do not rely on `--local-changes` unless the access token has the TeamCity
permission `Change build source code with a custom patch`. The normal workflow
is to commit and push the branch, then run the pipeline against that branch.

## Troubleshooting

If a job cannot find `gradlew.bat`, check that the job declares the GitHub
repository in its `repositories` block and that generated YAML contains a native
repository entry for that job.

If a branch build appears to run against `main`, check the child job log. The
checkout log should mention the selected branch and revision. If it shows
`refs/heads/main` for a non-main branch build, TeamCity is using the wrong VCS
root or checkout configuration.

If TeamCity reports `No compatible agent` with unresolved parameters, inspect
the job script content for `%...%` references that are not TeamCity parameters.
For Windows batch variables, use delayed expansion (`!VARIABLE!`) or avoid local
batch variables in the TeamCity step.

If pipeline generation reports that a repository is not found, check for direct
references to generated VCS root ids in job repository blocks. Use the local DSL
object instead.

If GitHub receives no status check, inspect `teamcity-commit-status.log` and
confirm that the status publisher feature is attached to the final job.

If `Figma Sync` fails on `main`, check whether the Figma MCP visual sync has
been run with the latest `design-model.json`. If the failure mentions a branch
other than the selected branch, fix repository checkout before investigating
Figma sync.

## Clean-up Rules

Clean-up rules are also project settings and must be changed in
`.teamcity/settings.kts`.

The project cleanup policy is:

```kotlin
cleanup {
    baseRule {
        artifacts(days = 7)
        history(days = 14)
        all(days = 30)
        preventDependencyCleanup = false
    }
}
```

The project also sets:

```kotlin
param("teamcity.activeBuildBranch.age.hours", "0")
```

This prevents closed branches with old builds from staying visible as active
branches for the default 24-hour window.
