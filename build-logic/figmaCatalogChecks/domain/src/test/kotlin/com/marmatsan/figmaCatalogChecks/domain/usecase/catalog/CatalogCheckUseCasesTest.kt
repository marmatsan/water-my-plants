package com.marmatsan.figmaCatalogChecks.domain.usecase.catalog

import com.marmatsan.figmaCatalogChecks.domain.comparison.catalog.LibraryCatalogTreeComparison
import com.marmatsan.figmaCatalogChecks.domain.comparison.usage.LibraryCatalogUsageComparison
import com.marmatsan.figmaCatalogChecks.domain.comparison.modules.ModuleNamesComparison
import com.marmatsan.figmaCatalogChecks.domain.comparison.modules.ModuleNamesComparisonResult
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.model.figma.FigmaNodeReference
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaCatalogChecks.domain.usecase.modules.CheckModulesUseCase
import com.marmatsan.figmaCatalogChecks.domain.usecase.modules.CheckModulesUseCaseRequest
import com.marmatsan.figmaCatalogChecks.domain.usecase.modules.FakeFigmaModulesPort
import com.marmatsan.figmaCatalogChecks.domain.usecase.modules.FakeProjectModulesPort
import com.marmatsan.figmaCatalogChecks.domain.usecase.modules.ModulesCheckResult
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
            projectCatalogUsagePort = FakeProjectCatalogUsagePort(),
            libraryCatalogTreeComparison = LibraryCatalogTreeComparison(),
            libraryCatalogUsageComparison = LibraryCatalogUsageComparison()
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
