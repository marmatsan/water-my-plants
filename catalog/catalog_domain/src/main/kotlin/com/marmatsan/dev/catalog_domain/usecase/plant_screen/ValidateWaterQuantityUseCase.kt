package com.marmatsan.dev.catalog_domain.usecase.plant_screen

import com.marmatsan.dev.core_domain.length
import com.marmatsan.dev.core_domain.usecase.UseCase

class ValidateWaterQuantityUseCase : UseCase {
    operator fun invoke(
        waterQuantity: Int
    ): Int {
        return if (waterQuantity.length() <= 4) {
            waterQuantity
        } else 0
    }
}