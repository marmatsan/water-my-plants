package com.marmatsan.water_my_plants.data

import me.tatarka.inject.annotations.Inject

class GreetingRepository @Inject constructor() {
    fun greeting(name: String) = "Hello $name!"
}