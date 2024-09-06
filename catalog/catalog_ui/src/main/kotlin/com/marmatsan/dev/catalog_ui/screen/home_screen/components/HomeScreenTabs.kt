package com.marmatsan.dev.catalog_ui.screen.home_screen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.marmatsan.catalog_ui.R
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.padding
import com.marmatsan.dev.core_ui.util.shortSegmentOfGoldenRatio

enum class TabPage {
    UPCOMING,
    FORGOT_TO_WATER,
    HISTORY
}

private data class TabData(
    val leftCorner: Offset,
    val textSize: IntSize
)

@Composable
fun HomeScreenTabs(
    modifier: Modifier = Modifier,
    tabSelected: TabPage = TabPage.UPCOMING,
    onTabSelected: ((tabPage: TabPage) -> Unit)? = null
) {

    var upcomingTopLeftCorner by remember {
        mutableStateOf(Offset.Zero)
    }

    var forgotToWaterTopLeftCorner by remember {
        mutableStateOf(Offset.Zero)
    }

    var historyTopLeftCorner by remember {
        mutableStateOf(Offset.Zero)
    }

    var upcomingTextSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    var forgotToWaterTextSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    var historyTextSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    var selectedTabData by remember {
        mutableStateOf(
            TabData(
                leftCorner = upcomingTopLeftCorner,
                textSize = upcomingTextSize
            )
        )
    }

    LaunchedEffect(tabSelected) {
        selectedTabData = when (tabSelected) {
            TabPage.UPCOMING -> TabData(
                leftCorner = upcomingTopLeftCorner,
                textSize = upcomingTextSize
            )

            TabPage.FORGOT_TO_WATER -> TabData(
                leftCorner = forgotToWaterTopLeftCorner,
                textSize = forgotToWaterTextSize
            )

            TabPage.HISTORY -> TabData(
                leftCorner = historyTopLeftCorner,
                textSize = historyTextSize
            )
        }
    }

    TabRow(
        modifier = modifier,
        selectedTabIndex = tabSelected.ordinal,
        containerColor = Color.Transparent,
        indicator = { _ ->
            HomeScreenTabsIndicator(
                start = Offset(
                    x = selectedTabData.leftCorner.x,
                    y = selectedTabData.leftCorner.y + selectedTabData.textSize.height
                ),
                end = Offset(
                    x = selectedTabData.leftCorner.x + (selectedTabData.textSize.width - shortSegmentOfGoldenRatio(selectedTabData.textSize.width)) ,
                    y = selectedTabData.leftCorner.y + selectedTabData.textSize.height
                )
            )
        },
        divider = {}
    ) {
        HomeTab(
            selected = tabSelected == TabPage.UPCOMING,
            tabLabel = stringResource(id = R.string.components_home_screen_tabs_tab_1),
            onClick = {
                onTabSelected?.invoke(TabPage.HISTORY)
            },
            layoutData = { topLeft, size ->
                upcomingTopLeftCorner = topLeft
                upcomingTextSize = size
            }
        )
        HomeTab(
            selected = tabSelected == TabPage.FORGOT_TO_WATER,
            tabLabel = stringResource(id = R.string.components_home_screen_tabs_tab_2),
            onClick = {
                onTabSelected?.invoke(TabPage.FORGOT_TO_WATER)
            },
            layoutData = { topLeft, size ->
                forgotToWaterTopLeftCorner = topLeft
                forgotToWaterTextSize = size
            }
        )
        HomeTab(
            selected = tabSelected == TabPage.HISTORY,
            tabLabel = stringResource(id = R.string.components_home_screen_tabs_tab_3),
            onClick = {
                onTabSelected?.invoke(TabPage.HISTORY)
            },
            layoutData = { topLeft, size ->
                historyTopLeftCorner = topLeft
                historyTextSize = size
            }
        )
    }
}

@Composable
private fun HomeTab(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    tabLabel: String,
    onClick: () -> Unit,
    layoutData: ((Offset, IntSize) -> Unit)? = null
) {

    val color by animateColorAsState(
        targetValue = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant,
        label = "tab color"
    )

    Text(
        modifier = modifier
            .wrapContentSize()
            .clickable(onClick = onClick)
            .padding(padding.medium)
            .onGloballyPositioned { layoutCoordinates ->
                layoutData?.invoke(layoutCoordinates.positionInParent(), layoutCoordinates.size)
            },
        text = tabLabel,
        color = color,
        style = typography.titleMedium
    )
}

@Composable
private fun HomeScreenTabsIndicator(
    start: Offset,
    end: Offset
) {
    val colorScheme = colorScheme

    Canvas(
        modifier = Modifier.fillMaxWidth()
    ) {
        drawLine(
            color = colorScheme.primary,
            start = Offset(
                x = start.x + 1.dp.toPx(),
                y = start.y + 1.dp.toPx()
            ),
            end = Offset(
                x = end.x - 1.dp.toPx(),
                y = end.y + 1.dp.toPx()
            ),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Preview
@Composable
private fun TabPreview() {
    WaterMyPlantsTheme {
        HomeTab(
            tabLabel = stringResource(id = R.string.components_home_screen_tabs_tab_1),
            onClick = {}
        )
    }
}

@Preview(
    widthDp = 452
)
@Composable
private fun HomeScreenTabsPreview() {
    WaterMyPlantsTheme {
        HomeScreenTabs()
    }
}