package com.marmatsan.dev.catalog_domain.di

import com.marmatsan.dev.catalog_domain.usecase.plant_screen.ValidateWaterQuantityUseCase
import me.tatarka.inject.annotations.Provides

interface CatalogComponent {
    @Provides
    fun provideValidateWaterQuantityUseCase(): ValidateWaterQuantityUseCase =
        ValidateWaterQuantityUseCase()
}