package com.marmatsan.dev.catalog_ui.screen.home_screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.catalog_ui.R
import com.marmatsan.dev.core_ui.components.iconbutton.IconButton
import com.marmatsan.dev.core_ui.components.iconbutton.IconButtonStyle
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.padding

@Composable
fun HomeScreenHeader(
    modifier: Modifier = Modifier,
    isSearching: Boolean? = null,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                PaddingValues(
                    horizontal = padding.none,
                    vertical = if (isSearching != true) padding.small else padding.extraSmall,
                )
            ),
        horizontalArrangement = if (isSearching != true)
            Arrangement.SpaceBetween
        else
            Arrangement.spacedBy(
                padding.medium, Alignment.Start
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LeadingElements(
            modifier = Modifier.weight(1f),
            isSearching = isSearching,
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearch
        )
        TrailingElements(
            isSearching = isSearching
        )
    }
}

@Composable
private fun LeadingElements(
    modifier: Modifier = Modifier,
    isSearching: Boolean? = null,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit
) = if (isSearching != true) {
    Text(
        modifier = modifier,
        text = stringResource(id = R.string.components_home_screen_header_header_title),
        style = typography.headlineMedium
    )
} else {
    SearchBar(
        modifier = modifier,
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        leadingIcon = {
            IconButton(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        tint = colorScheme.onSurfaceVariant,
                        contentDescription = null
                    )
                }
            )
        },
        placeHolder = {
            Text(
                text = "Search plant...",
                color = colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
private fun TrailingElements(
    isSearching: Boolean? = null,
) {
    Row(
        modifier = Modifier
            .padding(all = padding.none)
            .wrapContentSize(),
        horizontalArrangement = Arrangement.spacedBy(padding.small, Alignment.Start),
        verticalAlignment = Alignment.Top,
    ) {
        if (isSearching != true) {
            IconButton(
                iconButtonStyle = IconButtonStyle.Filled,
                iconButtonColors = IconButtonDefaults.filledIconButtonColors().copy(
                    containerColor = colorScheme.secondary,
                    contentColor = colorScheme.onSecondary
                ),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null
                    )
                }
            )
        }
        IconButton(
            iconButtonStyle = IconButtonStyle.Filled,
            iconButtonColors = IconButtonDefaults.filledIconButtonColors().copy(
                containerColor = colorScheme.secondary,
                contentColor = colorScheme.onSecondary
            ),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null
                )
            }
        )
    }
}

@Preview(
    widthDp = 484
)
@Composable
private fun HomeScreenHeaderPreview() {
    WaterMyPlantsTheme {
        HomeScreenHeader(
            isSearching = false,
            query = "",
            onQueryChange = {},
            onSearch = {}
        )
    }
}

@Preview(
    widthDp = 484
)
@Composable
private fun HomeScreenHeaderSearchingPreview() {
    WaterMyPlantsTheme {
        HomeScreenHeader(
            isSearching = true,
            query = "",
            onQueryChange = {},
            onSearch = {}
        )
    }
}