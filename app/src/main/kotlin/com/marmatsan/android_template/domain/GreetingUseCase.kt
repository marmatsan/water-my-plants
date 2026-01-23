package com.marmatsan.android_template.domain

import com.marmatsan.android_template.data.GreetingRepository
import me.tatarka.inject.annotations.Inject

class GreetingUseCase @Inject constructor(
    private val repository: GreetingRepository
) {
    operator fun invoke(name: String) = repository.greeting(name)
}