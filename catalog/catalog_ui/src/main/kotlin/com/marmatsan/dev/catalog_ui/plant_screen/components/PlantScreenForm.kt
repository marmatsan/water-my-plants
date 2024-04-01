package com.marmatsan.dev.catalog_ui.plant_screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.catalog_ui.R
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_ui.components.picker.Picker
import com.marmatsan.dev.core_ui.components.textfield.TextField
import com.marmatsan.dev.core_ui.dimensions.LocalSpacing
import com.marmatsan.dev.core_ui.theme.LocalElevation
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import java.time.DayOfWeek

@Composable
fun PlantScreenForm(
    modifier: Modifier = Modifier,
    name: String? = null,
    onNameChange: ((String) -> Unit)? = null,
    wateringDays: List<DayOfWeek>? = null,
    onWateringDaysChange: ((List<DayOfWeek>) -> Unit)? = null,
    waterAmount: String? = null,
    onWaterAmountChange: ((String) -> Unit)? = null
) {

    val plantNameSupportingText: @Composable (() -> Unit)? = if (name?.isNotBlank() == true) {
        {
            Text("${name.length}/100")
        }
    } else null

    val waterAmountSupportingText: @Composable (() -> Unit)? =
        if (waterAmount?.isNotBlank() == true) {
            {
                Text("${waterAmount.length}/4")
            }
        } else null

    val spacing = LocalSpacing.current
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shadowElevation = LocalElevation.current.level3
    ) {
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
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = name ?: String.Empty,
                        onValueChange = { newName ->
                            onNameChange?.invoke(newName)
                        },
                        label = { Text(text = stringResource(id = R.string.plant_screen_text_field_label_plant_name)) },
                        supportingText = plantNameSupportingText,
                        textFieldColors = TextFieldDefaults.colors().copy(
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
                        ),
                        textFieldShape = MaterialTheme.shapes.extraSmall
                    )
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
                                .weight(5f),
                            label = {
                                Text(text = stringResource(id = R.string.plant_screen_text_field_label_watering_days))
                            },
                            value = wateringDays?.joinToString(", ") { it.name } ?: String.Empty,
                            onValueChange = {
                                
                            }
                        )
                        Picker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(5f),
                            label = {
                                Text(text = stringResource(id = R.string.plant_screen_text_field_label_watering_time))
                            }
                        )
                    }
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
                                .weight(5f),
                            value = waterAmount ?: String.Empty,
                            onValueChange = { newWaterAmount ->
                                onWaterAmountChange?.invoke(newWaterAmount)
                            },
                            label = { Text(text = stringResource(id = R.string.plant_screen_text_field_label_water_amount)) },
                            supportingText = waterAmountSupportingText,
                            textFieldColors = TextFieldDefaults.colors().copy(
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
                            ),
                            textFieldShape = MaterialTheme.shapes.extraSmall
                        )
                        Picker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(5f),
                            label = {
                                Text(text = stringResource(id = R.string.plant_screen_text_field_label_plant_size))
                            }
                        )
                    }
                    // TODO: Change for CustomTextField()
                    TextField(
                        modifier = Modifier.fillMaxSize()
                    )
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