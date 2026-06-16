package com.marmatsan.water_my_plants.bdd.steps

import com.marmatsan.water_my_plants.data.GreetingRepository
import com.marmatsan.water_my_plants.domain.GreetingUseCase
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.shouldBe

class GreetingSteps {

    private lateinit var name: String
    private lateinit var message: String

    @Given("a person named {string}")
    fun aPersonNamed(name: String) {
        this.name = name
    }

    @When("the greeting is requested")
    fun theGreetingIsRequested() {
        val useCase = GreetingUseCase(
            repository = GreetingRepository()
        )

        message = useCase(name)
    }

    @Then("the greeting message should be {string}")
    fun theGreetingMessageShouldBe(expectedMessage: String) {
        message shouldBe expectedMessage
    }
}
