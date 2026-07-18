# TeamCity Versioned Settings

This directory is the source of truth for TeamCity project settings.

The contract used to derive the Figma representation of CI from these settings
is documented in
[`docs/ci/visual-model-contract.md`](../docs/ci/visual-model-contract.md).
Public HTTPS access, Cloudflare policies, CLI service authentication, webhook
validation, and CSRF recovery are documented in
[`docs/runbooks/teamcity-cloudflare-access.md`](../docs/runbooks/teamcity-cloudflare-access.md).
The Windows services that host this configuration are versioned in
[`docs/ci/windows-runtime.yaml`](../docs/ci/windows-runtime.yaml).

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

The GitHub ruleset for `main` is documented in
[`docs/ci/main-branch-protection.md`](../docs/ci/main-branch-protection.md).

The pipeline:

- monitors all branches;
- validates typed documentation before scope classification: canonical
  placement, frontmatter, review dates, runbook and ADR sections, canonical
  sources, and local Markdown links are checked by
  `.teamcity/scripts/validate-documentation.ps1`;
- validates documentation coverage before Gradle: changes to TeamCity,
  Figma-sync implementation, dependency-catalog model, or CI topology must
  update their mapped canonical documentation in
  [`.teamcity/documentation-coverage.json`](documentation-coverage.json);
- uses `Verify change scope` to run `git diff --check` for documentation-only
  changes after the repository-wide documentation validator; every other change runs
  `.\gradlew.bat check --stacktrace` so Gradle failures retain their diagnostic
  context in the TeamCity build log;
- blocks invalid dependency version key names through
  `checkFigmaVersionNaming`, which is wired into the Gradle `check` lifecycle;
- blocks unused dependency catalog entries through `checkFigmaCatalogUsage`,
  which is wired into the Gradle `check` lifecycle;
- publishes the `TeamCity CI` GitHub status from `Verify`.

TeamCity runs these scripts with Windows PowerShell 5.1 (`powershell.exe`). CI
validation scripts must not depend on APIs available only in newer .NET or
PowerShell versions.

`CI` does not run `generateFigmaDesignModel` and does not publish
`build/reports/figma-sync/design-model.json`. Figma represents the stable
`main` state, so short-lived branch builds must not produce an artifact that can
be mistaken for the official Figma sync input.

### Figma Sync

`Figma Sync` is the post-merge Figma verification pipeline. It is scoped to the
default branch and should not be required by GitHub before merging pull
requests.

The pipeline:

- triggers after `CI` finishes successfully on `<default>`;
- classifies the merged `main` revision before doing expensive work;
- for a model-affecting revision, generates `.teamcity/target/generated-configs`
  once and shares it with the verification job;
- generates `build/reports/figma-sync/design-model.json` from `main` only for a
  model-affecting revision;
- builds the compatible MCP writer and publishes visual plus metadata runner
  manifests with independent model, writer, and transport hashes;
- compares their target fingerprints with Figma metadata and publishes
  `visual-sync-plan.json` with a fail-closed `none`, `partial`, or `full`
  decision;
- sets `FIGMA_DESIGN_SYNC_OFFICIAL=true` and `FIGMA_DESIGN_SYNC_BRANCH` so the
  Gradle task can verify it is running under the official Figma Sync pipeline;
- publishes the Figma report directory and effective TeamCity configuration as
  job artifacts;
- runs `Check Figma trunk sync` against the metadata currently stored in Figma
  only when the model can change;
- publishes the optional `TeamCity Figma Sync` GitHub status on `main`.

For a documentation-only or transport-only `main` revision, the first Figma job
publishes only a `sync-scope.json` artifact and the final job exits successfully
without Maven, Gradle, model generation, metadata validation, or an MCP write.
The previous official Figma metadata remains authoritative because neither the
model nor the compiled visual writer changed.

The same Figma no-op applies to `model-neutral` revisions such as documentation
validation scripts and their coverage manifest. Unlike documentation-only
changes, these revisions still run the normal Gradle CI verification before
merge.

