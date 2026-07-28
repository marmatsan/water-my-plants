@file:Suppress("AvoidDuplicateDependencies")

plugins {
    alias(plugins.plugins.com.marmatsan.figmaDocumentationSync)
}

tasks.register("verifyPluginApplication") {
    group = "verification"
    description = "Verifies the published plugin can be resolved and applied without included builds."

    doLast {
        check(project.extensions.findByName("figmaDocumentationSync") != null) {
            "The published plugin did not register the figmaDocumentationSync extension."
        }
        check(
            listOf(
                "generateFigmaDesignModel",
                "prepareCanonicalFigmaSync",
                "verifyCanonicalFigmaSync",
                "checkFigmaTrunkSync",
            ).all(tasks.names::contains),
        ) {
            "The published plugin did not register its public Gradle tasks."
        }
    }
}
