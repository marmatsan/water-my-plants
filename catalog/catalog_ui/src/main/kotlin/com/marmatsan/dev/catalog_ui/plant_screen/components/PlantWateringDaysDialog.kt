package com.marmatsan.dev.catalog_ui.plant_screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.catalog_ui.R
import com.marmatsan.dev.core_domain.toggle
import com.marmatsan.dev.core_ui.components.dialog.Dialog
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import java.time.DayOfWeek

@Composable
fun PlantWateringDaysDialog(
    modifier: Modifier = Modifier,
    onCancelWateringDaysDialog: () -> Unit,
    onConfirmWateringDaysDialog: (List<DayOfWeek>?) -> Unit,
    wateringDays: List<DayOfWeek>? = null
) {
    var tmpWateringDays = remember {
        mutableStateListOf<DayOfWeek>()
    }

    if (wateringDays != null) {
        tmpWateringDays.addAll(wateringDays)
    }

    Dialog(
        modifier = modifier,
        headline = stringResource(id = R.string.plant_screen_dialog_watering_days_headline),
        dismissRequestActionLabel = stringResource(id = R.string.plant_screen_dialog_action2),
        acceptRequestActionLabel = stringResource(id = R.string.plant_screen_dialog_action1),
        onDismissRequest = onCancelWateringDaysDialog,
        onAcceptRequest = {
            onConfirmWateringDaysDialog(tmpWateringDays)
        },
        showTopDivider = true,
        showBottomDivider = true,
        content = {
            List(
                tmpWateringDays = tmpWateringDays,
                onWateringDayChange = { changedWateringDay ->
                    tmpWateringDays.toggle(changedWateringDay)
                }
            )
        }
    )
}

@Composable
fun List(
    tmpWateringDays: List<DayOfWeek>,
    onWateringDayChange: (DayOfWeek) -> Unit
) {
    DayOfWeek.entries.forEach { currentDayOfWeek ->
        ListItem(
            modifier = Modifier.clickable {
                onWateringDayChange(currentDayOfWeek)
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            headlineContent = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = when (currentDayOfWeek) {
                        DayOfWeek.MONDAY -> stringResource(id = R.string.plant_screen_dialog_watering_days_day1)
                        DayOfWeek.TUESDAY -> stringResource(id = R.string.plant_screen_dialog_watering_days_day2)
                        DayOfWeek.WEDNESDAY -> stringResource(id = R.string.plant_screen_dialog_watering_days_day3)
                        DayOfWeek.THURSDAY -> stringResource(id = R.string.plant_screen_dialog_watering_days_day4)
                        DayOfWeek.FRIDAY -> stringResource(id = R.string.plant_screen_dialog_watering_days_day5)
                        DayOfWeek.SATURDAY -> stringResource(id = R.string.plant_screen_dialog_watering_days_day6)
                        DayOfWeek.SUNDAY -> stringResource(id = R.string.plant_screen_dialog_watering_days_day7)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            leadingContent = {
                Checkbox(
                    checked = tmpWateringDays.contains(currentDayOfWeek),
                    onCheckedChange = {
                        onWateringDayChange(currentDayOfWeek)
                    }
                )
            }
        )
    }
}

@Preview
@Composable
fun PlantWateringDaysDialogPreview() {
    WaterMyPlantsTheme {
        PlantWateringDaysDialog(
            onCancelWateringDaysDialog = {},
            onConfirmWateringDaysDialog = {}
        )
    }
}