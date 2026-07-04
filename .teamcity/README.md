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
set "WMP_BRANCH=%teamcity.build.branch%"
if "%WMP_BRANCH%"=="<default>" set "WMP_BRANCH=main"
git fetch --depth=1 origin "+refs/heads/*:refs/remotes/origin/*" "+refs/pull/*/head:refs/remotes/origin/pull/*"
git checkout --force "origin/%WMP_BRANCH%" || git checkout --force "origin/pull/%WMP_BRANCH%"
```

Do not use `%build.vcs.number.WaterMyPlants_GitHub%` inside job script content.
The virtual jobs do not have that VCS root attached, so TeamCity treats the
parameter as unresolved during agent compatibility checks and reports `No
compatible agent`.

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
