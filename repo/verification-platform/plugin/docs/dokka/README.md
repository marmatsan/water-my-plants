# Module verification-platform-plugin

Gradle composition root for repository CI planning.

This module connects Git and Gradle adapters to the domain services and exposes
the resulting contracts through reviewed Gradle tasks. Provider-specific task
orchestration belongs here rather than in `verification-platform-domain`.

Read this module from its public Gradle entry points:

1. `VerificationPlatformPlugin` registers the CI planning and infrastructure tasks.
2. `GenerateCiPlanTask` writes the provider-neutral verification plan.
3. `GenerateCiTopologyPreviewTask` writes a non-authoritative agent topology
   preview.
4. `PrepareTeamCityCiPlanTask` exports allow-listed TeamCity parameters.
5. `RunTeamCityInfrastructureHealthTask` queues the infrastructure-health run.

# Package com.marmatsan.verificationPlatform.plugin

Gradle plugin and task types that compose the CI domain with concrete adapters.
