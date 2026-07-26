package com.marmatsan.dependencies.catalog.api

/** Convenience API for repositories that provide both resolved and aliased catalog views. */
interface DependencyCatalogProvider :
    ResolvedDependencyCatalogProvider,
    VersionAliasedDependencyCatalogProvider
