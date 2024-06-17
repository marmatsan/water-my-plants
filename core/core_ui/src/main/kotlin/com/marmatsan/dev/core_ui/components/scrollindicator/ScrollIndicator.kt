package com.marmatsan.dev.core_ui.components.scrollindicator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.core_ui.extension.shortSegmentOfGoldenRatio
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

@Composable
fun ScrollIndicator(
    modifier: Modifier = Modifier,
    scrollState: ScrollState
) {
    val colorScheme = colorScheme

    Canvas(
        modifier = modifier.width(6.dp)
    ) {
        val indicatorHeight = this.size.height.shortSegmentOfGoldenRatio()
        val normalizedScrollValue = (scrollState.value.toFloat() / scrollState.maxValue.toFloat())
        
        val topLeftY = normalizedScrollValue * (this.size.height - indicatorHeight)

        // Background
        drawRoundRect(
            color = colorScheme.surfaceVariant,
            size = Size(
                width = this.size.width,
                height = this.size.height
            ),
            cornerRadius = CornerRadius(100f)
        )

        // Indicator
        drawRoundRect(
            color = colorScheme.onSurface,
            topLeft = Offset(
                x = 0.dp.toPx(),
                y = topLeftY
            ),
            size = Size(
                width = this.size.width,
                height = indicatorHeight
            ),
            cornerRadius = CornerRadius(100f)
        )
    }
}

@Preview(
    heightDp = 372
)
@Composable
private fun ScrollIndicatorPreview() {
    WaterMyPlantsTheme {
        ScrollIndicator(
            scrollState = rememberScrollState()
        )
    }
}