package com.marmatsan.verificationPlatform.domain.service.errorhandling

/** Masks Kotlin comments and literals while preserving source line positions. */
internal class KotlinSourceCodeMasker {
    /** Returns [source] with non-code characters replaced by spaces. */
    fun mask(
        source: String,
    ): String {
        val masked = StringBuilder(source.length)
        var index = 0
        var state = State.CODE
        var blockCommentDepth = 0

        while (index < source.length) {
            when (state) {
                State.CODE -> {
                    when {
                        source.startsWith(
                            prefix = "//",
                            startIndex = index,
                        ) -> {
                            masked.appendMasked(
                                source = source,
                                startIndex = index,
                                length = 2,
                            )
                            index += 2
                            state = State.LINE_COMMENT
                        }

                        source.startsWith(
                            prefix = "/*",
                            startIndex = index,
                        ) -> {
                            masked.appendMasked(
                                source = source,
                                startIndex = index,
                                length = 2,
                            )
                            index += 2
                            blockCommentDepth = 1
                            state = State.BLOCK_COMMENT
                        }

                        source.startsWith(
                            prefix = "\"\"\"",
                            startIndex = index,
                        ) -> {
                            masked.appendMasked(
                                source = source,
                                startIndex = index,
                                length = 3,
                            )
                            index += 3
                            state = State.RAW_STRING
                        }

                        source[index] == '"' -> {
                            masked.append(' ')
                            index += 1
                            state = State.STRING
                        }

                        source[index] == '\'' -> {
                            masked.append(' ')
                            index += 1
                            state = State.CHARACTER
                        }

                        else -> {
                            masked.append(source[index])
                            index += 1
                        }
                    }
                }

                State.LINE_COMMENT -> {
                    val character = source[index]
                    masked.appendMasked(character)
                    index += 1
                    if (character.isLineBreak()) {
                        state = State.CODE
                    }
                }

                State.BLOCK_COMMENT -> {
                    when {
                        source.startsWith(
                            prefix = "/*",
                            startIndex = index,
                        ) -> {
                            masked.appendMasked(
                                source = source,
                                startIndex = index,
                                length = 2,
                            )
                            index += 2
                            blockCommentDepth += 1
                        }

                        source.startsWith(
                            prefix = "*/",
                            startIndex = index,
                        ) -> {
                            masked.appendMasked(
                                source = source,
                                startIndex = index,
                                length = 2,
                            )
                            index += 2
                            blockCommentDepth -= 1
                            if (blockCommentDepth == 0) {
                                state = State.CODE
                            }
                        }

                        else -> {
                            masked.appendMasked(source[index])
                            index += 1
                        }
                    }
                }

                State.RAW_STRING -> {
                    if (
                        source.startsWith(
                            prefix = "\"\"\"",
                            startIndex = index,
                        )
                    ) {
                        masked.appendMasked(
                            source = source,
                            startIndex = index,
                            length = 3,
                        )
                        index += 3
                        state = State.CODE
                    } else {
                        masked.appendMasked(source[index])
                        index += 1
                    }
                }

                State.STRING,
                State.CHARACTER,
                -> {
                    val character = source[index]
                    when {
                        character == '\\' && index + 1 < source.length -> {
                            masked.appendMasked(
                                source = source,
                                startIndex = index,
                                length = 2,
                            )
                            index += 2
                        }

                        state == State.STRING && character == '"' -> {
                            masked.append(' ')
                            index += 1
                            state = State.CODE
                        }

                        state == State.CHARACTER && character == '\'' -> {
                            masked.append(' ')
                            index += 1
                            state = State.CODE
                        }

                        else -> {
                            masked.appendMasked(character)
                            index += 1
                        }
                    }
                }
            }
        }

        return masked.toString()
    }

    private fun StringBuilder.appendMasked(
        source: String,
        startIndex: Int,
        length: Int,
    ) {
        repeat(
            times = length,
        ) { offset ->
            appendMasked(
                character = source[startIndex + offset],
            )
        }
    }

    private fun StringBuilder.appendMasked(
        character: Char,
    ) {
        append(
            if (character.isLineBreak()) {
                character
            } else {
                ' '
            },
        )
    }

    private fun Char.isLineBreak(): Boolean = this == '\r' || this == '\n'

    private enum class State {
        CODE,
        LINE_COMMENT,
        BLOCK_COMMENT,
        STRING,
        RAW_STRING,
        CHARACTER,
    }
}
