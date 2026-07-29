package com.marmatsan.onboarding.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marmatsan.core.ui.theme.AndroidTemplateTheme
import com.marmatsan.core.ui.theme.margin
import com.marmatsan.core.ui.theme.padding

private enum class PagerIndicatorState {
    Current,
    NotCurrent
}

@Composable
fun OnboardingScreen(
    onAddPlantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surface
                )
    ) {
        Background(
            modifier =
                Modifier
                    .align(
                        alignment = Alignment.Center
                    )
        )
        Column(
            modifier =
                Modifier
                    .align(
                        alignment = Alignment.TopCenter
                    ).fillMaxWidth()
                    .height(
                        height = 329.333.dp
                    ).padding(
                        horizontal = margin.mediumIncreased
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Welcome to Water My Plants!",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
        Column(
            modifier =
                Modifier
                    .align(
                        alignment = Alignment.BottomCenter
                    ).offset(
                        y = (-131.74).dp
                    ).fillMaxWidth()
                    .height(
                        height = 526.933.dp
                    ).padding(
                        horizontal = margin.mediumIncreased,
                        vertical = margin.large
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = margin.large
                        ),
                contentAlignment = Alignment.Center
            ) {
                Illustration(
                    variant = IllustrationVariant.Illustration1
                )
            }
            Text(
                text = "Your personal AI assistant for timely plant watering",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onAddPlantClick,
                shape = MaterialTheme.shapes.extraLarge,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                contentPadding =
                    PaddingValues(
                        horizontal = padding.mediumIncreased,
                        vertical = padding.medium
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier =
                        Modifier.size(
                            size = 24.dp
                        )
                )
                Text(
                    text = "Add my first plant",
                    style = MaterialTheme.typography.titleMedium,
                    modifier =
                        Modifier.padding(
                            start = padding.small
                        )
                )
            }
        }
        Box(
            modifier =
                Modifier
                    .align(
                        alignment = Alignment.BottomCenter
                    ).size(
                        width = 484.dp,
                        height = 131.733.dp
                    ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        space = padding.medium
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PagerIndicatorSymbol(
                    state = PagerIndicatorState.Current
                )
                PagerIndicatorSymbol(
                    state = PagerIndicatorState.NotCurrent
                )
                PagerIndicatorSymbol(
                    state = PagerIndicatorState.NotCurrent
                )
            }
        }
    }
}

@Composable
private fun PagerIndicatorSymbol(
    state: PagerIndicatorState,
    modifier: Modifier = Modifier
) {
    val baseColor =
        when (state) {
            PagerIndicatorState.Current -> MaterialTheme.colorScheme.onSurface
            PagerIndicatorState.NotCurrent -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    Box(
        modifier =
            modifier
                .size(
                    size = 12.dp
                ).alpha(
                    alpha = if (state == PagerIndicatorState.NotCurrent) 0.4f else 1f
                ).background(
                    color = baseColor,
                    shape = MaterialTheme.shapes.extraLarge
                )
    )
}

@Preview(
    showBackground = true
)
@Composable
private fun OnboardingScreenPreview() {
    AndroidTemplateTheme {
        OnboardingScreen(
            onAddPlantClick = {},
            modifier =
                Modifier.size(
                    width = 484.dp,
                    height = 988.dp
                )
        )
    }
}
