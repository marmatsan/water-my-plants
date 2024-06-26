package com.marmatsan.dev.core_ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.marmatsan.dev.core_ui.components.button.Button
import com.marmatsan.dev.core_ui.components.button.ButtonStyle
import com.marmatsan.dev.core_ui.theme.LocalSpacing
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme

@Composable
fun Dialog(
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    onAcceptRequest: (() -> Unit)? = null,
    dismissRequestActionLabel: String? = null,
    acceptRequestActionLabel: String? = null,
    showTopDivider: Boolean = false,
    showBottomDivider: Boolean = false,
    headline: String,
    supportingText: String? = null,
    content: @Composable() (() -> Unit)? = null
) {
    val spacing = LocalSpacing.current

    Dialog(
        onDismissRequest = { onDismissRequest?.invoke() }
    ) {
        Surface(
            modifier = modifier
                .width(312.dp)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(
                    spacing.none,
                    Alignment.CenterVertically
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Content(
                    modifier = modifier,
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
fun Content(
    modifier: Modifier = Modifier,
    headline: String,
    supportingText: String? = null,
    content: @Composable() (() -> Unit)? = null
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                start = spacing.semiLarge,
                end = spacing.semiLarge,
                top = spacing.semiLarge,
                bottom = if (content == null) spacing.none else spacing.semiLarge
            ),
        verticalArrangement = Arrangement.spacedBy(spacing.medium, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text( // Headline
            modifier = Modifier.fillMaxWidth(),
            text = headline,
            style = MaterialTheme.typography.headlineSmall
        )
        supportingText?.let {
            Text( // Supporting text
                modifier = Modifier.fillMaxWidth(),
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun Actions(
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    onAcceptRequest: (() -> Unit)? = null,
    dismissRequestActionLabel: String? = null,
    acceptRequestActionLabel: String? = null
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(spacing.none, Alignment.Top),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.padding(
                start = spacing.small,
                top = spacing.semiLarge,
                end = spacing.semiLarge,
                bottom = spacing.semiLarge
            ),
            horizontalArrangement = Arrangement.spacedBy(spacing.small, Alignment.Start),
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
fun PlantSizeDialogPreview() {
    WaterMyPlantsTheme {
        Dialog(
            headline = "Dialog",
            acceptRequestActionLabel = "Accept",
            dismissRequestActionLabel = "Cancel",
            supportingText = "Custom dialog"
        )
    }
}