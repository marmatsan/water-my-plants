package com.marmatsan.dev.core_ui.components.custom.picker

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_ui.components.custom.textfield.TextField
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

@Composable
fun Picker(
    modifier: Modifier = Modifier,
    value: String = String.Empty,
    onValueChange: ((String) -> Unit) = { },
    label: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = ""
        )
    }
) {
    TextField(
        modifier = modifier,
        readOnly = true,
        enabled = false,
        value = value,
        onValueChange = onValueChange,
        label = label,
        trailingIcon = trailingIcon,
        textFieldColors = TextFieldDefaults.colors()
    )

}

@Preview
@Composable
fun PickerPreview() {
    WaterMyPlantsTheme {
        Picker(
            label = { Text(text = "Watering days*")}
        )
    }
}