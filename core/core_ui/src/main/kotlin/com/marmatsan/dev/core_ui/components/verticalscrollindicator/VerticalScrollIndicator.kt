package com.marmatsan.dev.core_ui.components.verticalscrollindicator

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.util.shortSegmentOfGoldenRatio

// TODO: To be improved visually
@Composable
fun VerticalScrollIndicator(
    modifier: Modifier = Modifier,
    scrollState: ScrollState
) {
    val colorScheme = colorScheme

    var tmpTopLeftYCoordinate by remember {
        mutableFloatStateOf(0f)
    }
    var topLeftYCoordinate by remember {
        mutableFloatStateOf(0f)
    }

    val animatedTopLeftYCoordinate by animateFloatAsState(
        targetValue = topLeftYCoordinate,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "animatedTopLeftYCoordinate"
    )

    val normalizedScrollValue = scrollState.value.toFloat() / scrollState.maxValue.toFloat()
    val isDragged by scrollState.interactionSource.collectIsDraggedAsState()

    Canvas(
        modifier = modifier.width(6.dp)
    ) {
        val indicatorHeight = shortSegmentOfGoldenRatio(this.size.height)
        val maxTopLeftYCoordinate = this.size.height - indicatorHeight

        tmpTopLeftYCoordinate = topLeftYCoordinate

        topLeftYCoordinate = computeTopLeftYCoordinate(
            normalizedScrollValue = normalizedScrollValue,
            maxTopLeftYCoordinate = maxTopLeftYCoordinate,
            isDragged = isDragged,
            tmpTopLeftYCoordinate = tmpTopLeftYCoordinate
        )

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
                y = animatedTopLeftYCoordinate
            ),
            size = Size(
                width = this.size.width,
                height = indicatorHeight
            ),
            cornerRadius = CornerRadius(100f)
        )
    }
}

private fun computeTopLeftYCoordinate(
    normalizedScrollValue: Float,
    maxTopLeftYCoordinate: Float,
    isDragged: Boolean,
    tmpTopLeftYCoordinate: Float
): Float = if (normalizedScrollValue == 0f)
    0f
else if (normalizedScrollValue == 1f)
    maxTopLeftYCoordinate
else if (isDragged)
    normalizedScrollValue * maxTopLeftYCoordinate
else
    tmpTopLeftYCoordinate

@Preview(
    heightDp = 372
)
@Composable
private fun ScrollIndicatorPreview() {
    WaterMyPlantsTheme {
        VerticalScrollIndicator(
            scrollState = rememberScrollState()
        )
    }
}