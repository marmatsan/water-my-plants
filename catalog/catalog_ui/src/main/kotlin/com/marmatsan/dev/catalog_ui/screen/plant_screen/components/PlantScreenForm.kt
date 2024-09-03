package com.marmatsan.dev.catalog_ui.screen.plant_screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.catalog_ui.R
import com.marmatsan.dev.catalog_domain.model.PlantDataConstraints
import com.marmatsan.dev.catalog_domain.model.PlantSize
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_domain.isNotNull
import com.marmatsan.dev.core_domain.length
import com.marmatsan.dev.core_ui.components.customtextfield.CustomTextField
import com.marmatsan.dev.core_ui.components.picker.Picker
import com.marmatsan.dev.core_ui.components.textfield.TextField
import com.marmatsan.dev.core_ui.extension.bringIntoViewRequester
import com.marmatsan.dev.core_ui.extension.clearFocusOnKeyboardDismiss
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.elevation
import com.marmatsan.dev.core_ui.theme.padding
import java.time.DayOfWeek
import java.time.LocalTime

object PlantScreenFormStyle {

    @Composable
    fun textFieldColors(): TextFieldColors = TextFieldDefaults.colors().copy(
        unfocusedTextColor = colorScheme.onSurface,
        unfocusedContainerColor = colorScheme.secondaryContainer,
        unfocusedLabelColor = colorScheme.onSurfaceVariant,
        unfocusedIndicatorColor = Color.Transparent,
        focusedTextColor = colorScheme.onSurface,
        focusedContainerColor = colorScheme.secondaryContainer,
        focusedLabelColor = colorScheme.onSurfaceVariant,
        focusedIndicatorColor = Color.Transparent
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
        modifier = modifier.fillMaxSize(),
        shadowElevation = elevation.level3
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current

        // Form
        Column(
            modifier = Modifier
                .padding(
                    start = padding.medium,
                    top = padding.medium,
                    end = padding.medium,
                    bottom = padding.none
                ),
            verticalArrangement = Arrangement.spacedBy(
                padding.medium,
                Alignment.Top
            ),
            horizontalAlignment = Alignment.Start,
        ) {
            // Actions()
            // Content
            Row(
                modifier = Modifier
                    .padding(
                        start = padding.none,
                        top = padding.none,
                        end = padding.none,
                        bottom = padding.none
                    ),
                horizontalArrangement = Arrangement.spacedBy(
                    padding.medium,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.Top,
            ) {
                // Text fields
                Column(
                    modifier = Modifier
                        .padding(
                            start = padding.none,
                            top = padding.none,
                            end = padding.none,
                            bottom = padding.none
                        )
                        .weight(1f)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(
                        padding.medium,
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
                                text = stringResource(R.string.plant_screen_text_field_label_plant_name)
                            )
                        },
                        supportingText = if (name.isNotNull()) {
                            {
                                Text(
                                    text = "${name.length}/${PlantDataConstraints.PLANT_NAME_MAX_LENGTH}",
                                    style = typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        } else null,
                        textFieldColors = PlantScreenFormStyle.textFieldColors(),
                        textFieldShape = PlantScreenFormStyle.textFieldShape
                    )
                    // Watering days, Watering time
                    Row(
                        modifier = Modifier
                            .padding(all = padding.none)
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.spacedBy(
                            padding.medium,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Picker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(5f)
                                .height(TextFieldDefaults.MinHeight),
                            value = wateringDays?.joinToString(separator = ", ") { item ->
                                when (item) {
                                    DayOfWeek.MONDAY -> "Monday"
                                    DayOfWeek.TUESDAY -> "Tuesday"
                                    DayOfWeek.WEDNESDAY -> "Wednesday"
                                    DayOfWeek.THURSDAY -> "Thursday"
                                    DayOfWeek.FRIDAY -> "Friday"
                                    DayOfWeek.SATURDAY -> "Saturday"
                                    DayOfWeek.SUNDAY -> "Sunday"
                                }
                            },
                            label = {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(R.string.plant_screen_text_field_label_watering_days),
                                    maxLines = 1,
                                    color = colorScheme.onSurfaceVariant,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            onClick = onWateringDaysClick
                        )
                        Picker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(5f)
                                .height(TextFieldDefaults.MinHeight),
                            value = wateringTime?.toString(),
                            label = {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(R.string.plant_screen_text_field_label_watering_time),
                                    maxLines = 1,
                                    color = colorScheme.onSurfaceVariant,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            onClick = onWateringTimeClick
                        )
                    }
                    // Water amount, Plant size
                    Row(
                        modifier = Modifier
                            .padding(all = padding.none)
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.spacedBy(
                            padding.medium,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.Top,
                    ) {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(5f)
                                .clearFocusOnKeyboardDismiss(),
                            value = waterAmount?.toString() ?: String.Empty,
                            onValueChange = { newWaterAmount ->
                                onWaterAmountChange?.invoke(newWaterAmount)
                            },
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                            label = {
                                Text(
                                    text = stringResource(R.string.plant_screen_text_field_label_water_amount),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingText = if (waterAmount.isNotNull()) {
                                {
                                    Text(
                                        text = "${waterAmount.length}/${PlantDataConstraints.WATER_AMOUNT_MAX_LENGTH}",
                                        style = typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            } else null,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Number
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                }
                            ),
                            suffix = {
                                Text(
                                    text = "ml",
                                    style = typography.bodyLarge,
                                    color = colorScheme.onSurfaceVariant
                                )
                            },
                            textFieldColors = PlantScreenFormStyle.textFieldColors(),
                            textFieldShape = PlantScreenFormStyle.textFieldShape
                        )
                        Picker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(5f)
                                .height(TextFieldDefaults.MinHeight),
                            value = plantSize?.let { plantSize ->
                                when (plantSize) {
                                    PlantSize.EXTRA_LARGE -> "Extra large"
                                    PlantSize.LARGE -> "Large"
                                    PlantSize.MEDIUM -> "Medium"
                                    PlantSize.SMALL -> "Small"
                                }
                            },
                            label = {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(R.string.plant_screen_text_field_label_plant_size),
                                    maxLines = 1,
                                    color = colorScheme.onSurfaceVariant,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            onClick = onPlantSizeClick
                        )
                    }
                    // Description
                    CustomTextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .clearFocusOnKeyboardDismiss()
                            .bringIntoViewRequester(),
                        value = description ?: String.Empty,
                        onValueChange = {
                            onDescriptionChange?.invoke(it)
                        },
                        singleLine = false,
                        label = { textStyle ->
                            Text(
                                text = "Description",
                                style = textStyle,
                                color = colorScheme.onSurfaceVariant
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
                        )
                    )
                }
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