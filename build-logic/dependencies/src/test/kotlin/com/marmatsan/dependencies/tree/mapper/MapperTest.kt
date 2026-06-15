package com.marmatsan.dependencies.tree.mapper

import com.marmatsan.dependencies.tree.model.Artifact
import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.model.LibraryEntry
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class MapperTest : FunSpec({

    test("maps NodeData_Library to Dependency_Library when entries is not null") {
        // GIVEN
        val node = DependencyNode.Library(
            libraryGroup = "androidx.activity",
            entries = listOf(
                LibraryEntry.Single(
                    Artifact(artifact = "activity-compose", version = "1.9.1")
                )
            )
        )

        // WHEN
        val dependency = node.toDependencyLibrary(libraryGroup = "androidx.activity")

        val expected = Dependency.Library(
            libraryGroup = "androidx.activity",
            entries = listOf(
                LibraryEntry.Single(
                    artifact = Artifact(artifact = "activity-compose", version = "1.9.1")
                )
            )
        )

        // THEN
        dependency shouldBe expected
    }

    test("maps NodeData_Plugin to Dependency_Plugin when version is not null") {
        val node = DependencyNode.Plugin(
            pluginId = "com.android.application",
            version = "8.10.1"
        )

        val dependency = node.toDependencyPlugin(pluginId = "com.android.application")

        val expected = Dependency.Plugin(
            pluginId = "com.android.application",
            version = "8.10.1"

        )

        dependency shouldBe expected
    }
})
