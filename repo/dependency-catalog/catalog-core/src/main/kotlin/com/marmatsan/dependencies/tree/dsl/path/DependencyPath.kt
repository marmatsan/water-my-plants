package com.marmatsan.dependencies.tree.dsl.path

/**
 * Represents a validated dependency path relative to the current DSL node.
 *
 * Dots separate path segments. Empty segments and whitespace are rejected so a compact
 * declaration always expands into an unambiguous node hierarchy.
 *
 * @property segments Ordered path segments to resolve below the current node.
 */
internal class DependencyPath private constructor(
    val segments: List<String>
) {
    /** Parses and validates compact dependency path text. */
    companion object {
        /**
         * Parses [value] as a dot-separated relative dependency path.
         *
         * @throws IllegalArgumentException if the path is blank or contains an invalid segment.
         */
        fun parse(
            value: String
        ): DependencyPath {
            require(value.isNotBlank()) {
                "Dependency path must not be blank"
            }

            val segments = value.split('.')

            require(
                segments.all { segment ->
                    segment.isNotEmpty() &&
                        segment.none { character -> character.isWhitespace() }
                }
            ) {
                "Dependency path '$value' must contain non-blank segments without whitespace"
            }

            return DependencyPath(
                segments = segments
            )
        }
    }
}
