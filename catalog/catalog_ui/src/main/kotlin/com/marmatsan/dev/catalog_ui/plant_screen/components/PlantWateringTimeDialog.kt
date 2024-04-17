package com.marmatsan.dev.catalog_ui.plant_screen.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.catalog_ui.R
import com.marmatsan.dev.core_ui.components.dialog.Dialog
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantWateringTimeDialog(
    modifier: Modifier = Modifier,
    wateringTime: LocalTime? = null,
    onCancelWateringTimeDialog: (() -> Unit)? = null,
    onConfirmWateringTimeDialog: ((LocalTime?) -> Unit)? = null,
) {
    val state = rememberTimePickerState(
        initialHour = wateringTime?.hour ?: 0,
        initialMinute = wateringTime?.minute ?: 0,
        is24Hour = true
    )
    val formatter = remember { SimpleDateFormat("hh:mm", Locale.getDefault()) }

    Dialog(
        modifier = modifier,
        headline = stringResource(id = R.string.plant_screen_dialog_watering_time_headline),
        dismissRequestActionLabel = stringResource(id = R.string.plant_screen_dialog_action2),
        acceptRequestActionLabel = stringResource(id = R.string.plant_screen_dialog_action1),
        onDismissRequest = {
            onCancelWateringTimeDialog?.invoke()
        },
        onAcceptRequest = {
            val date: Date? = formatter.parse("${state.hour}:${state.minute}")
            val localTime: LocalTime? =
                date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalTime()
            onConfirmWateringTimeDialog?.invoke(localTime)
        }
    ) {
        TimeInput(
            modifier = Modifier/*.border( //TODO style
                border = BorderStroke(width = 4.dp, Color.Black)
            )*/,
            state = state
        )
    }
}

@Preview
@Composable
fun PlantTimePickerInputPreview() {
    WaterMyPlantsTheme {
        PlantWateringTimeDialog()
    }
}