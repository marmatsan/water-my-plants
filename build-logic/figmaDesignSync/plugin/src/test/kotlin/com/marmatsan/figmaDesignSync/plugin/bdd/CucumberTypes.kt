package com.marmatsan.figmaDesignSync.plugin.bdd

import io.cucumber.java.ParameterType
import java.time.Instant

class CucumberTypes {

    @ParameterType("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")
    fun instant(value: String): Instant = Instant.parse(value)
}
