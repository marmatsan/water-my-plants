package com.marmatsan.figmaDocumentationSync.domain.model.visual

/** Deterministic, Figma-independent plan for the CI documentation surface. */
data class CiVisualPlan(
    val parentName: String,
    val sections: List<Section>,
) {
    data class Section(
        val target: String,
        val name: String,
        val description: String,
        val orientation: Orientation,
        val headerSources: List<HeaderSource>,
        val nodes: List<Node>,
        val connections: List<Connection>,
    )

    data class HeaderSource(
        val label: String,
        val url: String,
    )

    data class Node(
        val id: String,
        val type: Type,
        val environment: Environment,
        val name: String,
        val description: String,
        val steps: List<Step>,
        val runtime: Runtime?,
        val source: String,
        val sourceUrl: String,
        val row: Int,
        val column: Int,
    )

    data class Step(
        val order: String,
        val role: StepRole,
        val level: StepLevel,
        val title: String,
        val technicalId: String?,
        val description: String?,
        val condition: String?,
    )

    data class Runtime(
        val platform: String,
        val service: String,
        val startup: String,
        val identity: String,
    )

    data class Connection(
        val id: String,
        val source: String,
        val target: String,
        val label: String,
    )

    enum class Orientation(
        val wireValue: String,
    ) {
        HORIZONTAL("horizontal"),
        GRID("grid"),
    }

    enum class Type(
        val wireValue: String,
    ) {
        ACTOR("actor"),
        SYSTEM("system"),
        GIT_REFERENCE("git reference"),
        PIPELINE("pipeline"),
        JOB("job"),
        ARTIFACT("artifact"),
        CHECK("check"),
        GATE("gate"),
    }

    enum class Environment(
        val wireValue: String,
    ) {
        GITHUB("github"),
        TEAMCITY("teamcity"),
        CLOUDFLARE("cloudflare"),
        FIGMA("figma"),
        CODEX("codex"),
        BROWSER("browser"),
        TERMINAL("terminal"),
        OPERATOR("operator"),
        JSON("json"),
    }

    enum class StepRole(
        val wireValue: String,
    ) {
        ACTION("action"),
        DECISION("decision"),
        OUTCOME("outcome"),
    }

    enum class StepLevel(
        val wireValue: String,
    ) {
        PHASE("phase"),
        NESTED("nested"),
    }
}
