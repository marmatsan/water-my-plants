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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantTimePickerInput(
    modifier: Modifier = Modifier
) {
    val state = rememberTimePickerState(is24Hour = true)
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Dialog(
        headline = stringResource(id = R.string.plant_screen_dialog_watering_time_headline)
    ) {
        TimeInput(state = state)
    }
}

@Preview
@Composable
fun PlantTimePickerInputPreview() {
    WaterMyPlantsTheme {
        PlantTimePickerInput()
    }
}