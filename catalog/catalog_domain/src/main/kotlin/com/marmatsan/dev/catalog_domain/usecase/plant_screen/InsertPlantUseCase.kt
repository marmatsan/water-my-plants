package com.marmatsan.dev.catalog_domain.usecase.plant_screen

import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.core_domain.usecase.SuspendingUseCase

class InsertPlantUseCase(
    private val repository: CatalogRepository
) : SuspendingUseCase<Plant, Unit>() {
    override suspend fun invoke(
        input: Plant
    ) = repository.createPlant(input)
}