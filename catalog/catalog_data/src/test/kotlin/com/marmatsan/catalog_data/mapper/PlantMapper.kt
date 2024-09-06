package com.marmatsan.catalog_data.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isEqualToWithGivenProperties
import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.dev.catalog_domain.model.Plant
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.stream.Stream

class PlantMapperTest {

    companion object {
        class CustomArgumentProvider : ArgumentsProvider {
            override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> =
                Stream.of(
                    Arguments.of(
                        Plant(),
                        RealmPlant()
                    ),
                    Arguments.of(
                        Plant(
                            name = "Monstera"
                        ),
                        RealmPlant().apply {
                            name = "Monstera"
                        }
                    ),
                    Arguments.of(
                        Plant(
                            wateringDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
                        ),
                        RealmPlant().apply {
                            wateringDays = "MONDAY,TUESDAY"
                        }
                    ),
                    Arguments.of(
                        Plant(
                            wateringTime = LocalTime.of(12, 0)
                        ),
                        RealmPlant().apply {
                            wateringTime = "12:00"
                        }
                    )
                )
        }
    }

    @ParameterizedTest
    @ArgumentsSource(CustomArgumentProvider::class)
    fun `Given a Plant, toRealmPlant mapper outputs a correct RealmPlant`(
        plantToMap: Plant,
        expectedRealmPlant: RealmPlant
    ) {
        // GIVEN
        // plantToMap and expectedRealmPlant

        // ACTION
        val realmPlant = plantToMap.toRealmPlant()

        // ASSERTION
        assertThat(realmPlant).isEqualToWithGivenProperties(
            other = expectedRealmPlant,
            properties = arrayOf(
                RealmPlant::name,
                RealmPlant::wateringDays,
                RealmPlant::wateringTime,
                RealmPlant::image,
                RealmPlant::waterAmount,
                RealmPlant::size,
                RealmPlant::description,
                RealmPlant::shortDescription,
                RealmPlant::watered
            )
        )
    }

    @ParameterizedTest
    @ArgumentsSource(CustomArgumentProvider::class)
    fun `Given a RealmPlant, toPlant mappers outputs a correct Plant`(
        expectedPlant: Plant,
        realmPlantToMap: RealmPlant
    ) {
        // GIVEN
        // plantToMap and expectedRealmPlant

        // ACTION
        val plant = realmPlantToMap.toPlant()

        // ASSERTION
        assertThat(plant).isDataClassEqualTo(expectedPlant)
    }
}