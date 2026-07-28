# Module verification-platform-plugin

Gradle composition root for repository CI planning.

This module connects Git and Gradle adapters to the domain services and exposes
the resulting contracts through reviewed Gradle tasks. Provider-specific task
orchestration belongs here rather than in `verification-platform-domain`.

Read this module from its public Gradle entry points:

1. `VerificationPlatformPlugin` registers the CI planning and infrastructure tasks.
2. `CheckGitWorkflowTask` validates the branch contract.
3. `VerificationPlatformPlugin` exposes the repository-wide
   `checkKotlinStyle` and `formatKotlinStyle` lifecycle tasks while their KtLint
   engine remains isolated in the included build.
4. `GenerateCiPlanTask` writes the provider-neutral verification plan.
5. `GenerateCiTopologyPreviewTask` writes a non-authoritative agent topology
   preview.
6. `PrepareTeamCityCiPlanTask` exports allow-listed TeamCity parameters.
7. `RunTeamCityInfrastructureHealthTask` queues the infrastructure-health run.
8. `IncludedBuildVerificationTasksExtension` binds direct included tasks and
   isolated wrapper invocations with explicit composition properties.

# Package com.marmatsan.verificationPlatform.plugin

Contains the public Gradle plugin composition root.

# Package com.marmatsan.verificationPlatform.plugin.extension

Defines consumer-facing configuration grouped by repository capability.

# Package com.marmatsan.verificationPlatform.plugin.registration

Contains focused task registrars selected by the plugin composition root. Each
registrar owns one repository capability and exchanges lazy `TaskProvider`
contracts so task realization remains controlled by Gradle.

# Package com.marmatsan.verificationPlatform.plugin.task.boundary

Validates included-build version ownership and reusable module boundaries.

# Package com.marmatsan.verificationPlatform.plugin.task.ci

Generates provider-neutral CI plans and non-authoritative topology previews.

# Package com.marmatsan.verificationPlatform.plugin.task.documentation

Validates repository documentation coverage and structure.

# Package com.marmatsan.verificationPlatform.plugin.task.git

Validates Git workflow and repository change-set contracts.

# Package com.marmatsan.verificationPlatform.plugin.task.teamcity

Validates and coordinates the TeamCity-specific execution boundary.