`prepare-figma-sync.ps1` removes the previous
`build/reports/figma-sync` directory before preparing any scope. This prevents a
persistent agent checkout from republishing a stale model or runner during a
documentation-only or transport-only no-op.

Gradle configuration cache and local build cache are enabled in
[`gradle.properties`](../gradle.properties). The current checked-in Pipeline DSL
does not expose TeamCity Build Cache, so cache reuse remains agent-local; do not
add untyped YAML just to force that feature.

The visual write step is still MCP-operated outside TeamCity. Until that write
step is automated, `Figma Sync` is expected to fail after a model-affecting
merge if Figma has not been synchronized yet. That failure is a post-merge
documentation signal, not a pull request merge gate. Run the MCP sync only with
the `design-model.json` artifact from `Figma Sync > Generate main design model`;
branch-local models and locally regenerated models are not authorized
publication inputs. After the MCP sync writes the latest metadata, rerun
`Figma Sync` on `main` to verify the result.

The generated executor and checkpoint contract are ready for a write-capable
MCP endpoint, but the current local Figma Desktop endpoint is capability-gated:
it does not advertise `use_figma` or `upload_assets`. TeamCity therefore
publishes the deterministic plan and runners but does not attempt a speculative
headless visual write. See
[`visual-sync-efficiency.md`](../repo/figma-design-sync/docs/runbooks/visual-sync-efficiency.md).

Use a Finish Build Trigger for this chain, not a direct VCS trigger on
`Figma Sync`. The trigger watches `CI`, requires a successful watched build, and
uses `+:<default>` as its branch filter. This prevents the Figma verification
pipeline from running before `main` has passed the normal CI pipeline.

Keep job reuse disabled:

```kotlin
allowReuse = false
```

This prevents TeamCity from satisfying a branch or pull request pipeline with a
previously successful job from another branch. The generated design model must
belong to the same branch revision as the pipeline chain being validated.

### Queue And Cache Behaviour

Keep TeamCity's built-in build queue optimization enabled for the VCS trigger.
It coalesces obsolete queued runs when a newer revision arrives. The Pipeline
DSL used by this repository has no typed, versioned setting to cancel a job that
has already started; do not add a self-cancellation REST script because it can
race with a newer revision and cancel the wrong run.

The repository enables Gradle configuration cache and build cache in
[`gradle.properties`](../gradle.properties). The current TeamCity DSL artifact
does not expose the Pipeline-compatible Build Cache API, so the active cache is
the local Gradle cache on the Windows build agent. Re-evaluate TeamCity artifact
caching only after the exact DSL dependency used by `.teamcity/pom.xml` exposes
that feature and a generated configuration validates it.

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

The generated CI script content should invoke the versioned scope wrapper:

```yaml
script-content: powershell.exe -NoProfile -ExecutionPolicy Bypass -File .teamcity\scripts\invoke-ci-verification.ps1
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
The `TeamCity CI` status is published by the final `CI` pipeline job, which
depends on the earlier job, so it represents the pull request validation chain.

`Figma Sync` publishes `TeamCity Figma Sync` from its final job so `main`
commits show whether post-merge Figma documentation verification passed. Do not
require that status in GitHub branch protection because the pipeline runs after
changes reach `main`.

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
- emit `commit-status-publisher` on the `CI` verify job and the final
  `Figma Sync` job;
- avoid generating or publishing `design-model.json` from `CI`;
- keep `Figma Sync` as a separate default-branch pipeline;
- set the official Figma design model environment guard only on `Figma Sync`;
- emit `buildDependencyTrigger` for `Figma Sync`, pointing at `CI`, with
  `afterSuccessfulBuildOnly=true`.

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

The public HTTPS route also requires Cloudflare Service Auth. Keep the
Cloudflare service token in PowerShell SecretStore and inject its headers only
for the duration of each CLI command. Follow
[`docs/runbooks/teamcity-cloudflare-access.md`](../docs/runbooks/teamcity-cloudflare-access.md)
for the wrapper, verification procedure, webhook boundary, and the known CSRF
contract for mutating requests.

Read-only diagnostics continue to use TeamCity CLI. Queue the post-MCP
verification rerun through the repository-owned HTTPS client. It performs
read-only idempotency checks in one session and sends the Bearer-authenticated
POST in a separate cookie-free session. The Cloudflare JWT is sent through
`cf-access-token`, so TeamCity does not receive `CF_Authorization` and does not
require CSRF:

```powershell
pwsh -File tools/teamcity/invoke-figma-sync-rerun.ps1 -Wait
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

