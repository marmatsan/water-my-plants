package com.marmatsan.dependencies.catalog.version

/** Resolves the representation used for one dependency version key. */
fun interface DependencyVersionResolver {
    /**
     * Resolves the exact, case-sensitive [key].
     *
     * Implementations may return a concrete version or preserve the key as a symbolic alias.
     */
    fun resolve(
        key: String
    ): String
}
