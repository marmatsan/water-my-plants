package com.marmatsan.water_my_plants.bdd.steps

import com.marmatsan.water_my_plants.data.GreetingRepository
import com.marmatsan.water_my_plants.domain.GreetingUseCase
import io.cucumber.java8.En
import io.kotest.matchers.shouldBe

class GreetingSteps : En {
    private lateinit var name: String
    private lateinit var message: String

    init {
        Given("a person named {string}") { name: String ->
            this.name = name
        }

        When("the greeting is requested") {
            val useCase =
                GreetingUseCase(
                    repository = GreetingRepository()
                )

            message = useCase(name)
        }

        Then("the greeting message should be {string}") { expectedMessage: String ->
            message shouldBe expectedMessage
        }
    }
}
