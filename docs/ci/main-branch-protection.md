# Main Branch Protection

`main` is the trunk branch. It must stay stable, tested, and releasable.

## Required Flow

All changes enter `main` through a pull request from a short-lived branch:

```text
feature/<short-description>
fix/<short-description>
chore/<short-description>
```

Do not push directly to `main`.

The expected flow is:

```text
branch -> pull request -> TeamCity CI -> merge -> CI on main -> Figma Sync
```

## GitHub Ruleset

The `Main` GitHub ruleset targets:

```text
refs/heads/main
```

It enforces:

- branch deletion is blocked;
- non-fast-forward updates are blocked;
- linear history is required;
- changes must be merged through a pull request;
- allowed pull request merge methods are squash and rebase;
- review conversations must be resolved before merge;
- `TeamCity CI` must pass before merge;
- required status checks must be strict, so the pull request branch must be up
  to date with `main` before merge.

The required status check is:

```text
TeamCity CI
```

Use `Any source` for this required status check. TeamCity currently publishes
`TeamCity CI` as a classic commit status through Commit Status Publisher, not as
a GitHub Checks API run. The PR can therefore show `Checks (0)` while still
showing `All checks have passed` for `TeamCity CI`.

Do not require `Figma Sync` in the GitHub ruleset. `Figma Sync` runs after
changes reach `main`.

## TeamCity CI

`CI` is the only required TeamCity pipeline for pull requests.

It validates:

- Gradle `check`;
- Figma design model generation;
- publication of `build/reports/figma-sync/design-model.json`.

The final `CI` job publishes the `TeamCity CI` commit status to GitHub.

## Post-Merge Figma Sync

After a pull request is merged, TeamCity runs `CI` on `main`.

When that `main` CI run succeeds, TeamCity triggers `Figma Sync` through a
Finish Build Trigger.

`Figma Sync`:

- runs only for `<default>`;
- generates the design model from `main`;
- publishes the generated design model artifact;
- runs `checkFigmaTrunkSync`.

The Figma write step is currently MCP-operated. If `Figma Sync` fails because
Figma is out of sync, run the MCP visual sync with the generated
`design-model.json`, then rerun `Figma Sync`.

## If CI Fails On Main

If `CI` fails on `main`, trunk contains a broken state.

Stop merging new pull requests until the failure is understood. Fix it through
the normal pull request flow:

```text
fix/<short-description> -> pull request -> TeamCity CI -> merge
```

Do not bypass the ruleset or push directly to `main` to repair the failure.
