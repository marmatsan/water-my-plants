package com.marmatsan.figmaCatalogChecks.domain.port.versions

interface FigmaVersionsPort {
    fun readVersions(source: FigmaVersionsSource): Map<String, String>
}
