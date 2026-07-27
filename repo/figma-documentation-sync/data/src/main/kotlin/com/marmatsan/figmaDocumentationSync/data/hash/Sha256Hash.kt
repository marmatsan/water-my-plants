package com.marmatsan.figmaDocumentationSync.data.hash

import java.security.MessageDigest

/** SHA-256 wire format shared by Kotlin and the transitional Node writer. */
object Sha256Hash {
    /** Returns the lowercase SHA-256 hash of UTF-8 [value]. */
    fun of(
        value: String,
    ): String =
        of(
            value = value.toByteArray(Charsets.UTF_8),
        )

    /** Returns the lowercase SHA-256 hash of raw [value]. */
    fun of(
        value: ByteArray,
    ): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value)
            .joinToString(
                prefix = "sha256:",
                separator = "",
            ) { byte ->
                "%02x".format(
                    byte,
                )
            }
}
