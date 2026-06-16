package com.marmatsan.water_my_plants.di

import com.marmatsan.water_my_plants.domain.GreetingUseCase
import me.tatarka.inject.annotations.Component

@Component
abstract class AppComponent {
    abstract val greetingUseCase: GreetingUseCase
}

fun createAppComponent(): AppComponent = AppComponent::class.create()