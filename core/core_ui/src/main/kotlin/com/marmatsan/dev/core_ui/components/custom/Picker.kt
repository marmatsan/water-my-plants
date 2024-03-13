package com.marmatsan.dev.core_ui.components.custom

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

@Composable
fun Picker(
    modifier: Modifier = Modifier,
    value: String = String.Empty,
    onValueChange: ((String) -> Unit) = { },
    label: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = {
        Icon(
            imageVector = Icons.Rounded.ExpandMore,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = ""
        )
    }
) {
    var showDialog by remember {
        mutableStateOf(false)
    }

}

@Preview
@Composable
fun RelayCustomPickerPreview() {
    WaterMyPlantsTheme {
        Picker(
            value = "",
            label = { Text("Watering days*") }
        )
    }
}