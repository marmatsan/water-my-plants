package com.marmatsan.catalog_data.repository

import com.marmatsan.catalog_data.local.DatabaseManagerLocalSource
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.core_domain.result.Error
import com.marmatsan.dev.core_domain.result.Result
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class CatalogRepositoryImpl(
    private val databaseManager: DatabaseManagerLocalSource
) : CatalogRepository {
    override suspend fun createPlant(plant: Plant): Result<Unit, Error> =
        databaseManager.createPlant(plant)

    override suspend fun readPlantById(plantId: String): Flow<Plant> =
        databaseManager.readPlantById(plantId)

    override suspend fun deletePlantById(plantId: String): Flow<Result<Plant, Error>> =
        databaseManager.deletePlantById(plantId)

    override fun readAllPlantsFlow(): Flow<List<Plant>> = databaseManager.readAllPlantsFlow()
}