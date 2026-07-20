package com.marmatsan.figmaDocumentationSync.plugin.bdd

import io.cucumber.java8.En
import io.cucumber.java8.ParameterDefinitionBody.A1
import java.time.Instant

@Suppress("ObjectLiteralToLambda")
class CucumberTypes : En {

    init {
        ParameterType(
            "instant",
            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z",
            object : A1<Instant> {
                override fun accept(
                    value: String
                ): Instant = Instant.parse(
                    value
                )
            }
        )
    }
}
