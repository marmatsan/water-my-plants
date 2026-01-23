package com.marmatsan.dependencies.gradle

import org.gradle.api.artifacts.VersionCatalog

fun VersionCatalog.getLibraryByAlias(alias: String): String {
    val provider = findLibrary(alias).orElseThrow {
        NoSuchElementException("Library alias '$alias' not found in version catalog named ${this.name}")
    }
    return provider.get().toString()
}