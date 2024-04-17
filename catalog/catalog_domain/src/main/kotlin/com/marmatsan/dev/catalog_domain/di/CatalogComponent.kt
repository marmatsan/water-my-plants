package com.marmatsan.dev.catalog_domain.di

import com.marmatsan.dev.catalog_domain.usecase.plant_screen.ValidatePlantNameUseCase
import com.marmatsan.dev.catalog_domain.usecase.plant_screen.ValidateWaterQuantityUseCase
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
abstract class CatalogComponent {
    @Provides
    fun provideValidateWaterQuantityUseCase(): ValidateWaterQuantityUseCase =
        ValidateWaterQuantityUseCase()

    @Provides
    fun provideValidatePlantNameUseCase(): ValidatePlantNameUseCase =
        ValidatePlantNameUseCase()
}