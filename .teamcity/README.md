# TeamCity Versioned Settings

This directory is the source of truth for TeamCity project settings.

## Branching Workflow

Every change to `.teamcity/settings.kts` must be made on the short-lived branch:

```text
chore/teamcity-settings
```

Create or recreate the branch from the latest `main`, keep the change focused on TeamCity settings, open a pull request back to `main`, and delete the branch after the merge.

Do not push TeamCity settings changes directly to `main`.

## How TeamCity Processes These Settings

TeamCity loads the current project configuration from the default branch of the versioned settings VCS root. For this repository, that branch is `main`.

Branch builds can use branch-local settings when the project is configured with:

```text
When build starts: use settings from VCS
```

That setting allows a build in a feature or chore branch to run with the `.teamcity/settings.kts` from that branch. It does not mean the TeamCity project UI immediately switches to that branch configuration. The stable project configuration shown in the UI is updated after the settings are merged to `main` and TeamCity reloads versioned settings.

`Load project settings from VCS` is a manual resync tool. It should not be required for every change once polling or webhooks detect new commits, but it is useful while bootstrapping the setup or recovering from a stopped synchronization.

## UI Editing

Project editing through the TeamCity UI is disabled because settings are stored in VCS.

TeamCity Pipelines defined in Kotlin DSL are also read-only in the UI. If TeamCity reports:

```text
pipelines cannot be edited in UI, edit Kotlin DSL files instead
```

the fix belongs in `.teamcity/settings.kts`, not in the pipeline editor.

## Pipeline Repository

Declare the project VCS root in `.teamcity/settings.kts` with the local id
`GitHub`:

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

Then reference that same object once from the pipeline and keep it enabled by
default:

```kotlin
repositories {
    repository(GitHub, enabledByDefault = true)
}
```

The project settings and source code live in the same GitHub repository. In
TeamCity, the local DSL id `GitHub` is materialized under the project id as
`WaterMyPlants_GitHub`, which is the root that the Pipeline UI resolves to the
selected branch.

This is the repository that TeamCity should automatically checkout for every
job.

In TeamCity 2026.1.2, the virtual jobs generated from this Kotlin DSL pipeline
did not materialize that default checkout in our setup: job logs showed a fresh
checkout directory followed immediately by the script step, and `gradlew.bat`
was missing. Until the native checkout behavior is reliable, each Gradle step
uses a shared script helper that fetches the selected TeamCity branch:

```kotlin
setlocal EnableExtensions EnableDelayedExpansion
set "WMP_BRANCH=%teamcity.build.branch%"
if "!WMP_BRANCH!"=="<default>" set "WMP_BRANCH=main"
git fetch --depth=1 origin "+refs/heads/*:refs/remotes/origin/*" "+refs/pull/*/head:refs/remotes/origin/pull/*"
git checkout --force -B "!WMP_BRANCH!" "origin/!WMP_BRANCH!" || git checkout --force "origin/pull/!WMP_BRANCH!"
```

Do not use `%build.vcs.number.WaterMyPlants_GitHub%` inside job script content.
The virtual jobs do not have that VCS root attached, so TeamCity treats the
parameter as unresolved during agent compatibility checks and reports `No
compatible agent`.

For local Windows batch variables, use delayed expansion (`!WMP_BRANCH!`) rather
than `%WMP_BRANCH%`. TeamCity treats `%...%` as a TeamCity parameter reference
before the job starts, so `%WMP_BRANCH%` also makes the job incompatible.

Avoid job-level repository blocks unless a job needs a different checkout
layout. TeamCity can generate pipeline YAML that references `WaterMyPlants_GitHub`
from a job without making that repository available to the pipeline generator,
which fails at runtime with:

```text
Repository referenced by WaterMyPlants_GitHub not found
```

Do not create a second Git VCS root with the same URL. A duplicate root can make the pipeline show a feature/chore branch while individual jobs still checkout `main`.

Do not use `DslContext.settingsRoot` as the job checkout repository. It is the settings root, and TeamCity can apply settings-path checkout rules such as `.teamcity`; jobs need the full repository to run `gradlew.bat`.

When this is working, the generated Pipeline Head contains:

```xml
<vcs-entry-ref root-id="WaterMyPlants_GitHub" />
```

and the pipeline run log for a branch build reports:

```text
VCS revisions: 'WaterMyPlants_GitHub' ... refs/heads/chore/teamcity-settings
```

If the log reports `WaterMyPlants_WaterMyPlantsRepository` or a revision from `refs/heads/main` while the Pipeline UI shows a chore/feature branch, TeamCity is using the wrong checkout root.

## Pipeline Runs and Job Logs

The top-level Pipeline build is composite. It aggregates job results and may fail with:

```text
Build chain finished (failed: 3)
```

That log does not show the actual Gradle failure. Debug the child jobs instead:

```text
Verify
Generate design model
Check Figma trunk sync
```

