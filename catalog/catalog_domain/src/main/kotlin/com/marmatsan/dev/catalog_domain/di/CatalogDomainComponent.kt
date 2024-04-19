package com.marmatsan.dev.catalog_domain.di

import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.InsertPlantUseCase
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.PlantScreenUseCases
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.ValidatePlantNameUseCase
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.ValidateWaterQuantityUseCase
import me.tatarka.inject.annotations.Provides

interface CatalogDomainComponent {
    @Provides
    fun providePlantScreenUseCases(
        repository: CatalogRepository
    ): PlantScreenUseCases {
        return PlantScreenUseCases(
            insertPlantUseCase = InsertPlantUseCase(repository),
            validatePlantNameUseCase = ValidatePlantNameUseCase(),
            validateWaterQuantityUseCase = ValidateWaterQuantityUseCase()
        )
    }
}