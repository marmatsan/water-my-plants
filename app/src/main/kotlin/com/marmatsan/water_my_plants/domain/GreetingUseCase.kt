package com.marmatsan.water_my_plants.domain

import com.marmatsan.water_my_plants.data.GreetingRepository
import me.tatarka.inject.annotations.Inject

class GreetingUseCase @Inject constructor(
    private val repository: GreetingRepository
) {
    operator fun invoke(
        name: String
    ) = repository.greeting(name)
}