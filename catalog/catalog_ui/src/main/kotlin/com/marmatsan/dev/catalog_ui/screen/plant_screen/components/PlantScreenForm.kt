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
import androidx.compose.ui.platform.LocalFocusManager
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
import com.marmatsan.dev.core_ui.components.picker.Picker
import com.marmatsan.dev.core_ui.components.textfield.TextField
import com.marmatsan.dev.core_ui.extension.bringIntoViewRequester
import com.marmatsan.dev.core_ui.extension.clearFocusOnKeyboardDismiss
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.elevation
import com.marmatsan.dev.core_ui.theme.spacing
import java.time.DayOfWeek
import java.time.LocalTime

object PlantScreenFormStyle {
    @Composable
    fun textFieldColors(): TextFieldColors = TextFieldDefaults.colors().copy(
        unfocusedTextColor = colorScheme.onSurface,
        unfocusedContainerColor = colorScheme.secondaryContainer,
        unfocusedIndicatorColor = Color.Transparent,
        unfocusedLabelColor = colorScheme.onSurfaceVariant,
        unfocusedSupportingTextColor = colorScheme.onSurfaceVariant,
        focusedTextColor = colorScheme.onSurface,
        focusedContainerColor = colorScheme.secondaryContainer,
        focusedIndicatorColor = Color.Transparent,
        focusedLabelColor = colorScheme.onSurfaceVariant,
        focusedSupportingTextColor = colorScheme.onSurfaceVariant,
        cursorColor = colorScheme.onSecondaryContainer
    )

    @Composable
    fun pickerColors(): TextFieldColors = textFieldColors()

    val textFieldShape: Shape @Composable get() = shapes.extraSmall
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
        modifier = modifier,
        shadowElevation = elevation.level3
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

