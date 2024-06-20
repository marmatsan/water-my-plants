package com.marmatsan.dev.catalog_ui.screen.plant_screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.dev.catalog_domain.model.PlantDataConstraints
import com.marmatsan.dev.catalog_domain.model.PlantSize
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_domain.isNotNull
import com.marmatsan.dev.core_ui.components.customtextfield.CustomTextField
import com.marmatsan.dev.core_ui.components.picker.Picker
import com.marmatsan.dev.core_ui.components.textfield.TextField
import com.marmatsan.dev.core_ui.components.verticalscrollindicator.VerticalScrollIndicator
import com.marmatsan.dev.core_ui.extension.clearFocusOnKeyboardDismiss
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.elevation
import com.marmatsan.dev.core_ui.theme.spacing
import java.time.DayOfWeek
import java.time.LocalTime

object PlantScreenFormStyle {

    @Composable
    fun textFieldColors(): TextFieldColors = TextFieldDefaults.colors().copy(

    )

    val textFieldShape: Shape
        @Composable
        get() = shapes.extraSmall
}

@Composable
fun PlantScreenForm(
    modifier: Modifier = Modifier,
    name: String? = null,
    onNameChange: ((String) -> Unit)? = null,
    wateringDays: List<DayOfWeek>? = null,
    wateringTime: LocalTime? = null,
    waterAmount: Int? = null,
    onWaterAmountChange: ((String) -> Unit)? = null,
    plantSize: PlantSize? = null,
    description: String? = null,
    onDescriptionChange: ((String) -> Unit)? = null,
    onPlantSizeClick: (() -> Unit)? = null,
    onWateringDaysClick: (() -> Unit)? = null,
    onWateringTimeClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxSize(),
        shadowElevation = elevation.level3
    ) {
        val scrollState = rememberScrollState()
        val keyboardController = LocalSoftwareKeyboardController.current

        // Form
        Column(
            modifier = Modifier
                .padding(
                    start = spacing.medium,
                    top = spacing.medium,
                    end = spacing.medium,
                    bottom = spacing.default
                ),
            verticalArrangement = Arrangement.spacedBy(
                spacing.medium,
                Alignment.Top
            ),
            horizontalAlignment = Alignment.Start,
        ) {
            // Actions()
            // Content
            Row(
                modifier = Modifier
                    .padding(
                        start = spacing.default,
                        top = spacing.default,
                        end = spacing.default,
                        bottom = spacing.default
                    ),
                horizontalArrangement = Arrangement.spacedBy(
                    spacing.medium,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.Top,
            ) {
                // Text fields
                Column(
                    modifier = Modifier
                        .padding(
                            start = spacing.default,
                            top = spacing.default,
                            end = spacing.default,
                            bottom = spacing.default
                        )
                        .weight(1f)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .height(IntrinsicSize.Max),
                    verticalArrangement = Arrangement.spacedBy(
                        spacing.medium,
                        Alignment.Top
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Plant name
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clearFocusOnKeyboardDismiss(),
                        value = name ?: String.Empty,
                        onValueChange = {
                            onNameChange?.invoke(it)
                        },
                        label = {
                            Text(
                                text = "Plant name"
                            )
                        },
                        supportingText = null,
                        textFieldShape = shapes.extraSmall
                    )
                    // Watering days, Watering time
                    Row(
                        modifier = Modifier
                            .padding(all = spacing.default)
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.spacedBy(
                            spacing.medium,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Picker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(5f)
                                .height(TextFieldDefaults.MinHeight),
                            label = {
                                Text(
                                    text = "Watering days",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            value = String.Empty,
                            onClick = { }
                        )
                        Picker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(5f)
                                .height(TextFieldDefaults.MinHeight),
                            value = String.Empty,
                            label = {
                                Text(
                                    text = "Watering time",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            onClick = { }
                        )
                    }
                    // Water amount, Plant size
                    Row(
                        modifier = Modifier
                            .padding(all = spacing.default)
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.spacedBy(
                            spacing.medium,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.Top,
                    ) {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(5f)
                                .clearFocusOnKeyboardDismiss(),
                            value = String.Empty,
                            onValueChange = {},
                            label = {
                                Text(
                                    text = "Water amount",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingText = null,
                            suffix = { },
                            textFieldShape = shapes.extraSmall
                        )
                        Picker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(5f)
                                .height(TextFieldDefaults.MinHeight),
                            label = {
                                Text(
                                    text = "Plant size",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            value = String.Empty,
                            onClick = { }
                        )
                    }
                    // Description
                    /* Box(
                         modifier = Modifier
                             .fillMaxSize()
                             .border(width = 4.dp, color = Color.Red)
                     )*/
                    CustomTextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .clearFocusOnKeyboardDismiss(),
                        value = description ?: String.Empty,
                        onValueChange = {
                            onDescriptionChange?.invoke(it)
                        },
                        singleLine = false,
                        label = { textStyle ->
                            Text(
                                text = "Description",
                                style = textStyle
                            )
                        },
                        supportingText = if (description.isNotNull()) {
                            { textStyle ->
                                Text(
                                    text = "${description.length}/${PlantDataConstraints.DESCRIPTION_MAX_LENGTH}",
                                    style = textStyle,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
                        onTextLayout = {

                        }
                    )
                }
                VerticalScrollIndicator(
                    modifier = Modifier.fillMaxHeight(),
                    scrollState = scrollState
                )
            }
        }
    }

}

@Preview(
    widthDp = 484,
    heightDp = 458
)
@Composable
fun PlantScreenFormPreview() {
    WaterMyPlantsTheme {
        PlantScreenForm()
    }
}