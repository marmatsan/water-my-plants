package com.marmatsan.dev.catalog_ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.catalog_ui.R
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_ui.components.button.Button
import com.marmatsan.dev.core_ui.components.button.ButtonStyle
import com.marmatsan.dev.core_ui.components.iconbutton.IconButton
import com.marmatsan.dev.core_ui.components.iconbutton.IconButtonStyle
import com.marmatsan.dev.core_ui.components.illustration.Design
import com.marmatsan.dev.core_ui.components.illustration.Illustration
import com.marmatsan.dev.core_ui.components.picker.Picker
import com.marmatsan.dev.core_ui.components.textfield.TextField
import com.marmatsan.dev.core_ui.dimensions.LocalDensity
import com.marmatsan.dev.core_ui.dimensions.LocalSpacing
import com.marmatsan.dev.core_ui.theme.LocalElevation
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

@Composable
fun PlantScreen(
    modifier: Modifier = Modifier,
    onCreatePlantClick: () -> Unit = {}
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background
            )
            .padding(
                all = spacing.default
            ),
        verticalArrangement = Arrangement.spacedBy(spacing.default, Alignment.Top),
        horizontalAlignment = Alignment.Start,
    ) {
        Header(
            modifier = modifier
                .fillMaxSize()
                .weight(5f)
                .padding(
                    all = spacing.default
                )
        )
        Form(
            modifier = modifier
                .fillMaxSize()
                .weight(5f)
        )
        ButtonContainer(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.background
                )
                .padding(
                    horizontal = spacing.medium,
                    vertical = spacing.small
                )
        )
    }

}

@Composable
fun Header(
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Box(
        modifier = modifier
    ) {
        Illustration(
            modifier = Modifier.fillMaxSize(),
            design = Design.Three
        )
        HeaderContent(
            modifier = modifier.padding(
                all = spacing.medium
            )
        )
    }
}

@Composable
fun HeaderContent(
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PlantScreenHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = spacing.default)
        )
        Image( // Plant icon
            painter = painterResource(id = com.marmatsan.core_ui.R.drawable.plant_icon),
            contentDescription = null
        )
        PlantScreenActions(
            modifier = Modifier
                .wrapContentSize()
                .padding(all = spacing.default)
        )
    }
}

@Composable
fun PlantScreenHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            iconButtonStyle = IconButtonStyle.Filled,
            iconButtonColors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    contentDescription = null
                )
            }

        )
        IconButton(
            iconButtonStyle = IconButtonStyle.Filled,
            iconButtonColors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.HideImage,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    contentDescription = null
                )
            }

        )
    }
}

@Composable
fun PlantScreenActions(
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            spacing.small,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            modifier = Modifier.height(ButtonDefaults.MinHeight + LocalDensity.current.positiveTwo),
            labelText = stringResource(id = R.string.plant_screen_button_add_image),
            labelTextColor = MaterialTheme.colorScheme.onSecondary,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            icon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Outlined.Add,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    contentDescription = null
                )
            }
        )
        IconButton(
            modifier = Modifier.size(48.dp),
            iconButtonStyle = IconButtonStyle.Filled,
            iconButtonColors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            icon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = ImageVector.vectorResource(com.marmatsan.core_ui.R.drawable.icon_filled_sparkle),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = null
                )
            }
        )
    }
}

@Composable
fun Form(
    modifier: Modifier = Modifier
) {
    var plantName by remember {
        mutableStateOf(String.Empty)
    }

    var waterAmount by remember {
        mutableStateOf(String.Empty)
    }

    val plantNameSupportingText: @Composable (() -> Unit)? = if (plantName.isNotBlank()) {
        {
            Text("${plantName.length}/100")
        }
    } else null

    val waterAmountSupportingText: @Composable (() -> Unit)? = {
        AnimatedVisibility(
            visible = waterAmount.isNotEmpty(),
            exit = slideOutVertically(
                animationSpec = tween(5000)
            )
        ) {
            Text("${waterAmount.length}/4")
        }

    }

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
                        value = plantName,
                        onValueChange = { newPlantName -> plantName = newPlantName },
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
                                .weight(5f)
                        )
                        Picker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(5f)
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
                            value = waterAmount,
                            onValueChange = { newWaterAmount -> waterAmount = newWaterAmount },
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
                                .weight(5f)
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

@Composable
fun ButtonContainer(
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.default, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(ButtonDefaults.MinHeight + 16.dp),
            buttonStyle = ButtonStyle.Outlined,
            labelText = "Create plant",
            icon = {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Outlined.Add,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = ""
                )
            }
        )
    }
}

@Preview
@Composable
fun PlantScreenPreview() {
    WaterMyPlantsTheme {
        PlantScreen {

        }
    }
}