        // Form
        Column(
            modifier = Modifier.padding(
                start = spacing.medium,
                top = spacing.medium,
                end = spacing.medium,
                bottom = spacing.default
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.default, Alignment.Top),
            horizontalAlignment = Alignment.Start,
        ) {
            // Content
            Row(
                modifier = Modifier.padding(
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
                    modifier = Modifier.padding(
                        start = spacing.default,
                        top = spacing.default,
                        end = spacing.default,
                        bottom = spacing.default
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Plant name
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clearFocusOnKeyboardDismiss(),
                        value = name ?: String.Empty,
                        onValueChange = { newName ->
                            onNameChange?.invoke(newName)
                        },
                        label = {
                            Text(
                                text = stringResource(id = R.string.plant_screen_text_field_label_plant_name),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingText = if (name.isNotNull()) {
                            {
                                Text("${name.length}/${PlantDataConstraints.PLANT_NAME_MAX_LENGTH}")
                            }
                        } else null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        ),
                        textFieldColors = PlantScreenFormStyle.textFieldColors(),
                        textFieldShape = PlantScreenFormStyle.textFieldShape
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
                                    text = stringResource(id = R.string.plant_screen_text_field_label_watering_days),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            value = wateringDays?.let { wateringDays ->
                                val wateringDaysStringBuilder = StringBuilder()
                                val sortedWateringDays = wateringDays.sorted()

                                sortedWateringDays.forEachIndexed { position, wateringDay ->
                                    val stringResourceId = when (wateringDay) {
                                        DayOfWeek.MONDAY -> R.string.plant_screen_dialog_watering_days_day1
                                        DayOfWeek.TUESDAY -> R.string.plant_screen_dialog_watering_days_day2
                                        DayOfWeek.WEDNESDAY -> R.string.plant_screen_dialog_watering_days_day3
                                        DayOfWeek.THURSDAY -> R.string.plant_screen_dialog_watering_days_day4
                                        DayOfWeek.FRIDAY -> R.string.plant_screen_dialog_watering_days_day5
                                        DayOfWeek.SATURDAY -> R.string.plant_screen_dialog_watering_days_day6
                                        DayOfWeek.SUNDAY -> R.string.plant_screen_dialog_watering_days_day7
                                    }
                                    wateringDaysStringBuilder.append(stringResource(id = stringResourceId))
                                    if (position != sortedWateringDays.lastIndex) {
                                        wateringDaysStringBuilder.append(", ")
                                    }
                                }
                                wateringDaysStringBuilder.toString()
                            } ?: String.Empty,
                            onClick = onWateringDaysClick
                        )
                        Picker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(5f),
                            value = wateringTime?.toString() ?: String.Empty,
                            label = {
                                Text(
                                    text = stringResource(id = R.string.plant_screen_text_field_label_watering_time),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            onClick = onWateringTimeClick
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
                            value = waterAmount?.toString() ?: String.Empty,
                            onValueChange = { newWaterAmount ->
                                onWaterAmountChange?.invoke(newWaterAmount)
                            },
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                            label = {
                                Text(
                                    text = stringResource(id = R.string.plant_screen_text_field_label_water_amount),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingText = if (waterAmount.isNotNull()) {
                                {
                                    Text("${waterAmount.length}/${PlantDataConstraints.WATER_AMOUNT_MAX_LENGTH}")
                                }
                            } else null,
                            suffix = {
                                Text(
                                    text = stringResource(id = R.string.plant_screen_text_field_prefix_water_amount),
                                    color = colorScheme.onSurfaceVariant,
                                    style = typography.bodyLarge
                                )
                            },
                            textFieldColors = PlantScreenFormStyle.textFieldColors(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            ),
                            textFieldShape = PlantScreenFormStyle.textFieldShape
                        )
                        Picker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(5f)
                                .height(TextFieldDefaults.MinHeight),
                            label = {
                                Text(
                                    text = stringResource(id = R.string.plant_screen_text_field_label_plant_size),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            value = plantSize?.let { plantSize ->
                                val stringResourceId = when (plantSize) {
                                    PlantSize.SMALL -> R.string.plant_screen_dialog_plant_size_option_1
                                    PlantSize.MEDIUM -> R.string.plant_screen_dialog_plant_size_option_2
                                    PlantSize.LARGE -> R.string.plant_screen_dialog_plant_size_option_3
                                    PlantSize.EXTRA_LARGE -> R.string.plant_screen_dialog_plant_size_option_4
                                }
                                stringResource(id = stringResourceId)
                            } ?: String.Empty,
                            onClick = onPlantSizeClick
                        )
                    }
                    // Description
                    TextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .clearFocusOnKeyboardDismiss()
                            .bringIntoViewRequester(),
                        value = description ?: String.Empty,
                        onValueChange = onDescriptionChange ?: {},
                        singleLine = false,
                        label = {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(id = R.string.plant_screen_text_field_label_description)
                            )
                        },
                        supportingText = if (description.isNotNull()) {
                            {
                                Text(
                                    text = "${description.length}/${PlantDataConstraints.DESCRIPTION_MAX_LENGTH}",
                                )
                            }
                        } else null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        ),
                        textFieldColors = PlantScreenFormStyle.textFieldColors(),
                        textFieldShape = PlantScreenFormStyle.textFieldShape
                    )
                    /*  CustomTextField(
                          modifier = Modifier.fillMaxSize(),
                          value = description ?: String.Empty,
                          onValueChange = onDescriptionChange ?: {},
                          singleLine = false,
                          label = { textStyle ->
                              Text(
                                  modifier = Modifier.fillMaxWidth(),
                                  text = stringResource(id = R.string.plant_screen_text_field_label_description),
                                  style = textStyle
                              )
                          },
                          supportingText = if (description.isNotNull()) {
                              { textStyle ->
                                  Text(
                                      text = "${description.length}/${PlantDataConstraints.DESCRIPTION_MAX_LENGTH}",
                                      style = textStyle
                                  )
                              }
                          } else null
                      )*/
                }
                // Scroll indicator: TODO
                // ScrollIndicator()
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