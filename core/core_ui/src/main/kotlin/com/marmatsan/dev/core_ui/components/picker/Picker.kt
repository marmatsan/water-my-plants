package com.marmatsan.dev.core_ui.components.picker

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_ui.components.textfield.TextField
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

@Composable
fun Picker(
    modifier: Modifier = Modifier,
    value: String? = null,
    onValueChange: ((String) -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = {
        Icon(
            imageVector = Icons.Rounded.ExpandMore,
            contentDescription = ""
        )
    },
    onClick: (() -> Unit)? = null
) {
    TextField(
        modifier = modifier.clickable { onClick?.invoke() },
        readOnly = true,
        enabled = false,
        value = value ?: String.Empty,
        onValueChange = { newValue ->
            onValueChange?.invoke(newValue)
        },
        label = label,
        trailingIcon = trailingIcon,
        textFieldColors = TextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledIndicatorColor = Color.Transparent,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        textFieldShape = MaterialTheme.shapes.extraSmall
    )

}

@Preview
@Composable
fun PickerPreview() {
    WaterMyPlantsTheme {
        Picker(
            label = { Text(text = "Watering days*") }
        )
    }
}