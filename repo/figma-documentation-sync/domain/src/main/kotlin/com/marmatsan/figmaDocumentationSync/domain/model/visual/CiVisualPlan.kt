package com.marmatsan.figmaDocumentationSync.domain.model.visual

/**
 * Deterministic, Figma-independent plan for the CI documentation surface.
 *
 * @property parentName name of the Figma parent documentation section.
 * @property sections independently rendered CI documentation sections.
 */
data class CiVisualPlan(
    val parentName: String,
    val sections: List<Section>,
) {
    /**
     * One independently rendered CI documentation section.
     *
     * @property target stable writer target name.
     * @property name human-readable section name.
     * @property description purpose of the section.
     * @property orientation layout strategy used by the writer.
     * @property headerSources repository sources linked from the section header.
     * @property nodes semantic CI nodes rendered in the section.
     * @property connections directed relationships between section nodes.
     */
    data class Section(
        val target: String,
        val name: String,
        val description: String,
        val orientation: Orientation,
        val headerSources: List<HeaderSource>,
        val nodes: List<Node>,
        val connections: List<Connection>,
    )

    /**
     * Repository source linked from a generated section header.
     *
     * @property label source label rendered in Figma.
     * @property url canonical main-branch source URL.
     */
    data class HeaderSource(
        val label: String,
        val url: String,
    )

    /**
     * Semantic node positioned inside a CI visual section.
     *
     * @property id stable node identity within the section.
     * @property type semantic node family.
     * @property environment visual environment icon selection.
     * @property name human-readable node name.
     * @property description operational responsibility of the node.
     * @property phases ordered phases rendered inside the node.
     * @property outcomes ordered observable outcomes.
     * @property runtime optional Windows runtime details.
     * @property source concise canonical source label.
     * @property sourceUrl canonical main-branch source URL.
     * @property row logical layout row.
     * @property column logical layout column.
     */
    data class Node(
        val id: String,
        val type: Type,
        val environment: Environment,
        val name: String,
        val description: String,
        val phases: List<Phase>,
        val outcomes: List<Outcome>,
        val runtime: Runtime?,
        val source: String,
        val sourceUrl: String,
        val row: Int,
        val column: Int,
    )

    /**
     * Ordered group of steps rendered inside a CI node.
     *
     * @property order stable display order.
     * @property title human-readable phase title.
     * @property technicalId optional adapter-specific identity.
     * @property description optional operational description.
     * @property steps ordered executable or decision steps.
     */
    data class Phase(
        val order: String,
        val title: String,
        val technicalId: String?,
        val description: String?,
        val steps: List<Step>,
    )

    /**
     * One action or decision rendered inside a CI phase.
     *
     * @property order stable display order.
     * @property role semantic rendering role.
     * @property title human-readable step title.
     * @property technicalId optional adapter-specific identity.
     * @property description optional operational description.
     * @property condition optional execution condition.
     */
    data class Step(
        val order: String,
        val role: StepRole,
        val title: String,
        val technicalId: String?,
        val description: String?,
        val condition: String?,
    )

    /**
     * Observable artifact, check, success, or action produced by a node.
     *
     * @property order stable display order.
     * @property kind semantic outcome family.
     * @property title human-readable outcome title.
     * @property technicalId optional adapter-specific identity.
     * @property description optional operational description.
     * @property condition optional condition under which the outcome exists.
     */
    data class Outcome(
        val order: String,
        val kind: OutcomeKind,
        val title: String,
        val technicalId: String?,
        val description: String?,
        val condition: String?,
    )

    /**
     * Windows runtime details attached to an infrastructure node.
     *
     * @property platform host platform description.
     * @property service Windows service identifier.
     * @property startup expected startup mode.
     * @property identity operating-system identity running the service.
     */
    data class Runtime(
        val platform: String,
        val service: String,
        val startup: String,
        val identity: String,
    )

    /**
     * Directed visual relationship between two nodes.
     *
     * @property id stable connection identity.
     * @property source origin node id.
     * @property target destination node id.
     * @property label concise relationship label.
     * @property kind semantic connector family.
     */
    data class Connection(
        val id: String,
        val source: String,
        val target: String,
        val label: String,
        val kind: ConnectionKind,
    )

    /**
     * Layout strategy for one CI section.
     *
     * @property wireValue serialized writer value.
     */
    enum class Orientation(
        val wireValue: String,
    ) {
        HORIZONTAL("horizontal"),
        GRID("grid"),
    }

    /**
     * Semantic family of a visual CI node.
     *
     * @property wireValue serialized writer value.
     */
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

    /**
     * Environment icon rendered for a visual CI node.
     *
     * @property wireValue serialized component variant.
     */
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
        GRADLE("gradle"),
    }

    /**
     * Semantic rendering role of a CI step.
     *
     * @property wireValue serialized component variant.
     */
    enum class StepRole(
        val wireValue: String,
    ) {
        ACTION("action"),
        DECISION("decision"),
        GROUP("group"),
    }

    /**
     * Semantic rendering family of a node outcome.
     *
     * @property wireValue serialized component variant.
     */
    enum class OutcomeKind(
        val wireValue: String,
    ) {
        ARTIFACT("artifact"),
        CHECK("check"),
        SUCCESS("success"),
        ACTION("action"),
    }

    /**
     * Semantic rendering family of a directed connection.
     *
     * @property wireValue serialized connector value.
     */
    enum class ConnectionKind(
        val wireValue: String,
    ) {
        CONTROL("control"),
        DATA("data"),
        STATUS("status"),
        ATTENTION("attention"),
        NEUTRAL("neutral"),
    }
}
