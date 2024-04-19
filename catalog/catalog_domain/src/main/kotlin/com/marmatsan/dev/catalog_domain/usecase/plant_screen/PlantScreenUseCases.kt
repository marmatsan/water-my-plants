package com.marmatsan.dev.catalog_domain.usecase.plant_screen

data class PlantScreenUseCases(
    val insertPlantUseCase: InsertPlantUseCase,
    val validatePlantNameUseCase: ValidatePlantNameUseCase,
    val validateWaterQuantityUseCase: ValidateWaterQuantityUseCase
)
