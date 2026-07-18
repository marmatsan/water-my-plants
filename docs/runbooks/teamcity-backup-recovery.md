---
title: TeamCity backup and recovery
type: runbook
scope: repository-ci
owner: ci-platform
status: active
last-reviewed: 2026-07-18
review-cycle-days: 90
sources:
  - .teamcity/settings.kts
  - docs/ci/windows-runtime.yaml
  - docs/runbooks/teamcity-cloudflare-access.md
---

# TeamCity Backup And Recovery Runbook

## Purpose

Use this runbook to create a recoverable TeamCity backup, verify that it has
left the TeamCity host, or restore the service in an isolated environment. Run
it before a TeamCity upgrade and at least monthly as an explicit recovery
exercise.

The target service levels are:

- recovery point objective (RPO): at most 24 hours for TeamCity configuration
  and database state;
- recovery time objective (RTO): four hours to restore an isolated server and
  prove that projects, users, build history, and retained artifacts are usable.

## Prerequisites

- TeamCity system-administrator permission.
- An encrypted backup destination outside the TeamCity host with enough free
  space for the backup ZIP and `system/artifacts`.
- The active TeamCity Data Directory from `Administration > Global Settings`.
- The TeamCity installation directory containing `bin\maintainDB.cmd`.
- An isolated Windows host or VM for the quarterly restore drill.
- A recorded checksum and timestamp for each backup set. Do not store secrets
  or backup archives in this repository.

## Procedure

1. Confirm `TeamCity`, `TCBuildAgent`, and `Cloudflared` are healthy and record
   the active TeamCity version and Data Directory.
2. Daily, from `Administration > Backup`, start a custom online backup containing
   configuration, database, supplementary data, and personal changes. Build
   logs are optional because the artifact tree is copied separately.
3. Wait until `GET /app/rest/server/backup` reports `idle` and confirm a new ZIP
   exists under `<TeamCity Data Directory>\backup`.
4. Copy the ZIP and the complete
   `<TeamCity Data Directory>\system\artifacts` tree to the encrypted off-host
   destination. TeamCity backup ZIPs do not contain build artifacts.
5. Copy installation-specific configuration needed to rebuild the origin,
   including `<TeamCity Home>\conf\server.xml`. Store it beside the backup,
   never in Git.
6. Generate SHA-256 checksums at the destination and record the backup time,
   TeamCity version, database type, Data Directory, artifact-copy completion,
   and storage retention expiry.
7. Retain at least seven daily backup sets and three monthly recovery points.
   Delete an old set only after a newer set has passed checksum verification.

For an upgrade or an exact point-in-time maintenance backup, stop the server
after draining the queue and use `maintainDB.cmd` so running and queued state is
not changing during capture:

```powershell
Stop-Service -Name TeamCity
& '<TeamCity Home>\bin\maintainDB.cmd' backup -A '<TeamCity Data Directory>'
Start-Service -Name TeamCity
```

Replace the angle-bracket placeholders with paths verified from the active
server. Execute service operations only from an elevated PowerShell session.

## Verification

For every backup set:

1. Recalculate the ZIP and artifact-copy checksums from the off-host location.
2. Open the ZIP and confirm it contains TeamCity configuration and database
   content; a file existing with non-zero size is not sufficient proof.
3. Confirm the artifact copy includes a recently retained
   `build/reports/figma-sync` artifact.

Once per quarter, restore into a clean isolated Data Directory with the same or
a newer TeamCity version:

```powershell
& '<TeamCity Home>\bin\maintainDB.cmd' restore `
    -A '<New TeamCity Data Directory>' `
    -F '<Off-host backup ZIP>' `
    -T '<Target database.properties>'
```

Copy `system\artifacts` into the new Data Directory before the restored server
starts. Keep Cloudflare Tunnel disabled on the drill host. Start the isolated
server and verify login, project configuration, recent build history, one Figma
artifact download, and one non-mutating DSL generation. Record actual RPO and
RTO; the drill passes only when both targets are met.

## Recovery

If backup creation fails, preserve the last verified set, correct free-space or
permission failures, and start a new backup. Never overwrite the only known
good set.

If restore fails, leave the production server untouched. Create another clean
Data Directory, correct the database or filesystem issue, and retry. Use
`maintainDB.cmd restore --continue` only after removing the partially restored
database object named by TeamCity. If artifacts were copied after first start,
run TeamCity artifact-metadata reindexing before declaring recovery complete.

## Prohibited Actions

- Do not treat `.teamcity/settings.kts` as a backup of users, credentials,
  history, database state, plugins, or artifacts.
- Do not keep the only backup on the TeamCity server disk.
- Do not restore over the active production Data Directory as a drill.
- Do not start a restored server before its artifact tree is in place.
- Do not expose the isolated recovery host through the production tunnel.

## Sources

- [TeamCity data backup](https://www.jetbrains.com/help/teamcity/teamcity-data-backup.html)
- [Restoring TeamCity data](https://www.jetbrains.com/help/teamcity/restoring-teamcity-data-from-backup.html)
- `.teamcity/settings.kts`
- `docs/ci/windows-runtime.yaml`
- `docs/runbooks/teamcity-cloudflare-access.md`
