package com.marmatsan.dev.core_ui.components.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.core_domain.isNotNull
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.spacing

@Composable
fun Picker(
    modifier: Modifier = Modifier,
    value: String? = null,
    label: @Composable () -> Unit = {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            text = "Label",
            color = colorScheme.onSurfaceVariant
        )
    },
    onClick: (() -> Unit)? = null
) {
    // state-layer
    Row(
        modifier = modifier
            .fillMaxSize()
            .clip(shapes.extraSmall)
            .background(colorScheme.secondaryContainer)
            .clickable(
                onClick = { onClick?.invoke() }
            )
            .padding(
                horizontal = spacing.none,
                vertical = spacing.extraSmall
            ),
        horizontalArrangement = Arrangement.spacedBy(
            space = spacing.none,
            alignment = Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = spacing.medium,
                    top = spacing.none,
                    end = spacing.none,
                    bottom = spacing.none
                ),
            verticalArrangement = Arrangement.spacedBy(
                space = spacing.none,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val showInputText = value.isNotNull() && value.isNotEmpty()
            // label-text-container
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                CompositionLocalProvider(
                    if (showInputText)
                        LocalTextStyle provides typography.bodySmall
                    else
                        LocalTextStyle provides typography.bodyLarge
                ) {
                    label()
                }
            }
            // input-text-container
            if (showInputText) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            initialDelayMillis = 500,
                            repeatDelayMillis = 0
                        ),
                    text = value!!,
                    maxLines = 1,
                    color = colorScheme.onSurface
                )
            }
        }
        // icon
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Rounded.ExpandMore,
                tint = colorScheme.onSurfaceVariant,
                contentDescription = null
            )
        }
    }
}

@Preview(
    widthDp = 210,
    heightDp = 56
)
@Composable
private fun PickerWithNoValuePreview() {
    WaterMyPlantsTheme {
        Picker()
    }
}

@Preview(
    widthDp = 210,
    heightDp = 56
)
@Composable
private fun PickerWithValuePreview() {
    WaterMyPlantsTheme {
        Picker(
            value = "Hello"
        )
    }
}