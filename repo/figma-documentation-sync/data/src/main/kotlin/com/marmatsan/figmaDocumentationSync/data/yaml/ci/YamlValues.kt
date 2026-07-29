package com.marmatsan.figmaDocumentationSync.data.yaml.ci

/** Returns this YAML value as a string-keyed map or fails with [context]. */
internal fun Any?.asStringMap(
    context: String
): Map<String, Any?> {
    val source = this as? Map<*, *> ?: error("Expected YAML mapping for $context")
    return source.entries.associate { (key, value) ->
        val stringKey = key as? String ?: error("Expected string key in $context")
        stringKey to value
    }
}

/** Returns the required YAML map stored under [key]. */
internal fun Map<String, Any?>.requiredMap(
    key: String
): Map<String, Any?> =
    get(
        key = key
    ).asStringMap(
        context = key
    )

/** Returns the required YAML list stored under [key]. */
internal fun Map<String, Any?>.requiredList(
    key: String
): List<Any?> =
    get(
        key = key
    ) as? List<*> ?: error("Expected YAML list '$key'")

/** Returns the required YAML string stored under [key]. */
internal fun Map<String, Any?>.requiredString(
    key: String
): String =
    get(
        key = key
    ) as? String ?: error("Expected YAML string '$key'")

/** Returns the optional YAML string stored under [key]. */
internal fun Map<String, Any?>.optionalString(
    key: String
): String? =
    get(
        key = key
    )?.let { value -> value as? String ?: error("Expected YAML string '$key'") }

/** Returns the required YAML integer stored under [key]. */
internal fun Map<String, Any?>.requiredInt(
    key: String
): Int =
    (
        get(
            key = key
        ) as? Number
    )?.toInt() ?: error("Expected YAML integer '$key'")

/** Returns the optional YAML string list stored under [key], or an empty list when absent. */
internal fun Map<String, Any?>.optionalStringList(
    key: String
): List<String> =
    when (
        val value =
            get(
                key = key
            )
    ) {
        null -> {
            emptyList()
        }

        is List<*> -> {
            value.map { item ->
                item as? String ?: error("Expected string value in YAML list '$key'")
            }
        }

        else -> {
            error("Expected YAML list '$key'")
        }
    }
