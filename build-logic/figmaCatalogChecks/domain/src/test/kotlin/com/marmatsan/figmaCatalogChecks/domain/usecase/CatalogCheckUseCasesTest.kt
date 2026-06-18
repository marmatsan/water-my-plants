package com.marmatsan.figmaCatalogChecks.domain.usecase

import com.marmatsan.figmaCatalogChecks.domain.model.*
import com.marmatsan.figmaCatalogChecks.domain.comparison.*
import com.marmatsan.figmaCatalogChecks.domain.port.*

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class CatalogCheckUseCasesTest : FunSpec({

    test("CheckModulesUseCase compares modules through ports") {
        // GIVEN
        val useCase = CheckModulesUseCase(
            projectModulesPort = FakeProjectModulesPort(
                modules = setOf(":app", ":core:ui")
            ),
            figmaModulesPort = FakeFigmaModulesPort(
                modules = setOf(":app", ":core:core_ui")
            ),
            moduleNamesComparison = ModuleNamesComparison()
        )

        // WHEN
        val result = useCase.execute(
            CheckModulesUseCaseRequest(
                page = FigmaNodeReference(
                    fileKey = "file",
                    nodeId = "1:1"
                ),
                moduleComponent = FigmaNodeReference(
                    fileKey = "file",
                    nodeId = "2:2"
                ),
                rootSettingsFilePath = "settings.gradle.kts",
                buildLogicSettingsFilePath = "build-logic/settings.gradle.kts",
                token = "token"
            )
        )

        // THEN
        result shouldBe ModulesCheckResult.Mismatch(
            comparison = ModuleNamesComparisonResult(
                missingInFigma = setOf(":core:ui"),
                extraInFigma = setOf(":core:core_ui")
            )
        )
    }

    test("CheckLibraryCatalogTreeUseCase returns match without depending on data layer") {
        // GIVEN
        val tree = LibraryCatalogTree(
            roots = listOf(
                LibraryCatalogNode(group = "androidx")
            )
        )
        val useCase = CheckLibraryCatalogTreeUseCase(
            projectCatalogTreesPort = FakeProjectCatalogTreesPort(libraryTree = tree),
            figmaCatalogTreesPort = FakeFigmaCatalogTreesPort(libraryTree = tree),
            libraryCatalogTreeComparison = LibraryCatalogTreeComparison()
        )

        // WHEN
        val result = useCase.execute(
            CheckLibraryCatalogTreeUseCaseRequest(
                page = FigmaNodeReference(
                    fileKey = "file",
                    nodeId = "1:1"
                ),
                section = FigmaNodeReference(
                    fileKey = "file",
                    nodeId = "2:2"
                ),
                projectSource = ProjectCatalogTreeSource.DependenciesDslVersionAliases,
                token = "token"
            )
        )

        // THEN
        result shouldBe LibraryCatalogTreeCheckResult.Match(
            sectionNodeId = "2:2",
            repositoryNodeCount = 1
        )
    }
})

private class FakeProjectModulesPort(
    private val modules: Set<String>
) : ProjectModulesPort {
    override fun readModules(source: ProjectModulesSource): Set<String> = modules
}

private class FakeFigmaModulesPort(
    private val modules: Set<String>
) : FigmaModulesPort {
    override fun readModules(source: FigmaModuleComponentSource): Set<String> = modules
}

private class FakeProjectCatalogTreesPort(
    private val libraryTree: LibraryCatalogTree = LibraryCatalogTree(roots = emptyList()),
    private val pluginTree: PluginCatalogTree = PluginCatalogTree(roots = emptyList())
) : ProjectCatalogTreesPort {
    override fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree = libraryTree

    override fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree = pluginTree
}

private class FakeFigmaCatalogTreesPort(
    private val libraryTree: LibraryCatalogTree = LibraryCatalogTree(roots = emptyList()),
    private val pluginTree: PluginCatalogTree = PluginCatalogTree(roots = emptyList())
) : FigmaCatalogTreesPort {
    override fun readLibraryTree(source: FigmaCatalogTreeSource): LibraryCatalogTree = libraryTree

    override fun readPluginTree(source: FigmaCatalogTreeSource): PluginCatalogTree = pluginTree
}
