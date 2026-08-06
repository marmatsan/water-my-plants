@file:Suppress("AvoidDuplicateDependencies")

plugins {
    alias(plugins.plugins.com.marmatsan.figmaDocumentationSync)
    alias(plugins.plugins.com.marmatsan.figmaDocumentationSync.teamcityOperations)
}

tasks.register("verifyPluginApplication") {
    group = "verification"
    description = "Verifies the published plugin can be resolved and applied without included builds."

    doLast {
        check(project.extensions.findByName("figmaDocumentationSync") != null) {
            "The published plugin did not register the figmaDocumentationSync extension."
        }
        check(project.extensions.findByName("figmaTeamCityOperations") != null) {
            "The published plugin did not register the figmaTeamCityOperations extension."
        }
        check(
            listOf(
                "generateFigmaDesignModel",
                "prepareCanonicalFigmaSync",
                "verifyCanonicalFigmaSync",
                "checkFigmaTrunkSync"
            ).all(tasks.names::contains)
        ) {
            "The published plugin did not register its public Gradle tasks."
        }
        check(
            listOf(
                "prepareTeamCityFigmaSyncHandoff",
                "uploadCanonicalFigmaPayload",
                "rerunTeamCityFigmaSync"
            ).all(tasks.names::contains)
        ) {
            "The TeamCity operations plugin did not register its public Gradle tasks."
        }
    }
}
