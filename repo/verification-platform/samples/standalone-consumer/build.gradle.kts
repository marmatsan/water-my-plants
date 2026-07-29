@file:Suppress("AvoidDuplicateDependencies")

plugins {
    base
    alias(plugins.plugins.com.marmatsan.verificationPlatform)
}

tasks.register("verifyPluginApplication") {
    group = "verification"
    description = "Verifies the staged verification platform entry point and public task contract."

    doLast {
        check(project.extensions.findByName("verificationPlatform") != null)
        check(
            listOf(
                "generateCiPlan",
                "checkIncludedBuildVersions",
                "checkModuleBoundaries",
                "checkDocumentation"
            ).all(tasks.names::contains)
        )
    }
}