## Windows Agent Runtime

Run the TeamCity server and build agent with separate virtual service accounts:

- `NT SERVICE\TeamCity` owns the server data directory;
- `NT SERVICE\TCBuildAgent` owns the agent's mutable `system`, `work`, `temp`,
  and `.gradle` directories.

The complete Windows service inventory, recovery order, Cloudflare Tunnel
service, and public-route health checks live in
[`docs/runbooks/teamcity-cloudflare-access.md`](../docs/runbooks/teamcity-cloudflare-access.md#windows-service-runtime).

The agent must use a system-wide JDK instead of a JDK inside a developer profile.
Keep the following properties in
`C:\TeamCity\buildAgent\conf\buildAgent.properties`, updating the Temurin
directory when the installed patch version changes:

```properties
env.JAVA_HOME=C\:\\Program Files\\Eclipse Adoptium\\jdk-21.0.11.10-hotspot
env.GRADLE_USER_HOME=C\:\\TeamCity\\buildAgent\\.gradle
```

`NT SERVICE\TCBuildAgent` needs `Modify` on its mutable directories. It also
needs non-inherited `ReadAndExecute` on `C:\TeamCity` so Java
`Path.toRealPath()` can traverse the parent directory while resolving Gradle
distribution JARs. Do not grant the agent `FullControl` over `C:\TeamCity` or
inherit agent permissions into the server data directory.

After changing the service account or these permissions:

1. stop the build agent;
2. stop Gradle daemons owned by the agent;
3. remove only the failed `dependencies-accessors` cache and the checkout's
   `.gradle` directory;
4. restart the agent and run the pipeline again.

The first clean run downloads and compiles more work than later runs. Confirm
that its log reports the expected JDK and that `BUILD SUCCESSFUL` comes from the
child `Verify` job.

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

If Gradle reports `GeneratedClassCompilationException`, inspect the final
`Caused by` entry from the `--stacktrace` output. An `AccessDeniedException` for
`gradle-core-api-*.jar` from `ZipFileSystemProvider.removeFileSystem` means the
agent cannot resolve the real path through `C:\TeamCity`; fix parent-directory
traversal before clearing the generated accessor cache.

If agent-side checkout reports dubious ownership after changing the Windows
service account, stop the agent and assign both ownership and `Modify` access on
the agent's mutable directories to `NT SERVICE\TCBuildAgent`. Do not hide the
problem with a global Git `safe.directory` entry.

If the TeamCity server cannot run `git ls-remote origin` after its service
account changes, reset the server `git` cache from
`Administration > Diagnostics > Caches`. The page does not show progress; verify
the reset in the server log, then send a VCS commit-hook notification or push a
new commit.

If GitHub receives no status check, inspect `teamcity-commit-status.log` and
confirm that the status publisher feature is attached to the final job.

If `Figma Sync` fails on `main`, check whether the Figma MCP visual sync has
been run with the latest `design-model.json` artifact from
`Figma Sync > Generate main design model`. If the failure mentions a branch
other than `main`, fix repository checkout before investigating Figma sync.

After the MCP write updates official metadata, rerun the complete `Figma Sync`
pipeline with
`pwsh -File tools/teamcity/invoke-figma-sync-rerun.ps1 -Wait`. A successful
standalone `Check Figma trunk sync` proves that metadata matches, but it does
not replace the previously failed aggregate pipeline or its GitHub status.
Confirm that `Generate main design model`, `Check Figma trunk sync`, and the
aggregate `Figma Sync` run all succeed.

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
