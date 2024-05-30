package com.marmatsan.dev.catalog_ui.screen.home_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.dev.core_ui.components.iconbutton.IconButton
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.spacing

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    // active: Boolean,
    // onActiveChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    placeHolder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    interactionSource: InteractionSource = remember {
        MutableInteractionSource()
    }
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isFocused = interactionSource.collectIsFocusedAsState().value

/*    LaunchedEffect(active) {
        if (!active) {
            focusManager.clearFocus()
        }
    }*/

    Box(
        modifier = modifier
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            textStyle = typography.bodyLarge,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
            singleLine = true,
            modifier = Modifier
                .height(56.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { newFocusState ->
                    // onActiveChange(newFocusState.isFocused)
                }
                .semantics {
                    onClick {
                        focusRequester.requestFocus()
                        true
                    }
                },
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .clip(shapes.extraLarge)
                        .background(colorScheme.surfaceContainerHigh)
                        .border(
                            width = 1.dp,
                            color = if (isFocused) colorScheme.outline else colorScheme.primary,
                            shape = shapes.extraLarge
                        )
                        .padding(all = spacing.extraSmall)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        spacing.extraSmall,
                        Alignment.Start
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leadingIcon?.invoke()
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            placeHolder?.invoke()
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
}

@Preview
@Composable
private fun SearchBarPreview() {
    WaterMyPlantsTheme {
        SearchBar(
            query = "",
            onQueryChange = {},
            onSearch = {},
            // active = false,
            // onActiveChange = {},
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
}