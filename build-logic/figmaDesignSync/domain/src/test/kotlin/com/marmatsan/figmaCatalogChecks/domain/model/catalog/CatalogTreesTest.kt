package com.marmatsan.figmaDesignSync.domain.model.catalog

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class CatalogTreesTest : FunSpec({

    test("catalog version is visible by default when it has a value") {
        // WHEN
        val version = CatalogVersion("2.4.0")

        // THEN
        version.visible shouldBe true
    }

    test("catalog version is hidden by default when it has no value") {
        // WHEN
        val version = CatalogVersion(null)

        // THEN
        version.visible shouldBe false
    }

    test("catalog version cannot be visible without a value") {
        shouldThrow<IllegalArgumentException> {
            CatalogVersion(
                value = null,
                visible = true
            )
        }
    }

    test("library node shows artifacts by default when it has entries") {
        // GIVEN
        val entries = listOf(
            LibraryCatalogEntry.Artifact(
                artifact = "activity-compose",
                version = CatalogVersion("1.12.0")
            )
        )

        // WHEN
        val node = LibraryCatalogNode(
            group = "activity",
            entries = entries
        )

        // THEN
        node.artifactsVisible shouldBe true
    }
})
