package com.marmatsan.dev.catalog_domain.usecase.plant_screen

import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.core_domain.result.Error
import com.marmatsan.dev.core_domain.result.Result
import com.marmatsan.dev.core_domain.usecase.SuspendingUseCase

class InsertPlantUseCase(
    private val repository: CatalogRepository
) : SuspendingUseCase<Plant, Unit, Error>() {
    override suspend fun invoke(
        input: Plant
    ): Result<Unit, Error> = repository.insertPlant(input)
}