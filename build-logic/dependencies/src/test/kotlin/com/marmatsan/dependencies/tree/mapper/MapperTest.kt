package com.marmatsan.dependencies.tree.mapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.model.NodeData
import org.junit.jupiter.api.Test

internal class MapperTest {

    @Test
    fun `maps NodeData_Library to Dependency_Library when entries is not null`() {
        // GIVEN
        val node = NodeData.Library(
            libraryGroup = "androidx.activity",
            entries = listOf(
                NodeData.Library.Entry.Single(
                    NodeData.Library.Artifact(artifact = "activity-compose", version = "1.9.1")
                )
            )
        )

        // WHEN
        val dependency = node.toDependencyLibrary(libraryGroup = "androidx.activity")

        val expected = Dependency.Library(
            libraryGroup = "androidx.activity",
            entries = listOf(
                Dependency.Library.Entry.Single(
                    artifact = Dependency.Library.Artifact(artifact = "activity-compose", version = "1.9.1")
                )
            )
        )

        // THEN
        assertThat(dependency).isEqualTo(expected)
    }

    @Test
    fun `maps NodeData_Plugin to Dependency_Plugin when version is not null`() {
        val node = NodeData.Plugin(
            pluginId = "com.android.application",
            version = "8.10.1"
        )

        val dependency = node.toDependencyPlugin(pluginId = "com.android.application")

        val expected = Dependency.Plugin(
            pluginId = "com.android.application",
            version = "8.10.1"

        )

        assertThat(dependency).isEqualTo(expected)
    }
}