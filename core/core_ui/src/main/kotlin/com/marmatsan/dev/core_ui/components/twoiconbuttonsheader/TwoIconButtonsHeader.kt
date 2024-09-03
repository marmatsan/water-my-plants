package com.marmatsan.dev.core_ui.components.twoiconbuttonsheader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.dev.core_ui.components.iconbutton.IconButton
import com.marmatsan.dev.core_ui.components.iconbutton.IconButtonStyle
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

@Composable
fun TwoIconButtonsHeader(
    modifier: Modifier = Modifier,
    showSecondaryButton: Boolean = false,
    primaryIconButtonIcon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null
        )
    },
    secondaryIconButtonIcon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Outlined.HideImage,
            contentDescription = null
        )
    },
    onPrimaryIconButtonClick: (() -> Unit)? = null,
    onSecondaryIconButtonClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            iconButtonStyle = IconButtonStyle.Filled,
            iconButtonColors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            icon = primaryIconButtonIcon,
            onClick = {
                onPrimaryIconButtonClick?.invoke()
            }
        )
        if (showSecondaryButton) {
            IconButton(
                iconButtonStyle = IconButtonStyle.Filled,
                iconButtonColors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                icon = secondaryIconButtonIcon,
                onClick = {
                    onSecondaryIconButtonClick?.invoke()
                }
            )
        }
    }
}

@Preview(
    widthDp = 484
)
@Composable
private fun TwoIconButtonsHeaderPreview() {
    WaterMyPlantsTheme {
        TwoIconButtonsHeader(
            showSecondaryButton = true
        )
    }
}