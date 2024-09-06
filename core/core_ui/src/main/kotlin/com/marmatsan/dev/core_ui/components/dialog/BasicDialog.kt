package com.marmatsan.dev.core_ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MobileFriendly
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.marmatsan.dev.core_ui.components.button.Button
import com.marmatsan.dev.core_ui.components.button.ButtonStyle
import com.marmatsan.dev.core_ui.theme.LocalPadding
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.padding

@Composable
fun BasicDialog(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onDismissRequest: (() -> Unit)? = null,
    onAcceptRequest: (() -> Unit)? = null,
    dismissRequestActionLabel: String? = null,
    acceptRequestActionLabel: String? = null,
    showTopDivider: Boolean = false,
    showBottomDivider: Boolean = false,
    headline: String,
    supportingText: String? = null,
    content: @Composable (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = { onDismissRequest?.invoke() }
    ) {
        Surface(
            modifier = modifier
                .width(312.dp)
                .wrapContentHeight(),
            shape = shapes.extraLarge,
            color = colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier.padding(
                    top = if (icon == null) padding.none else padding.semiLarge
                ),
                verticalArrangement = Arrangement.spacedBy(
                    padding.none,
                    Alignment.CenterVertically
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                icon?.let {
                    Icon(
                        imageVector = icon,
                        tint = colorScheme.secondary,
                        contentDescription = null
                    )
                }
                TitleAndDescription(
                    modifier = modifier,
                    icon = icon,
                    headline = headline,
                    supportingText = supportingText,
                    content = content
                )
                if (showTopDivider) {
                    HorizontalDivider()
                }
                content?.invoke()
                if (showBottomDivider) {
                    HorizontalDivider()
                }
                Actions(
                    onDismissRequest = onDismissRequest,
                    onAcceptRequest = onAcceptRequest,
                    dismissRequestActionLabel = dismissRequestActionLabel,
                    acceptRequestActionLabel = acceptRequestActionLabel
                )
            }
        }
    }
}

@Composable
private fun TitleAndDescription(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    headline: String,
    supportingText: String? = null,
    content: @Composable() (() -> Unit)? = null
) {
    val padding = LocalPadding.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                start = padding.semiLarge,
                end = padding.semiLarge,
                top = padding.semiLarge,
                bottom = if (content == null) padding.none else padding.semiLarge
            ),
        verticalArrangement = Arrangement.spacedBy(padding.medium, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text( // Headline
            modifier = Modifier.fillMaxWidth(),
            text = headline,
            color = colorScheme.onSurface,
            style = typography.headlineSmall,
            textAlign = if (icon == null) TextAlign.Start else TextAlign.Center
        )
        supportingText?.let {
            Text( // Supporting text
                modifier = Modifier.fillMaxWidth(),
                text = supportingText,
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Actions(
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    onAcceptRequest: (() -> Unit)? = null,
    dismissRequestActionLabel: String? = null,
    acceptRequestActionLabel: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(padding.none, Alignment.Top),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.padding(
                start = padding.small,
                top = padding.semiLarge,
                end = padding.semiLarge,
                bottom = padding.semiLarge
            ),
            horizontalArrangement = Arrangement.spacedBy(padding.small, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            dismissRequestActionLabel?.let {
                Button(
                    buttonStyle = ButtonStyle.Text,
                    labelText = dismissRequestActionLabel,
                    onClick = { onDismissRequest?.invoke() }
                )
            }
            acceptRequestActionLabel?.let {
                Button(
                    buttonStyle = ButtonStyle.Text,
                    labelText = acceptRequestActionLabel,
                    onClick = { onAcceptRequest?.invoke() }
                )
            }
        }
    }
}

@Preview
@Composable
private fun BasicDialogNoHeroIconPreview() {
    WaterMyPlantsTheme {
        BasicDialog(
            icon = null,
            headline = "Basic dialog title",
            acceptRequestActionLabel = "Accept",
            dismissRequestActionLabel = "Cancel",
            supportingText = "A dialog is a type of modal window that appears in front of app content to provide critical information, or prompt for a decision to be made."
        )
    }
}

@Preview
@Composable
private fun BasicDialogHeroIconPreview() {
    WaterMyPlantsTheme {
        BasicDialog(
            icon = Icons.Outlined.MobileFriendly,
            headline = "Dialog with hero icon",
            acceptRequestActionLabel = "Accept",
            dismissRequestActionLabel = "Cancel",
            supportingText = "A dialog is a type of modal window that appears in front of app content to provide critical information, or prompt for a decision to be made."
        )
    }
}