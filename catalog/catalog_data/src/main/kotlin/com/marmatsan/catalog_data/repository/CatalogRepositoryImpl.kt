package com.marmatsan.catalog_data.repository

import com.marmatsan.catalog_data.local.DatabaseManagerLocalSource
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class CatalogRepositoryImpl(
    private val databaseManager: DatabaseManagerLocalSource
) : CatalogRepository {
    override suspend fun createPlant(plant: Plant) =
        databaseManager.createPlant(plant)

    override fun readPlantById(plantId: String): Plant =
        databaseManager.readPlantById(plantId)

    override suspend fun deletePlantById(plantId: String): Unit =
        databaseManager.deletePlantById(plantId)

    override fun readAllPlantsFlow(): Flow<List<Plant>> = databaseManager.readAllPlantsFlow()
}