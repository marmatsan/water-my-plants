package com.marmatsan.catalog_data.mapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.catalog_domain.model.PlantSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalTime

class PlantMapperTest {

    private lateinit var plantData: PlantData
    private lateinit var realmPlantData: RealmPlantData

    private data class PlantData(
        val name: String = "Monstera",
        val wateringDays: List<DayOfWeek> = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
        val wateringTime: LocalTime = LocalTime.parse("18:00"),
        val waterAmount: Int = 4000,
        val size: PlantSize = PlantSize.LARGE,
        val description: String = "Monstera plant",
        val shortDescription: String = "Monstera plant short description",
        val watered: Boolean = false
    )

    private data class RealmPlantData(
        val name: String = "Monstera",
        val wateringDays: String = "MONDAY,TUESDAY",
        val wateringTime: String = "18:00",
        val waterAmount: Int = 4000,
        val size: String = "LARGE",
        val description: String = "Monstera plant",
        val shortDescription: String = "Monstera plant short description",
        val watered: Boolean = false
    )

    @BeforeEach
    fun setUp() {
        plantData = PlantData()
        realmPlantData = RealmPlantData()
    }

    @Test
    fun `Given a Plant, toRealmPlant mapper outputs a correct RealmPlant`() {
        // GIVEN
        val plant = Plant(
            name = plantData.name, // Uri to be mocked
            wateringDays = plantData.wateringDays,
            wateringTime = plantData.wateringTime,
            waterAmount = plantData.waterAmount,
            size = plantData.size,
            description = plantData.description,
            shortDescription = plantData.shortDescription,
            watered = plantData.watered
        )

        // ACTION
        val realmPlant = plant.toRealmPlant()

        // ASSERTION
        assertThat(realmPlant.name).isEqualTo(realmPlantData.name)
        assertThat(realmPlant.wateringDays).isEqualTo(realmPlantData.wateringDays)
        assertThat(realmPlant.wateringTime).isEqualTo(realmPlantData.wateringTime)
        assertThat(realmPlant.waterAmount).isEqualTo(realmPlantData.waterAmount)
        assertThat(realmPlant.size).isEqualTo(realmPlantData.size)
        assertThat(realmPlant.description).isEqualTo(realmPlantData.description)
        assertThat(realmPlant.shortDescription).isEqualTo(realmPlantData.shortDescription)
        assertThat(realmPlant.watered).isEqualTo(realmPlantData.watered)
    }

    @Test
    fun `Given a RealmPlant, toPlant mappers outputs a correct Plant`() {
        // GIVEN
        val realmPlant = RealmPlant().apply {
            name = realmPlantData.name
            wateringDays = realmPlantData.wateringDays
            wateringTime = realmPlantData.wateringTime
            waterAmount = realmPlantData.waterAmount
            size = realmPlantData.size
            description = realmPlantData.description
            shortDescription = realmPlantData.shortDescription
            watered = realmPlantData.watered
        }

        // ACTION
        val plant = realmPlant.toPlant()

        // ASSERTION
        assertThat(plant.name).isEqualTo(plantData.name)
        assertThat(plant.wateringDays).isEqualTo(plantData.wateringDays)
        assertThat(plant.wateringTime).isEqualTo(plantData.wateringTime)
        assertThat(plant.waterAmount).isEqualTo(plantData.waterAmount)
        assertThat(plant.size).isEqualTo(plantData.size)
        assertThat(plant.description).isEqualTo(plantData.description)
        assertThat(plant.shortDescription).isEqualTo(plantData.shortDescription)
        assertThat(plant.watered).isEqualTo(plantData.watered)
    }

}