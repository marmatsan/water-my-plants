package com.marmatsan.dev.catalog_ui.plant_screen.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.catalog_ui.R
import com.marmatsan.dev.catalog_domain.model.PlantSize
import com.marmatsan.dev.core_ui.components.dialog.Dialog
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

@Composable
fun PlantSizeDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onAcceptRequest: () -> Unit,
    plantSize: PlantSize? = null,
    onPlantSizeChange: (PlantSize) -> Unit
) {
    Dialog(
        modifier = modifier,
        headline = stringResource(
            id = R.string.plant_screen_dialog_plant_size_headline
        ),
        dismissRequestActionLabel = stringResource(id = R.string.plant_screen_dialog_action2),
        acceptRequestActionLabel = stringResource(id = R.string.plant_screen_dialog_action1),
        showTopDivider = true,
        showBottomDivider = true,
        content = {
            List(
                plantSize = plantSize,
                onPlantSizeChange = onPlantSizeChange
            )
        },
        onDismissRequest = onDismissRequest,
        onAcceptRequest = onAcceptRequest
    )

}

@Composable
fun List(
    plantSize: PlantSize? = null,
    onPlantSizeChange: (PlantSize) -> Unit
) {
    PlantSize.entries.forEach { currentPlantSize ->
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            headlineContent = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = when (currentPlantSize) {
                        PlantSize.SMALL -> stringResource(id = R.string.plant_screen_dialog_plant_size_option_1)
                        PlantSize.MEDIUM -> stringResource(id = R.string.plant_screen_dialog_plant_size_option_2)
                        PlantSize.LARGE -> stringResource(id = R.string.plant_screen_dialog_plant_size_option_3)
                        PlantSize.EXTRA_LARGE -> stringResource(id = R.string.plant_screen_dialog_plant_size_option_4)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            leadingContent = {
                RadioButton(
                    modifier = Modifier.size(24.dp),
                    selected = currentPlantSize == plantSize,
                    onClick = {
                        onPlantSizeChange(currentPlantSize)
                    }
                )
            }
        )
    }
}

@Preview
@Composable
fun PlantSizeDialogPreview() {
    WaterMyPlantsTheme {
        PlantSizeDialog(
            onDismissRequest = {},
            onAcceptRequest = {},
            plantSize = PlantSize.EXTRA_LARGE,
            onPlantSizeChange = {}
        )
    }
}