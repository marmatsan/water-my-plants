package com.marmatsan.android_template.di

import com.marmatsan.android_template.domain.GreetingUseCase
import me.tatarka.inject.annotations.Component

@Component
abstract class AppComponent {
    abstract val greetingUseCase: GreetingUseCase
}

fun createAppComponent(): AppComponent = AppComponent::class.create()