The expected Figma gate failure for an unsynchronized branch mentions that branch name:

```text
Figma is out of sync with chore/teamcity-settings
```

If it says `Figma is out of sync with main`, the job checked out `main` and the pipeline checkout configuration is wrong.

## Pipeline Job Reuse

Pipeline jobs must keep reuse disabled:

```kotlin
allowReuse = false
```

This prevents TeamCity from satisfying a branch or pull request pipeline with a previously successful job from `main`. The Figma gate depends on the exact `design-model.json` generated for the same branch revision, so `verify`, `generate_design_model`, and `check_figma_trunk_sync` must all run in the same pipeline chain for the selected branch.

## GitHub Status Publishing

GitHub status publishing is configured in DSL because Pipeline editing is
read-only when versioned settings are enabled.

Each job adds the generic TeamCity build feature:

```kotlin
features {
    feature(GitHubStatusPublisher("TeamCity CI / Verify"))
}
```

The feature emits `commit-status-publisher` into the generated Pipeline YAML
and uses GitHub with VCS root credentials:

```kotlin
param("publisherId", "githubStatusPublisher")
param("github_host", "https://api.github.com")
param("github_authentication_type", "vcsRoot")
param("build_custom_name", statusCheckName)
```

Do not add a raw GitHub token to the repository. The VCS root credentials or a
TeamCity-managed GitHub App token must provide permission to write commit
statuses.

The feature intentionally omits `vcsRootId`. TeamCity's Commit Status Publisher
then publishes for the Git VCS roots attached to the generated job. If GitHub
still receives no statuses, inspect `teamcity-commit-status.log` and switch the
feature to a TeamCity-managed GitHub App token instead of `vcsRoot`.

## Secure Parameters

Secrets are declared in DSL only by TeamCity credential references, never by raw secret values.

Example:

```kotlin
password("figma.file.content.access.token", "credentialsJSON:...")
```

The pipeline maps the secure project parameter to the build environment:

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

The generated directory is ignored by Git, but it is useful for checking what XML/YAML TeamCity will receive. For this project, the pipeline head should reference `WaterMyPlants_GitHub` and should not emit a duplicate VCS root for `https://github.com/marmatsan/water-my-plants.git`.

## TeamCity CLI

Use the TeamCity CLI for local diagnostics and repeatable pipeline runs. It does
not replace `.teamcity/settings.kts`; it is a client for validating settings,
querying TeamCity, and starting builds.

The CLI is configured locally through `teamcity.toml` at the repository root:

```toml
[[server]]
url = 'http://localhost:8111'
project = 'WaterMyPlants'
job = 'WaterMyPlants_WaterMyPlantsCi'
```

Keep `teamcity.toml` out of Git. It points to a developer-local TeamCity server
and is ignored by `.gitignore`.

Authenticate the CLI with a TeamCity access token:

```powershell
teamcity auth login --server http://localhost:8111 --token <token>
teamcity auth status
```

The local TeamCity server currently uses HTTP, so the CLI prints an insecure
connection warning. That is expected for the localhost setup. Do not paste the
token into source files, shell scripts, or docs.

Bind the current checkout to the TeamCity project and default pipeline if the
local `teamcity.toml` is missing:

```powershell
teamcity link --server http://localhost:8111 --project WaterMyPlants --job WaterMyPlants_WaterMyPlantsCi --scope=
```

Validate versioned settings from the CLI when `mvn` is available on `PATH`:

```powershell
teamcity project settings validate .teamcity --verbose
```

On Windows with TeamCity CLI 1.2.1, the CLI does not use the Maven Wrapper from
the repository root for a `.teamcity` DSL directory. Keep a single Maven Wrapper
at the repository root and expose its downloaded Maven 3.9.16 distribution for
CLI validation:

```powershell
$mavenBin = Get-ChildItem "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.9.16-bin" -Recurse -Filter mvn.cmd | Select-Object -First 1 -ExpandProperty DirectoryName
$env:PATH = "$mavenBin;$env:PATH"
teamcity project settings validate .teamcity --verbose
```

Check versioned settings synchronization:

```powershell
teamcity project settings status WaterMyPlants
```

Run the linked CI pipeline for the current Git branch:

```powershell
teamcity run start --branch '@this' --revision '@head' --watch
```

Quote `@this` and `@head` in PowerShell. Without quotes, PowerShell can treat
`@...` as syntax instead of passing the literal value to the CLI.

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
the child job run ids for `Verify`, `Generate design model`, and `Check Figma
trunk sync`, then inspect the failing child job log.

Do not rely on `--local-changes` unless the access token has the TeamCity
permission `Change build source code with a custom patch`. The normal workflow
is to commit and push the branch, then run the pipeline against that branch
revision.

## Clean-up Rules

Clean-up rules are also project settings and must be changed in `.teamcity/settings.kts`.

The current project cleanup policy is:

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

This prevents closed branches with old builds from staying visible as active branches for the default 24-hour window.
