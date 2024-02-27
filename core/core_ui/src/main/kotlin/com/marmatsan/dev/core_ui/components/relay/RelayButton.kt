package com.marmatsan.dev.core_ui.components.relay

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.marmatsan.core_domain.R
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_ui.dimensions.LocalSpacing

@Composable
fun RelayButton(
    modifier: Modifier = Modifier,
    text: String = String.Empty,
    density: Dp,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current

    Button(
        modifier = Modifier.height(ButtonDefaults.MinHeight + density),
        onClick = onClick
    ) {
        Icon(
            modifier = modifier.size(18.dp),
            imageVector = icon,
            contentDescription = stringResource(id = R.string.cd_button_icon)
        )
        Text(
            modifier = modifier.padding(start = spacing.small),
            text = text
        )
    }
}