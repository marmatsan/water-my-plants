package com.marmatsan.dependencies.gradle.tree.dsl

/** Validates one public catalog path and returns its ordered dot-separated segments. */
internal fun catalogPathSegments(
    path: String,
): List<String> {
    require(path.isNotBlank()) {
        "Catalog path must not be blank"
    }

    return path.split('.').also { segments ->
        require(
            segments.all { segment ->
                segment.isNotBlank() && segment == segment.trim()
            },
        ) {
            "Catalog path '$path' must contain non-blank segments without surrounding whitespace"
        }
    }
}
