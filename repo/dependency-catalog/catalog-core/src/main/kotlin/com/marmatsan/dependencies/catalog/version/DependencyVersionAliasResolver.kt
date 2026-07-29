package com.marmatsan.dependencies.catalog.version

/** Preserves every dependency version key as its symbolic catalog alias. */
object DependencyVersionAliasResolver : DependencyVersionResolver {
    /** Returns [key] unchanged so downstream documentation can retain version ownership. */
    override fun resolve(
        key: String
    ): String = key
}
