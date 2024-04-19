package com.marmatsan.dev.catalog_domain.repository

import com.marmatsan.dev.catalog_domain.model.Plant

interface CatalogRepository {
    suspend fun insertPlant(plant: Plant)
}