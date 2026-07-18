plugins {
    id("com.marmatsan.figmaDesignSync")
}

tasks.register("verifyPluginApplication") {
    group = "verification"
    description = "Verifies the published plugin can be resolved and applied without included builds."

    doLast {
        check(project.extensions.findByName("figmaDesignSync") != null) {
            "The published plugin did not register the figmaDesignSync extension."
        }
        check(
            listOf(
                "generateFigmaDesignModel",
                "prepareOfficialFigmaSync",
                "verifyOfficialFigmaSync",
                "checkFigmaTrunkSync"
            ).all(tasks.names::contains)
        ) {
            "The published plugin did not register its public Gradle tasks."
        }
    }
}
